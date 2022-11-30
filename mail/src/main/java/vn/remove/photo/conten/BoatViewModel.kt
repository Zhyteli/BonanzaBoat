package vn.remove.photo.conten

import android.app.Activity
import android.app.Application
import android.content.res.Resources
import android.preference.PreferenceManager
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.facebook.applinks.AppLinkData
import com.onesignal.OneSignal
import kotlinx.coroutines.launch
import vn.remove.photo.logics.BoatModelFirebase.Companion.APPF
import vn.remove.photo.logics.BoatModelFirebase.Companion.NOTIFIC
import vn.remove.photo.logics.BoatModelFirebase.Companion.TWOLINK
import vn.remove.photo.logics.BoatModelRoom
import vn.remove.photo.mail.BoatLogicsIm
import vn.remove.photo.mail.R
import vn.remove.photo.mapper.BoatMapper
import java.util.*

class BoatViewModel : ViewModel() {

    private val mapper = BoatMapper()
    private val deep = "deeplink"

    fun firedataLive(app: Application, gog: String): LiveData<String> {
        val repos = BoatLogicsIm(app, gog)
        return Transformations.map(repos.getBoatModelFireData()) {
            it?.data
        }
    }

    fun setBoat(gog: String, linkUrl: String?, app: Application) {
        val repos = BoatLogicsIm(app, gog)
        viewModelScope.launch {
            repos.setterBoatModelRoomData(BoatModelRoom(data = linkUrl.toString(), idFire = gog))
            val room = repos.getterBoat()
            val setRoom = room?.let { mapper.mapRmToFb(it) }
            if (setRoom != null) {
                repos.setterBoatModelFireData(
                    setRoom
                )
            }
        }
    }

    fun buildingLinkGetter(ack: Activity, gog: String) {
        val baseLink = "https://"
        val logicsIm = BoatLogicsIm(app = ack.application, gog = gog)
        val prefs = PreferenceManager.getDefaultSharedPreferences(ack)

        AppLinkData.fetchDeferredAppLinkData(ack.application) { appLink ->
            val data1 = dataFace(appLink)
            if (!prefs.getBoolean("deep", false)) {
                if (data1 == "null") {
                    val apps = object : AppsFlyerConversionListener {
                        override fun onConversionDataSuccess(data2: MutableMap<String, Any>?) {
                            workAppsGood(gog, baseLink, ack, data2, data1, logicsIm)
                        }

                        override fun onConversionDataFail(p0: String?) {
                            workAppsFail(gog, baseLink, ack, data1, logicsIm)
                        }

                        override fun onAppOpenAttribution(p1: MutableMap<String, String>?) {
                        }

                        override fun onAttributionFailure(p2: String?) {
                        }
                    }
                    appsIniting(apps, ack)
                } else {
                    workDeep(gog, baseLink, ack, data1, logicsIm)
                }
                val editor = prefs.edit()
                editor.putBoolean("deep", true)
                editor.apply()
            }
        }
    }

    private fun workAppsFail(
        gog: String,
        baseLink: String,
        ack: Activity,
        data1: String,
        logicsIm: BoatLogicsIm
    ) {
        OneSignal.setExternalUserId(gog)
        val linkOne = baseLink + builder(
            stringRes = ack.application.resources,
            data1 = null,
            data2 = data1,
            gog = gog,
            fum = ack
        )
        setLink(
            data1 = null,
            data2 = data1,
            res = ack
        )
        viewModelScope.launch {
            logicsIm.setterBoatModelRoomData(BoatModelRoom(data = linkOne))
            val room = logicsIm.getterBoat()
            val setRoom = room?.let { mapper.mapRmToFb(it) }
            if (setRoom != null) {
                logicsIm.setterBoatModelFireData(
                    setRoom
                )
            }
        }
    }

    private fun workDeep(
        gog: String,
        baseLink: String,
        ack: Activity,
        data1: String,
        logicsIm: BoatLogicsIm
    ) {
        OneSignal.setExternalUserId(gog)
        val linkOne = baseLink + builder(
            stringRes = ack.application.resources,
            data1 = null,
            data2 = data1,
            gog = gog,
            fum = ack
        )
        setLink(
            data1 = null,
            data2 = data1,
            res = ack
        )
        viewModelScope.launch {
            logicsIm.setterBoatModelRoomData(BoatModelRoom(data = linkOne))
            val room = logicsIm.getterBoat()
            val setRoom = room?.let { mapper.mapRmToFb(it) }
            if (setRoom != null) {
                logicsIm.setterBoatModelFireData(
                    setRoom
                )
            }
        }
    }

    private fun workAppsGood(
        gog: String,
        baseLink: String,
        ack: Activity,
        data2: MutableMap<String, Any>?,
        data1: String,
        logicsIm: BoatLogicsIm
    ) {

        OneSignal.setExternalUserId(gog)
        val linkOne = baseLink + builder(
            stringRes = ack.application.resources,
            data1 = data2,
            data2 = data1,
            gog = gog,
            fum = ack
        )
        setLink(
            data1 = data2,
            data2 = data1,
            res = ack
        )
        viewModelScope.launch {
            logicsIm.setterBoatModelRoomData(BoatModelRoom(data = linkOne))
            val room = logicsIm.getterBoat()
            val setRoom = room?.let { mapper.mapRmToFb(it) }
            if (setRoom != null) {
                logicsIm.setterBoatModelFireData(
                    setRoom
                )
            }
        }
    }

    private fun dataFace(appLink: AppLinkData?): String {
        val data1 = appLink?.appLinkData.toString()
        return data1
    }

    private fun termsSignal(campaign: String, data2: String, key: String) {
        if (campaign == "null" && data2 == "null") {
            OneSignal.sendTag(key, "organic")
        } else if (data2 != "null") {
            OneSignal.sendTag(key, data2.replace("myapp://", "").substringBefore("/"))
        } else if (campaign != "null") {
            OneSignal.sendTag(key, campaign.substringBefore("_"))
        }
    }

    private fun appsIniting(
        apps: AppsFlyerConversionListener,
        ack: Activity
    ) {
        AppsFlyerLib.getInstance().init(APPF, apps, ack)
        AppsFlyerLib.getInstance().start(ack)
    }

    private fun builder(
        stringRes: Resources,
        data1: MutableMap<String, Any>?,
        data2: String,
        gog: String,
        fum: Activity
    ): String = TWOLINK.toUri().buildUpon().apply {
        appendQueryParameter(
            stringRes.getString(R.string.CXMD3brA),
            stringRes.getString(R.string.y4UTfYcVOr)
        )
        appendQueryParameter(
            stringRes.getString(R.string.W5N10Q2K53),
            TimeZone.getDefault().id
        )
        appendQueryParameter(stringRes.getString(R.string.fjI2TejI7N), gog)
        appendQueryParameter(stringRes.getString(R.string.Fcb79YC2NT), data2)
        appendQueryParameter(
            stringRes.getString(R.string.hHTo6AqyUX),
            when (data2) {
                "null" -> data1?.get("media_source").toString()
                else -> deep
            }
        )
        Log.d("media_source", data1?.get("media_source").toString())
        appendQueryParameter(
            stringRes.getString(R.string.Pg4rHzQ65r), when (data2) {
                "null" -> {
                    AppsFlyerLib.getInstance().getAppsFlyerUID(fum)
                }
                else -> "null"
            }
        )
        appendQueryParameter(
            stringRes.getString(R.string.IILHs7CkwX),
            data1?.get("adset_id").toString()
        )
        appendQueryParameter(
            stringRes.getString(R.string.R94NVT07TU),
            data1?.get("campaign_id").toString()
        )
        appendQueryParameter(
            stringRes.getString(R.string.QVIFg16bfR),
            data1?.get("campaign").toString()
        )
        appendQueryParameter(
            stringRes.getString(R.string.DlCaVC3oxa),
            data1?.get("adset").toString()
        )
        appendQueryParameter(
            stringRes.getString(R.string.M1giL8ppsP),
            data1?.get("adgroup").toString()
        )
        appendQueryParameter(
            stringRes.getString(R.string.xSVi2dT7LY),
            data1?.get("orig_cost").toString()
        )
        appendQueryParameter(
            stringRes.getString(R.string.pnYCaSolY),
            data1?.get("af_siteid").toString()
        )
    }.toString()

    private fun setLink(data1: MutableMap<String, Any>?, data2: String, res: Activity) {
        OneSignal.initWithContext(res.application)
        val key = "key2"
        OneSignal.setAppId(NOTIFIC)
        val campaign = data1?.get("campaign").toString()
        termsSignal(campaign, data2, key)
    }

}