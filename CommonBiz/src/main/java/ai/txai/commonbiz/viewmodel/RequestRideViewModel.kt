package ai.txai.commonbiz.viewmodel

import ai.txai.common.base.BaseActivity
import ai.txai.common.dialog.DialogCreator
import ai.txai.common.json.GsonManager
import ai.txai.common.observer.CommonObserver
import ai.txai.common.router.ARouterConstants
import ai.txai.common.router.ARouterUtils
import ai.txai.common.thread.Dispatcher
import ai.txai.common.thread.TScheduler
import ai.txai.common.utils.AndroidUtils
import ai.txai.commonbiz.R
import ai.txai.commonbiz.bean.OrderResponse
import ai.txai.commonbiz.bean.TripPlanResponse
import ai.txai.commonbiz.data.BizData
import ai.txai.commonbiz.main.TripDetailsViewModel
import ai.txai.commonbiz.repository.BizApiRepository
import ai.txai.database.site.Site
import ai.txai.database.vehicle.VehicleModel
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.blankj.utilcode.util.ThreadUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Time: 31/03/2022
 * Author Hay
 */
class RequestRideViewModel(application: Application) : TripDetailsViewModel(application) {
    val vModels = MutableLiveData<List<VehicleModel>>()
    val mapTrips = mutableMapOf<String, TripPlanResponse>()
    val triPlaResponses = MutableLiveData<MutableMap<String, TripPlanResponse>>()
    val orderResponse = MutableLiveData<OrderResponse>()
    var pickUpSite: Site? = null
    var dropOffSite: Site? = null
    var vModel: VehicleModel? = null
    override fun onCreate(args: Bundle?) {
        super.onCreate(args)
        val strPick = args!!.getString(EXTRA_PICK_UP_SITE)
        val strDrop = args.getString(EXTRA_DROP_OFF_SITE)
        pickUpSite = GsonManager.fromJsonObject(strPick, Site::class.java)
        dropOffSite = GsonManager.fromJsonObject(strDrop, Site::class.java)
    }

    companion object {
        const val EXTRA_PICK_UP_SITE = "extra.pick.up.site"
        const val EXTRA_DROP_OFF_SITE = "extra.drop.off.site"
    }

    override fun onCreate() {
        super.onCreate()
        BizData.getInstance().requestVehicleModel { list ->
            ThreadUtils.runOnUiThread {
                if (list == null) {
                    DialogCreator.showConfirmDialog(
                        BaseActivity.getLastActivity(),
                        AndroidUtils.getApplicationContext()
                            .getString(R.string.biz_no_trip_available_vehicle)
                    ) {
                        ARouterUtils.navigation(
                            BaseActivity.getLastActivity(),
                            ARouterConstants.PATH_ACTIVITY_V2_MAIN
                        )
                    }
                } else {
                    vModels.postValue(list)
                }
            }
        }
    }

    fun requestTripPlan() {
        CoroutineScope(Dispatcher.IO).launch {
            mapTrips.clear()
            var countDownLatch = CountDownLatch(vModels.value?.size ?: 0)
            vModels.value?.forEach {
                BizApiRepository.tripPlan(pickUpSite!!, dropOffSite!!, it.id.toString())
                    .subscribeOn(TScheduler.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : CommonObserver<TripPlanResponse>() {
                        override fun onSuccess(t: TripPlanResponse?) {
                            if (t != null) {
                                mapTrips[t.vehicleTypeId] = t
                            }
                            countDownLatch.countDown()
                        }

                        override fun onFailed(msg: String?) {
                            super.onFailed(msg)
                            countDownLatch.countDown()
                        }
                    })
            }
            countDownLatch.await(1, TimeUnit.MINUTES)
            triPlaResponses.postValue(mapTrips)
        }
    }

    fun confirmOrder() {
        if (vModel == null) {
            return
        }
        showLoading("")
        tripModel.startTrip(
            pickUpSite!!,
            dropOffSite!!,
            vModel!!,
            object : CommonObserver<OrderResponse>() {
                override fun onSuccess(t: OrderResponse?) {
                    if (t != null) {
                        orderResponse.postValue(t)
                    }
                    hideLoading()
                }

                override fun onFailed(msg: String?) {
                    hideLoading()
                    ThreadUtils.runOnUiThread {
                        DialogCreator.showConfirmDialog(
                            BaseActivity.getLastActivity(), msg
                        ) {
                            ARouterUtils.navigation(
                                BaseActivity.getLastActivity(),
                                ARouterConstants.PATH_ACTIVITY_V2_MAIN
                            )
                        }
                    }
                }
            })
    }
}