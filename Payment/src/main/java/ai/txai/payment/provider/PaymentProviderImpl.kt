package ai.txai.payment.provider

import ai.txai.common.observer.CommonObserver
import ai.txai.common.router.ARouterConstants
import ai.txai.common.router.ARouterUtils
import ai.txai.common.router.bean.PayMethod
import ai.txai.common.router.provider.PaymentProvider
import ai.txai.common.router.provider.PaymentProvider.DefaultModelListener
import ai.txai.common.thread.TScheduler
import ai.txai.payment.activity.PaymentActivity
import ai.txai.payment.fragment.MethodListFragment
import ai.txai.payment.repository.PaymentApiRepository
import ai.txai.payment.response.PayMethodEntry
import ai.txai.payment.response.PayOrder
import ai.txai.payment.utils.BankUtils
import ai.txai.payment.utils.PaymentParameterField
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import com.alibaba.android.arouter.facade.annotation.Route
import io.reactivex.android.schedulers.AndroidSchedulers

/**
 * Time: 10/06/2022
 * Author Hay
 */
@Route(path = ARouterConstants.PATH_SERVICE_PAYMENT)
class PaymentProviderImpl : PaymentProvider {
    override fun preloadPaymentInfo() {
        Companion.preloadPaymentInfo()
    }

    override fun defaultPaymentMethod(orderId: String?, listener: DefaultModelListener?) {
        Companion.defaultPaymentMethod(orderId, listener)
    }

    override fun payNow(activity: Activity, fare: Double, orderId: String) {
        Companion.payNow(activity, fare, orderId)
    }


    override fun selectCard(activity: Activity, fare: Double, orderId: String) {
        Companion.selectCard(activity, fare, orderId)
    }

    override fun isPaying(orderId: String): Boolean {
        return Companion.isPaying(orderId)
    }

    override fun clearPaying(orderId: String?) {
        Companion.clearPaying(orderId)
    }

    override fun paymentStatus(orderId: String, listener: PaymentProvider.PayStatusListener?) {
        Companion.paymentStatus(orderId, listener)
    }

    companion object {
        var mapPaying = HashMap<String, Boolean>()
        var progressMethod: PayMethodEntry? = null
        var defaultMethod: PayMethodEntry? = null
        var refreshedMethod: Boolean = false
        fun preloadPaymentInfo() {
            mapPaying.clear()
            refreshedMethod = false
            defaultMethod = null
            progressMethod = null
            PaymentApiRepository.defaultPayMethod().subscribeOn(TScheduler.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : CommonObserver<PayMethodEntry>() {
                    override fun onSuccess(t: PayMethodEntry?) {
                        defaultMethod = t
                        refreshedMethod = true
                    }
                })
        }

        private fun payNow(activity: Activity, fare: Double, orderId: String, entry: PayMethodEntry?) {
            mapPaying.clear()
            val args = Bundle()
            args.putDouble(PaymentParameterField.EXTRA_AMOUNT, fare)
            args.putString(PaymentParameterField.EXTRA_ORDER_ID, orderId)
            args.putString(
                PaymentParameterField.EXTRA_CARD_TOKEN,
                entry?.payMethodInfo?.cardToken ?: ""
            )

            if (checkCard(activity, args, entry)) return

            when {
                BankUtils.isPayby(entry?.payMethod) -> {
                    args.putString(
                        PaymentParameterField.EXTRA_FRAGMENT_PATH,
                        ARouterConstants.PATH_FRAGMENT_PAYBY
                    )
                    ARouterUtils.navigation(activity, ARouterConstants.PATH_ACTIVITY_PAYMENT, args)
                }
                entry?.payMethodInfo != null -> {
                    args.putString(
                        PaymentParameterField.EXTRA_FRAGMENT_PATH,
                        PaymentActivity.METHOD_INPUT_CVV
                    )
                    ARouterUtils.navigation(activity, ARouterConstants.PATH_ACTIVITY_PAYMENT, args)
                }
                else -> {
                    args.putString(
                        PaymentParameterField.EXTRA_FRAGMENT_PATH,
                        ARouterConstants.PATH_FRAGMENT_PAYMENT_METHOD_LIST
                    )
                    ARouterUtils.navigation(activity, ARouterConstants.PATH_ACTIVITY_PAYMENT, args)
                }
            }
        }

        private fun checkCard(activity: Activity, args: Bundle, entry: PayMethodEntry?): Boolean {
            entry?.payMethodInfo?.let { method ->
                if (method.expired) {
                    args.putString(PaymentParameterField.EXTRA_FRAGMENT_PATH, PaymentActivity.METHOD_DELETE_CARD)
                    ARouterUtils.navigation(activity, ARouterConstants.PATH_ACTIVITY_PAYMENT, args)
                    return true
                }
            }
            return false
        }

        fun defaultPaymentMethod(orderId: String?, listener: DefaultModelListener?) {
            orderId?.let {
                if (isPaying(orderId)) {
                    when {
                        BankUtils.isPayby(progressMethod?.payMethod) -> {
                            listener?.result(
                                PayMethod.builder()
                                    .logoRes(BankUtils.bankLogoRes(progressMethod?.payMethod))
                                    .name(progressMethod?.payMethod)
                                    .isCard(false)
                                    .build()
                            )
                            return
                        }
                        progressMethod?.payMethodInfo != null -> {
                            listener?.result(
                                PayMethod.builder()
                                    .logoRes(BankUtils.bankLogoRes(progressMethod!!.payMethodInfo?.brand))
                                    .name(progressMethod!!.payMethodInfo?.last4)
                                    .isCard(true)
                                    .build()
                            )
                            return
                        }
                    }
                }
            }

            when {
                BankUtils.isPayby(defaultMethod?.payMethod) -> {
                    listener?.result(
                        PayMethod.builder()
                            .logoRes(BankUtils.bankLogoRes(defaultMethod?.payMethod))
                            .name(defaultMethod?.payMethod)
                            .isCard(false)
                            .build()
                    )
                }
                defaultMethod?.payMethodInfo != null -> {
                    listener?.result(
                        PayMethod.builder()
                            .logoRes(BankUtils.bankLogoRes(defaultMethod!!.payMethodInfo?.brand))
                            .name(defaultMethod!!.payMethodInfo?.last4)
                            .isCard(true)
                            .build()
                    )
                }
                refreshedMethod -> {
                    listener?.result(null)
                }
                else -> {
                    PaymentApiRepository.defaultPayMethod().subscribeOn(TScheduler.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(object : CommonObserver<PayMethodEntry>() {
                            override fun onSuccess(t: PayMethodEntry?) {
                                if (t == null || TextUtils.isEmpty(t.payMethod)) {
                                    listener?.result(null)
                                } else {
                                    defaultMethod = t
                                    defaultPaymentMethod(orderId, listener)
                                }
                            }
                        })
                }
            }

        }

        fun payNow(activity: Activity, fare: Double, orderId: String) {
            payNow(activity, fare, orderId, defaultMethod)
        }


        fun selectCard(activity: Activity, fare: Double, orderId: String) {
            mapPaying.clear()
            val args = Bundle()
            args.putDouble(PaymentParameterField.EXTRA_AMOUNT, fare)
            args.putString(PaymentParameterField.EXTRA_ORDER_ID, orderId)
            args.putString(
                MethodListFragment.EXTRA_DEFAULT_METHOD_KEY,
                MethodListFragment.formatKey(defaultMethod?.payMethodInfo)
            )
            args.putString(
                PaymentParameterField.EXTRA_FRAGMENT_PATH,
                ARouterConstants.PATH_FRAGMENT_PAYMENT_METHOD_LIST
            )

            ARouterUtils.navigation(activity, ARouterConstants.PATH_ACTIVITY_PAYMENT, args)
        }

        fun isPaying(orderId: String): Boolean {
            return mapPaying[orderId] == true
        }

        fun clearPaying(orderId: String?) {
            mapPaying.remove(orderId)
        }

        fun paymentStatus(orderId: String, listener: PaymentProvider.PayStatusListener?) {
            listener?.let {
                PaymentApiRepository.queryPayStatus(orderId)
                    .subscribeOn(TScheduler.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : CommonObserver<PayOrder>() {
                        override fun onSuccess(t: PayOrder?) {
                            listener.result(t?.toPayStatus())
                        }
                    })
            }
        }
    }

    override fun init(context: Context) {}
}