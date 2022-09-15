package ai.txai.commonbiz.viewmodel

import ai.txai.common.router.ARouterConstants
import ai.txai.common.router.ProviderManager
import ai.txai.common.router.bean.PayMethod
import ai.txai.common.router.bean.PayStatus
import ai.txai.common.router.provider.PaymentProvider
import ai.txai.commonbiz.main.TripDetailsViewModel
import ai.txai.database.order.Order
import android.app.Application
import android.os.Bundle
import android.text.TextUtils
import androidx.lifecycle.MutableLiveData

/**
 * Time: 31/03/2022
 * Author Hay
 */
class FinishViewModel(application: Application) : TripDetailsViewModel(application) {
    var orderDetail = MutableLiveData<Order>()

    var method = MutableLiveData<PayMethod?>()

    override fun onCreate(args: Bundle?) {
        super.onCreate(args)
        order?.let { orderDetail.postValue(order) }
    }

    override fun onResume() {
        super.onResume()
        requestDefaultMethod()
    }

    private fun requestDefaultMethod() {
        val paymentProvider =
            ProviderManager.provide(ARouterConstants.PATH_SERVICE_PAYMENT) as PaymentProvider?

        paymentProvider?.defaultPaymentMethod(orderId) { method.postValue(it) }
    }

    fun requestPayStatus() {
        tripModel.requestPayStatus(orderId)
    }

    fun hasDefaultMethod(): Boolean {
        val value = method.value
        return value != null
    }
}