package ai.txai.common.observer

import ai.txai.common.json.GsonManager
import ai.txai.common.log.LOG
import ai.txai.common.response.CommonResponse
import ai.txai.common.utils.ErrorUtils
import android.text.TextUtils
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

abstract class CommonObserver<T>: Observer<CommonResponse<T>> {
    companion object {
        private const val TAG = "CommonObserver"
        private const val SUCCESS = 200
    }

    private var disposable: Disposable? = null

    override fun onError(e: Throwable) {
        LOG.d(TAG,"onError ${e.stackTraceToString()}")
        disposable?.let {
            if (it.isDisposed) it.dispose()
        }
        if (!ErrorUtils.filterErrors(e)) {
            onFailed(e.toString())
        }
    }

    override fun onNext(response: CommonResponse<T>) {
        val status = response.getResultHeader()?.getStatus() ?: -1
        val msg = response.getResultHeader()?.getMsg() ?: ""
        if (status == SUCCESS) {
            val gsonString = GsonManager.GsonString(response.getResult())
            if (TextUtils.equals("{}", gsonString)) {
                onSuccess(null)
            } else {
                onSuccess(response.getResult())
            }
        } else {
            onFailed(status, msg)
        }
    }

    override fun onSubscribe(d: Disposable) {
        this.disposable = d
    }

    override fun onComplete() {
        disposable?.let {
            if (it.isDisposed) it.dispose()
        }
    }

    private fun onFailed(status: Int, msg: String) {
        //此处可能会有账号过期处理
        if (ErrorUtils.filterErrors(status, msg)) {
            return
        }

        if (filterFailed(status, msg)) {
            return
        }

        onFailed(msg)
    }

    open fun onSuccess(t: T?) {
    }

    open fun onFailed(msg: String?) {
        disposable?.let {
            if (it.isDisposed) it.dispose()
        }
    }

    open fun filterFailed(status: Int, msg: String): Boolean {
        return false
    }
}