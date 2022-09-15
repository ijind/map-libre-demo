package com.gomap.robotaxi.login

import ai.txai.common.dialog.DialogCreator
import ai.txai.common.json.GsonManager
import ai.txai.common.log.LOG
import ai.txai.common.manager.LifeCycleManager
import ai.txai.common.utils.AndroidUtils
import ai.txai.push.common.NotifyClassifyEnum
import ai.txai.push.listener.PushNotifyListener
import ai.txai.push.payload.PushNotifyPayload
import ai.txai.push.payload.notify.LoginStatusNotify
import com.gomap.robotaxi.push.PushManager

object GlobalLoginStatusListener: PushNotifyListener {
    private const val TAG = "GlobalLoginStatusListener"
    private const val USER_SSO_LOGIN = "user.ssoLogin"
    private const val USER_DISABLE = "user.statusDisabled"

    override fun onReceived(pushNotifyPayload: PushNotifyPayload?) {
        LOG.d(TAG, " login status onReceived")
        if (pushNotifyPayload == null) return

        val gson = GsonManager.getGson()
        val jsonStr = pushNotifyPayload.messageBody.toString()
        val notifyClassify = pushNotifyPayload.notifyClassify
        if (notifyClassify == NotifyClassifyEnum.LoginStatusNotify) {
            val loginNotify = gson.fromJson(jsonStr, LoginStatusNotify::class.java)
            AndroidUtils.delayOperation(0) {
                operationByLoginStatus(loginNotify)
            }
        }
    }

    private fun operationByLoginStatus(notify: LoginStatusNotify) {
        LOG.d(TAG, " login status ${notify.memo}")
        when(notify.memo) {
            USER_DISABLE -> {
                val message = "You have been banned, please contact the customer service to file an appeal"
                showConfirmDialog(message)
            }
            USER_SSO_LOGIN -> {
                val message = "Your account has been logged in on another device."
                showConfirmDialog(message)
            }
        }
    }

    private fun showConfirmDialog(message: String) {
        LifeCycleManager.getCurActivity()?.let {
            DialogCreator.showConfirmDialog(it, message) {
                LifeCycleManager.accountLogout()
            }
        }
    }
}