package vn.remove.photo.conten

import android.app.Activity
import android.app.Application
import android.content.res.Resources
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
import vn.remove.photo.logics.BoatModelRoom
import vn.remove.photo.mail.BoatLogicsIm
import vn.remove.photo.mail.BoatLogicsIm.Companion.DEFAULT_STRING
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

        AppLinkData.fetchDeferredAppLinkData(ack.application) { appLink ->
            val data1 = appLink?.targetUri.toString()
            if (data1 == "null") {
                val apps = object : AppsFlyerConversionListener {
                    override fun onConversionDataSuccess(data2: MutableMap<String, Any>?) {
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
                            activity = ack
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

                    override fun onConversionDataFail(p0: String?) {
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
                            activity = ack
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

                    override fun onAppOpenAttribution(p1: MutableMap<String, String>?) {
                    }

                    override fun onAttributionFailure(p2: String?) {
                    }
                }
                appsIniting(apps, ack)
            } else {
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
                    activity = ack
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
    ): String = DEFAULT_STRING.toUri().buildUpon().apply {
        appendQueryParameter(
            stringRes.getString(R.string.secure_get_parametr),
            stringRes.getString(R.string.secure_key)
        )
        appendQueryParameter(
            stringRes.getString(R.string.dev_tmz_key),
            TimeZone.getDefault().id
        )
        appendQueryParameter(stringRes.getString(R.string.gadid_key), gog)
        appendQueryParameter(stringRes.getString(R.string.deeplink_key), data2)
        appendQueryParameter(
            stringRes.getString(R.string.source_key),
            when (data2) {
                "null" -> data1?.get("media_source").toString()
                else -> deep
            }
        )
        Log.d("media_source", data1?.get("media_source").toString())
        appendQueryParameter(
            stringRes.getString(R.string.af_id_key), when (data2) {
                "null" -> {
                    AppsFlyerLib.getInstance().getAppsFlyerUID(fum)
                }
                else -> "null"
            }
        )
        appendQueryParameter(
            stringRes.getString(R.string.adset_id_key),
            data1?.get("adset_id").toString()
        )
        appendQueryParameter(
            stringRes.getString(R.string.campaign_id_key),
            data1?.get("campaign_id").toString()
        )
        appendQueryParameter(
            stringRes.getString(R.string.app_campaign_key),
            data1?.get("campaign").toString()
        )
        appendQueryParameter(
            stringRes.getString(R.string.adset_key),
            data1?.get("adset").toString()
        )
        appendQueryParameter(
            stringRes.getString(R.string.adgroup_key),
            data1?.get("adgroup").toString()
        )
        appendQueryParameter(
            stringRes.getString(R.string.orig_cost_key),
            data1?.get("orig_cost").toString()
        )
        appendQueryParameter(
            stringRes.getString(R.string.af_siteid_key),
            data1?.get("af_siteid").toString()
        )
    }.toString()

    private fun setLink(data1: MutableMap<String, Any>?, data2: String, activity: Activity) {
        OneSignal.initWithContext(activity.application)
        val key = "key2"
        OneSignal.setAppId(NOTIFIC)
        val campaign = data1?.get("campaign").toString()
        if (campaign == "null" && data2 == "null") {
            OneSignal.sendTag(key, "organic")
        } else if (data2 != "null") {
            OneSignal.sendTag(key, data2.replace("myapp://", "").substringBefore("/"))
        } else if (campaign != "null") {
            OneSignal.sendTag(key, campaign.substringBefore("_"))
        }
    }
}