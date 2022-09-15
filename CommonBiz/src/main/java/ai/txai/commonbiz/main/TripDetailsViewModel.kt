package ai.txai.commonbiz.main

import ai.txai.common.base.BaseActivity
import ai.txai.common.log.LOG
import ai.txai.common.mvvm.BaseViewModel
import ai.txai.common.observer.CommonObserver
import ai.txai.common.router.ARouterConstants
import ai.txai.common.router.ARouterUtils
import ai.txai.commonbiz.bean.CancelOrderResponse
import ai.txai.commonbiz.data.BizData
import ai.txai.commonbiz.onetrip.OnStateChangeListener
import ai.txai.commonbiz.onetrip.TripModel
import ai.txai.commonbiz.onetrip.TripModel.getInstance
import ai.txai.database.enums.TripState
import ai.txai.database.order.Order
import ai.txai.database.router.Router
import ai.txai.database.vehicle.Vehicle
import ai.txai.database.vehicle.VehicleIndicator
import ai.txai.push.payload.notify.DispatchStatusNotify
import ai.txai.push.payload.notify.DispatchWaitingNotify
import ai.txai.push.payload.notify.OrderStatusNotify
import android.app.Application
import android.os.Bundle
import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import com.blankj.utilcode.util.ThreadUtils

/**
 * Time: 2/21/22
 * Author Hay
 */
abstract class TripDetailsViewModel(application: Application) : BaseViewModel(application),
    OnStateChangeListener {
    companion object {
        private const val TAG = "MainViewModel"
    }

    val tripModel: TripModel = getInstance()

    val queueNotify = MutableLiveData<DispatchWaitingNotify>()
    val vehicleNotify = MutableLiveData<Pair<VehicleIndicator?, Router?>>()
    val vehicleDetail = MutableLiveData<Vehicle>()
    protected var orderId: String? = null
    protected var order: Order? = null

    override fun onCreate(args: Bundle?) {
        super.onCreate(args)
        tripModel.registerOnStateChangeListener(this)
        orderId = args?.getString(TripDetailsActivity.EXTRA_ORDER_ID)
        order = tripModel.tripStateMachine.order
        if (order == null) {
            order = BizData.getInstance().getOrder(orderId)
        }

        val vehicle = tripModel.tripStateMachine.vehicle
        if (vehicle != null) {
            vehicleNotify.postValue(Pair(vehicle, null))
        } else {
            order?.let {
                BizData.getInstance().requestSite({ site ->
                    val pickUpSite = site[0]
                    if (pickUpSite != null) {
                        val tmpVehicle = VehicleIndicator(it.vehicleNo)
                        tmpVehicle.setPoint(pickUpSite.point)
                        vehicleNotify.postValue(Pair(tmpVehicle, null))
                    }
                }, order!!.pickUpId)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        tripModel.unregisterOnStateChangeListener(this)
    }

    fun requestVehicleDetails(id: String?) {
        LOG.i(TAG, "requestVehicleDetails %s", id)
        if (TextUtils.isEmpty(id)) {
            return
        }
        BizData.getInstance().requestVehicleDetails(id) {
            vehicleDetail.postValue(it)
        }
    }

    fun cancelOrder() {
        if (orderId == null) {
            return
        }
        showLoading("")
        orderId?.let {
            tripModel.cancelTrip(object : CommonObserver<CancelOrderResponse>() {
                override fun onSuccess(t: CancelOrderResponse?) {
                    hideLoading()
                    canceledOrder(it)
                }

                override fun onFailed(msg: String?) {
                    hideLoading()
                    canceledOrder(it)
                }
            })
        }
    }

    override fun onQueueChange(notify: DispatchWaitingNotify?) {
        LOG.d(TAG, "onQueueChange")
        queueNotify.postValue(notify)
    }

    override fun onDispatchStatusChange(
        tripState: TripState,
        notify: DispatchStatusNotify,
        order: Order
    ) {
        LOG.d(TAG, "onDispatchStatusChange")
    }

    override fun onRouteChange(router: Order) {
        LOG.d(TAG, "onRouteChange")
    }

    override fun onVehicleChange(vehicle: VehicleIndicator?, router: Router?) {
        vehicleNotify.postValue(Pair(vehicle, router))
    }

    override fun onStateChange(tripS: TripState) {
    }

    override fun onOrderStatusChange(notify: OrderStatusNotify?) {
        LOG.d(TAG, "onCancelOrder")
    }

    fun canceledOrder(orderId: String) {
        ThreadUtils.runOnUiThread {
            val args = Bundle()
            args.putString(CancelActivity.EXTRA_ORDER_ID, orderId)
            ARouterUtils.navigation(
                BaseActivity.getLastActivity(),
                ARouterConstants.PATH_ACTIVITY_CANCEL,
                args
            )
        }
    }
}