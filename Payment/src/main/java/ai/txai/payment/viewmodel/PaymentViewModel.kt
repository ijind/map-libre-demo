package ai.txai.payment.viewmodel

import ai.txai.common.base.BaseActivity
import ai.txai.common.dialog.DialogCreator
import ai.txai.common.mvvm.BaseViewModel
import ai.txai.common.observer.CommonObserver
import ai.txai.common.router.ARouterConstants
import ai.txai.common.router.ProviderManager
import ai.txai.common.router.provider.PaymentProvider
import ai.txai.common.thread.TScheduler
import ai.txai.payment.provider.PaymentProviderImpl
import ai.txai.payment.repository.PaymentApiRepository
import ai.txai.payment.request.BankCardTokenBean
import ai.txai.payment.request.PayRequestBean
import ai.txai.payment.response.PayMethodEntry
import ai.txai.payment.response.PayMethodInfo
import ai.txai.payment.response.PayMethodList
import ai.txai.payment.response.PayOrder
import android.app.Application
import android.os.Bundle
import android.util.Pair
import androidx.lifecycle.MutableLiveData
import com.blankj.utilcode.util.ThreadUtils
import io.reactivex.android.schedulers.AndroidSchedulers

/**
 * Time: 13/06/2022
 * Author Hay
 */
open class PaymentViewModel(application: Application) : BaseViewModel(application) {
    var fragment = MutableLiveData<Pair<String, Bundle>>()
    var payOrderLive = MutableLiveData<PayOrder?>()
    var cardMethods = MutableLiveData<PayMethodList>()
    var removeCard = MutableLiveData<PayMethodInfo>()
    override fun onCreate(args: Bundle) {
        super.onCreate(args)
    }

    fun methodList() {
        showLoading("")
        PaymentApiRepository.payMethodList().subscribeOn(TScheduler.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : CommonObserver<PayMethodList>() {
                override fun onSuccess(t: PayMethodList?) {
                    super.onSuccess(t)
                    if (t?.list?.isEmpty() == false) {
                        cardMethods.postValue(t)
                    }
                    hideLoading()
                }

                override fun onFailed(msg: String?) {
                    super.onFailed(msg)
                    hideLoading()
                    showToast(msg, false)
                }
            })
    }


    fun confirmPay(payBean: PayRequestBean, last4: String? = null, brand: String? = null) {
        showLoading("")
        PaymentApiRepository.startPay(payBean).subscribeOn(TScheduler.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : CommonObserver<PayOrder>() {
                override fun onSuccess(t: PayOrder?) {
                    super.onSuccess(t)
                    payOrderLive.postValue(t)
                    val payEntry = PayMethodEntry()
                    payEntry.payMethod = payBean.payMethod
                    last4?.let {
                        payEntry.payMethodInfo = PayMethodInfo()
                        payEntry.payMethodInfo.last4 = last4
                        payEntry.payMethodInfo.brand = brand
                    }
                    PaymentProviderImpl.progressMethod = payEntry
                    hideLoading()
                }

                override fun filterFailed(status: Int, msg: String): Boolean {
                    hideLoading()
                    if (status == 11304002) {
                        DialogCreator.showConfirmDialog(
                            BaseActivity.getLastActivity(),
                            msg
                        ) {
                            if (payBean.payMethodInfo is BankCardTokenBean) {
                                val methodInfo = PayMethodInfo()
                                methodInfo.cardToken =
                                    (payBean.payMethodInfo as BankCardTokenBean).cardToken
                                deleteCard(methodInfo)
                            }
                        }
                        return true
                    }
                    return false
                }

                override fun onFailed(msg: String?) {
                    super.onFailed(msg)
                    hideLoading()
                    showToast(msg, false)
                }
            })
    }

    fun deleteCard(methodInfo: PayMethodInfo) {
        showLoading("")
        PaymentApiRepository.removePayMethod(methodInfo.cardToken).subscribeOn(TScheduler.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : CommonObserver<String>() {
                override fun onSuccess(t: String?) {
                    super.onSuccess(t)
                    removeCard.postValue(methodInfo)
                    PaymentProviderImpl.preloadPaymentInfo()
                    hideLoading()
                }

                override fun onFailed(msg: String?) {
                    super.onFailed(msg)
                    hideLoading()
                    showToast(msg, false)
                    ThreadUtils.runOnUiThreadDelayed({ removeCard.postValue(null) }, 2000)
                }
            })
    }
}