package ai.txai.commonbiz.viewmodel

import ai.txai.common.observer.CommonObserver
import ai.txai.common.thread.TScheduler
import ai.txai.commonbiz.bean.OrderDetailResponse
import ai.txai.commonbiz.bean.WaitingQueueBean
import ai.txai.commonbiz.main.TripDetailsViewModel
import ai.txai.commonbiz.repository.BizApiRepository
import ai.txai.push.payload.notify.DispatchWaitingNotify
import android.app.Application
import androidx.lifecycle.MutableLiveData
import io.reactivex.android.schedulers.AndroidSchedulers

/**
 * Time: 31/03/2022
 * Author Hay
 */
class PendingViewModel(application: Application) : TripDetailsViewModel(application) {
    val waitingRefresh = MutableLiveData<WaitingQueueBean>()
    fun requestWaitTime() {
        BizApiRepository.detailOrder(tripModel.tripStateMachine.order.id)
            .subscribeOn(TScheduler.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : CommonObserver<OrderDetailResponse>() {
                override fun onSuccess(t: OrderDetailResponse?) {
                    super.onSuccess(t)
                    waitingRefresh.postValue(t?.waitingQueueBean)
                }
            })
    }

}