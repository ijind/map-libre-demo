package ai.txai.common.utils

import ai.txai.common.R
import ai.txai.common.dialog.DialogCreator
import ai.txai.common.manager.LifeCycleManager
import ai.txai.common.network.NetUtils
import ai.txai.common.network.NetworkObserver
import com.blankj.utilcode.util.ThreadUtils
import java.net.UnknownHostException

object ErrorUtils {
    private const val AUTH_FAILED = 401
    private const val TOKEN_VERIFY_FAILED = 10204005
    private const val DISABLE_USER = 10104004
    private const val TOKEN_NOT_FOUNT = 10203007

    fun filterErrors(exception: Throwable): Boolean {
        var message = ""
        when (exception) {
            is UnknownHostException -> {
                if (!NetUtils.isInternetEnable()) {
                    val curActivity = LifeCycleManager.getCurActivity()
                    curActivity?.let { (it as NetworkObserver).onDisconnected() }
                    return true
                }
            }
        }
        if (message == "") return false

        ToastUtils.show(message)
        return true
    }

    fun filterErrors(errorCode: Int): Boolean {
        return filterErrors(errorCode, "")
    }

    fun filterErrors(errorCode: Int, message: String): Boolean {
        var isHandle = true
        when (errorCode) {
            TOKEN_NOT_FOUNT, TOKEN_VERIFY_FAILED, AUTH_FAILED -> {
                runOperationOnMain { LifeCycleManager.accountLogout() }
                isHandle = false
            }
            DISABLE_USER -> {
                runOperationOnMain {
                    LifeCycleManager.getCurActivity()?.let {
                        DialogCreator.showConfirmDialog(it, message, null).showPop()
                    }
                }
            }
            else -> isHandle = false
        }
        return isHandle
    }

    private fun runOperationOnMain(operation: () -> Unit) {
        ThreadUtils.runOnUiThread { operation.invoke() }
    }
}