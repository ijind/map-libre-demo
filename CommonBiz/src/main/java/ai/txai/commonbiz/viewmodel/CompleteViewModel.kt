package ai.txai.commonbiz.viewmodel

import ai.txai.common.observer.CommonObserver
import ai.txai.common.thread.Dispatcher
import ai.txai.commonbiz.bean.OrderDetailResponse
import ai.txai.commonbiz.data.BizData
import ai.txai.commonbiz.main.TripDetailsActivity
import ai.txai.commonbiz.main.TripDetailsViewModel
import ai.txai.commonbiz.repository.BizApiRepository
import ai.txai.database.order.Order
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Time: 31/03/2022
 * Author Hay
 */
class CompleteViewModel(application: Application) : TripDetailsViewModel(application) {

    var orderDetail = MutableLiveData<Order>()

    override fun onCreate(args: Bundle?) {
        super.onCreate(args)
        val orderId = args?.getString(TripDetailsActivity.EXTRA_ORDER_ID)
        requestDetails(orderId)
    }


    private fun requestDetails(orderId: String?) {
        CoroutineScope(Dispatcher.IO).launch {
            val order = BizData.getInstance().getOrder(orderId)
            if (order != null) {
                requestVehicleDetails(order.vehicleNo)
                orderDetail.postValue(order)
            } else {
                BizApiRepository.detailOrder(orderId!!)
                    .subscribe(object : CommonObserver<OrderDetailResponse>() {
                        override fun onSuccess(orderDetailResponse: OrderDetailResponse?) {
                            super.onSuccess(orderDetailResponse)
                            val order1 = orderDetailResponse?.toOrder()
                            if (order1 != null) {
                                requestVehicleDetails(order1?.vehicleNo)
                                BizData.getInstance().saveOrder(order1)
                                orderDetail.postValue(order1)
                            }
                        }
                    })
            }
        }
    }

}