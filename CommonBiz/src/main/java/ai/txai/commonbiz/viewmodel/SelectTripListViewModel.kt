package ai.txai.commonbiz.viewmodel

import ai.txai.common.base.BaseListViewModel
import ai.txai.common.observer.CommonObserver
import ai.txai.common.thread.Dispatcher
import ai.txai.common.thread.TScheduler
import ai.txai.commonbiz.bean.OrderIntroResponse
import ai.txai.commonbiz.data.BizData
import ai.txai.commonbiz.repository.BizApiRepository
import ai.txai.database.order.Order
import ai.txai.database.vehicle.VehicleModel
import ai.txai.push.payload.eunms.OrderState
import android.app.Application
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Time: 15/03/2022
 * Author Hay
 */
class SelectTripListViewModel(application: Application) :
    BaseListViewModel<Order, Long>(application) {
    override fun loadData(endTime: Long?, forMore: Boolean) {
        showLoading("")
        BizApiRepository.listOrder(endTime ?: System.currentTimeMillis(), 20, OrderState.Completed.name)
            .subscribeOn(TScheduler.io())
            .subscribe(object : CommonObserver<OrderIntroResponse>() {
                override fun onSuccess(t: OrderIntroResponse?) {
                    list = mutableListOf()
                    if (t != null) {
                        list.addAll(t.toOrderList())
                    }
                    if (forMore) {
                        loadMore.postValue(true)
                    } else {
                        refresh.postValue(true)
                    }
                    if (list.size > 0) {
                        flag = list[list.size - 1].createTime
                    }
                    hasMore.postValue(t?.more ?: false)
                    hideLoading()
                }

                override fun onFailed(msg: String?) {
                    super.onFailed(msg)
                    hideLoading()
                }
            })
    }

}
