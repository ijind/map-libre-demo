package ai.txai.commonbiz.viewmodel

import ai.txai.commonbiz.data.BizData
import ai.txai.commonbiz.main.TripDetailsViewModel
import ai.txai.database.location.Point
import ai.txai.database.site.Site
import ai.txai.mapsdk.utils.MapBoxUtils
import android.app.Application
import androidx.lifecycle.MutableLiveData

/**
 * Time: 31/03/2022
 * Author Hay
 */
open class SearchInMapViewModel(application: Application) : TripDetailsViewModel(application) {
    val mapSelectSite = MutableLiveData<Site>()
    val allSites = MutableLiveData<List<Site>>()
    override fun onResume() {
        super.onResume()
        refreshPOI()
    }

    companion object {
        private const val TAG = "SearchInMapViewModel"
    }

    fun defaultLocation(point: Point?) {
        if (allSites.value == null || point == null) {
            return
        }
        val site = MapBoxUtils.nearlySite(
            com.mapbox.geojson.Point.fromLngLat(
                point.longitude,
                point.latitude
            ), allSites.value
        )
        mapSelectSite.postValue(site)
        return
    }

    private fun refreshPOI() {
        BizData.getInstance().requestSites(object : BizData.SiteChangeListener {
            override fun onLoaded(vehicle: MutableList<Site>) {
                allSites.postValue(vehicle)
            }

            override fun onFailed(msg: String) {
                showToast(msg, false)
            }
        })
    }
}