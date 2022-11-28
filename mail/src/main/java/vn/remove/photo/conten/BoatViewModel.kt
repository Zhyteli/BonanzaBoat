package vn.remove.photo.conten

import android.app.Activity
import android.app.Application
import android.content.Context
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
import vn.remove.photo.logics.BoatModelRoom
import vn.remove.photo.mail.BoatLogicsIm
import vn.remove.photo.mail.BoatLogicsIm.Companion.DEFAULT_STRING
import vn.remove.photo.mail.R
import vn.remove.photo.mapper.BoatMapper
import java.util.*

class BoatViewModel : ViewModel() {

    private val mapper = BoatMapper()

    fun liveDataFromDb(application: Application, gog: String): LiveData<String> {
        val repos = BoatLogicsIm(application, gog)
        return Transformations.map(repos.getBoatModelFireData()) {
            it?.data
        }
    }

    fun saveUrl(id: String, url: String?, application: Application){
        val repos = BoatLogicsIm(application, id)
        viewModelScope.launch {
            repos.setterBoatModelRoomData(BoatModelRoom(data = url.toString(), idFire = id))
            val room = repos.getterBoat()
            val setRoom = room?.let { mapper.mapRmToFb(it) }
            if (setRoom != null) {
                repos.setterBoatModelFireData(
                    setRoom
                )
            }
        }
    }

    fun dataAcquisition(activity: Activity, id: String) {

        val repository = BoatLogicsIm(application = activity.application, gadid = id)

        AppLinkData.fetchDeferredAppLinkData(activity.application) { appLink ->
            val fbData = appLink?.targetUri.toString()
            if (fbData == "null") {
                val conversionDataListener = object : AppsFlyerConversionListener {
                    override fun onConversionDataSuccess(apfData: MutableMap<String, Any>?) {
                        Log.d("mfail1", apfData.toString())
                        OneSignal.setExternalUserId(id)
                        val data = "https://" + buildLinkData(
                            res = activity.application.resources,
                            aData = apfData,
                            fData = fbData,
                            gadid = id,
                            activity = activity
                        )
                        tagOneSignal(
                            data1 = apfData,
                            data2 = fbData,
                            activity = activity
                        )
                        viewModelScope.launch {
                            repository.setterBoatModelRoomData(BoatModelRoom(data = data))
                            val room = repository.getterBoat()
                            val setRoom = room?.let { mapper.mapRmToFb(it) }
                            if (setRoom != null) {
                                repository.setterBoatModelFireData(
                                    setRoom
                                )
                            }
                        }
                    }

                    override fun onConversionDataFail(p0: String?) {
                        Log.d("mfail2", p0.toString())
                        OneSignal.setExternalUserId(id)
                        val data = "https://" + buildLinkData(
                            res = activity.application.resources,
                            aData = null,
                            fData = fbData,
                            gadid = id,
                            activity = activity
                        )
                        tagOneSignal(
                            data1 = null,
                            data2 = fbData,
                            activity = activity
                        )
                        viewModelScope.launch {
                            repository.setterBoatModelRoomData(BoatModelRoom(data = data))
                            val room = repository.getterBoat()
                            val setRoom = room?.let { mapper.mapRmToFb(it) }
                            if (setRoom != null) {
                                repository.setterBoatModelFireData(
                                    setRoom
                                )
                            }
                        }
                    }

                    override fun onAppOpenAttribution(p0: MutableMap<String, String>?) {
                    }

                    override fun onAttributionFailure(p0: String?) {
                    }
                }
                AppsFlyerLib.getInstance().init(DEV_KEY, conversionDataListener, activity)
                AppsFlyerLib.getInstance().start(activity)
            } else {
                Log.d("mfail3", "mfail3")
                OneSignal.setExternalUserId(id)
                val data = "https://" + buildLinkData(
                    res = activity.application.resources,
                    aData = null,
                    fData = fbData,
                    gadid = id,
                    activity = activity
                )
                tagOneSignal(
                    data1 = null,
                    data2 = fbData,
                    activity = activity
                )
                viewModelScope.launch {
                    repository.setterBoatModelRoomData(BoatModelRoom(data = data))
                    val room = repository.getterBoat()
                    val setRoom = room?.let { mapper.mapRmToFb(it) }
                    if (setRoom != null) {
                        repository.setterBoatModelFireData(
                            setRoom
                        )
                    }
                }
            }
        }
    }

    private fun buildLinkData(
        res: Resources,
        aData: MutableMap<String, Any>?,
        fData: String,
        gadid: String,
        activity: Activity
    ): String = DEFAULT_STRING.toUri().buildUpon().apply {
        appendQueryParameter(
            res.getString(R.string.secure_get_parametr),
            res.getString(R.string.secure_key)
        )
        appendQueryParameter(
            res.getString(R.string.dev_tmz_key),
            TimeZone.getDefault().id
        )
        appendQueryParameter(res.getString(R.string.gadid_key), gadid)
        appendQueryParameter(res.getString(R.string.deeplink_key), fData)
        appendQueryParameter(
            res.getString(R.string.source_key),
            when (fData) {
                "null" -> aData?.get("media_source").toString()
                else -> "deeplink"
            }
        )
        Log.d("media_source", aData?.get("media_source").toString())
        appendQueryParameter(
            res.getString(R.string.af_id_key), when (fData) {
                "null" -> {
                    AppsFlyerLib.getInstance().getAppsFlyerUID(activity)
                }
                else -> "null"
            }
        )
        appendQueryParameter(
            res.getString(R.string.adset_id_key),
            aData?.get("adset_id").toString()
        )
        appendQueryParameter(
            res.getString(R.string.campaign_id_key),
            aData?.get("campaign_id").toString()
        )
        appendQueryParameter(
            res.getString(R.string.app_campaign_key),
            aData?.get("campaign").toString()
        )
        appendQueryParameter(res.getString(R.string.adset_key), aData?.get("adset").toString())
        appendQueryParameter(res.getString(R.string.adgroup_key), aData?.get("adgroup").toString())
        appendQueryParameter(
            res.getString(R.string.orig_cost_key),
            aData?.get("orig_cost").toString()
        )
        appendQueryParameter(
            res.getString(R.string.af_siteid_key),
            aData?.get("af_siteid").toString()
        )
    }.toString()

    private fun tagOneSignal(data1: MutableMap<String, Any>?, data2: String, activity: Activity) {
        OneSignal.initWithContext(activity.application)
        OneSignal.setAppId(ONESIGNAL_KEY)

        val campaign = data1?.get("campaign").toString()
        val key = "key2"

        if (campaign == "null" && data2 == "null") {
            OneSignal.sendTag(key, "organic")
        } else if (data2 != "null") {
            OneSignal.sendTag(key, data2.replace("myapp://", "").substringBefore("/"))
        } else if (campaign != "null") {
            OneSignal.sendTag(key, campaign.substringBefore("_"))
        }
    }
    companion object {
        const val DEV_KEY = "4JxjqwbKPVxKaAK5XhQ4kT"
        const val ONESIGNAL_KEY = "a2f89b9c-f826-4fb8-a39d-6b220e474d60"
    }
}