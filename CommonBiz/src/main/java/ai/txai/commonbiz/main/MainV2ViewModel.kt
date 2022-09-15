package ai.txai.commonbiz.main

import ai.txai.common.glide.GlideUtils
import ai.txai.common.mvvm.BaseViewModel
import ai.txai.common.observer.CommonObserver
import ai.txai.common.thread.TScheduler.io
import ai.txai.common.utils.AndroidUtils
import ai.txai.common.value.Result
import ai.txai.commonbiz.bean.AreaResponse
import ai.txai.commonbiz.bean.POIResponse
import ai.txai.commonbiz.data.BizData
import ai.txai.commonbiz.onetrip.OnStateChangeListener
import ai.txai.commonbiz.onetrip.SelfDrivingListener
import ai.txai.commonbiz.onetrip.SelfDrivingTaxi
import ai.txai.commonbiz.onetrip.TripModel
import ai.txai.commonbiz.onetrip.TripModel.getInstance
import ai.txai.commonbiz.repository.BizApiRepository.poiDefault
import ai.txai.database.enums.TripState
import ai.txai.database.order.Order
import ai.txai.database.router.Router
import ai.txai.database.site.Site
import ai.txai.database.user.User
import ai.txai.database.utils.CommonData
import ai.txai.database.vehicle.VehicleIndicator
import ai.txai.mapsdk.utils.MapBoxUtils
import ai.txai.push.listener.PushStateListener
import ai.txai.push.payload.notify.DispatchStatusNotify
import ai.txai.push.payload.notify.DispatchWaitingNotify
import ai.txai.push.payload.notify.OrderStatusNotify
import android.app.Application
import android.os.Bundle
import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import com.mapbox.geojson.Point
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import io.reactivex.android.schedulers.AndroidSchedulers

/**
 * Time: 2/21/22
 * Author Hay
 */
class MainV2ViewModel(application: Application) : BaseViewModel(application),
    OnIndicatorPositionChangedListener, OnStateChangeListener, PushStateListener,
    SelfDrivingListener {
    private val tripModel: TripModel = getInstance()

    val hasInProgressOrder = MutableLiveData<Boolean>()

    val userBean = MutableLiveData<User>()

    val selectedSite = MutableLiveData<Pair<Boolean, Site?>?>()
    val availableVehicle = MutableLiveData<List<VehicleIndicator>>()
    val areaPoints = MutableLiveData<MutableList<MutableList<ai.txai.database.location.Point>>>()
    val allSites = MutableLiveData<List<Site>>()
    val areas = MutableLiveData<List<AreaResponse>>()

    var vehicleIndicator: VehicleIndicator? = null

    var dropOffSite: Site? = null

    val pushState = MutableLiveData<Int>()

    /**
     * 当前位置
     */
    var currentPoint: Point? = null

    /**
     * 是否授权位置权限
     */
    var grantPermission: Boolean? = null

    /**
     * 是否超过3s获取默认位置
     */
    var hasGetDefaultSite: Boolean? = null

    val defaultRunnable = Runnable {
        hasGetDefaultSite = true
        if (grantPermission == true) {
            defaultLocation()
        } else {
            triggerDefaultPickup()
        }
        
        tripModel.refreshOrder(true)
    }

    override fun onCreate(args: Bundle?) {
        tripModel.registerOnStateChangeListener(this)
        tripModel.registerPushStateListener(this)
        SelfDrivingTaxi.getInstance().registerSelfDrivingListener(this)
        refreshPOI()
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

    override fun onResume() {
        super.onResume()
        hasInProgressOrder.postValue(tripModel.tripStateMachine.inProgress())
    }

    override fun onDestroy() {
        super.onDestroy()
        tripModel.unregisterOnStateChangeListener(this)
        tripModel.unregisterPushStateListener(this)
        SelfDrivingTaxi.getInstance().unregisterSelfDrivingListener(this)
    }

    override fun onStart() {
        super.onStart()
        userBean.postValue(CommonData.getInstance().currentUser())
    }

    fun refreshVehicle() {
        SelfDrivingTaxi.getInstance().requestAvailableVehicles()
    }

    fun foreDefaultLocation() {
        if (currentPoint != null) {
            selectedSite.value = null
            defaultLocation()
        }
    }

    fun defaultLocation() {
        if (hasGetDefaultSite == false) {
            return
        }
        if (selectedSite.value != null || allSites.value == null || grantPermission == null) {
            return
        }
        if (grantPermission!! && currentPoint != null) {
            val pickUpSite = MapBoxUtils.nearlySite(currentPoint, allSites.value)
            dropOffSite = MapBoxUtils.nearlySite(pickUpSite, allSites.value)
            selectedSite.postValue(Pair(true, pickUpSite))
            return
        }

        triggerDefaultPickup()
    }

    private fun triggerDefaultPickup() {
        poiDefault().subscribeOn(io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : CommonObserver<POIResponse>() {
                override fun onSuccess(t: POIResponse?) {
                    super.onSuccess(t)
                    if (t != null && hasGetDefaultSite == true && selectedSite.value == null) {
                        val pickUpSite = t.toSite()
                        dropOffSite = MapBoxUtils.nearlySite(pickUpSite, allSites.value)
                        selectedSite.postValue(Pair(false, pickUpSite))
                    }
                }

                override fun onFailed(msg: String?) {
                    super.onFailed(msg)
                    msg?.let { showToast(it, false) }
                }
            })
    }


    override fun onIndicatorPositionChanged(point: Point) {
        currentPoint = point

        tripModel.currentPoint = point
    }

    override fun onQueueChange(notify: DispatchWaitingNotify?) {
    }

    override fun onDispatchStatusChange(
        state: TripState?,
        notify: DispatchStatusNotify?,
        order: Order?
    ) {
    }

    override fun onRouteChange(router: Order?) {
    }

    override fun onVehicleChange(notify: VehicleIndicator?, router: Router?) {
    }

    override fun onStateChange(ts: TripState) {
        hasInProgressOrder.postValue(tripModel.tripStateMachine.inProgress())
    }

    override fun onOrderStatusChange(orderStatusNotify: OrderStatusNotify?) {
    }

    override fun onStateChanged(from: Int, to: Int) {
        pushState.postValue(to)
    }

    override fun loadedAreas(result: Result<MutableList<AreaResponse>>) {
        if (result.isComplete) {
            if (!TextUtils.isEmpty(result.msg)) {
                showToast(result.msg, false)
            } else {
                areas.postValue(result.data)
                val arrayList = mutableListOf<MutableList<ai.txai.database.location.Point>>()
                result.data.forEach {
                    arrayList.add(it.getBoundary())
                    GlideUtils.preloadImage(AndroidUtils.getApplicationContext(), it.picture)
                }
                areaPoints.postValue(arrayList)
            }
        }
    }

    override fun loadedVehicles(result: Result<MutableList<VehicleIndicator>>) {
        if (result.isComplete) {
            if (!TextUtils.isEmpty(result.msg)) {
                showToast(result.msg, false)
            } else {
                availableVehicle.postValue(result.data)
            }
        }
    }
}