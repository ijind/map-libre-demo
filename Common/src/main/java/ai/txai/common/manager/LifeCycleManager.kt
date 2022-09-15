package ai.txai.common.manager

import ai.txai.common.base.BaseActivity
import ai.txai.common.external.LoginManager
import ai.txai.common.router.ARouterConstants
import ai.txai.common.router.ARouterUtils
import ai.txai.common.utils.ToastUtils
import ai.txai.database.utils.CommonData
import android.app.Activity
import android.content.Intent
import android.os.Bundle

object LifeCycleManager {
    private var lastTime = 0L
    fun getCurActivity(): Activity? = BaseActivity.getLastActivity()

    fun finishAllActivities() = BaseActivity.finishAllActivities()

    fun accountLogout() {
        accountLogout(Bundle(), "")
    }

    fun accountLogout(message: String) {
        accountLogout(Bundle(), message)
    }

    fun accountLogout(params: Bundle) {
        accountLogout(params, "")
    }

    fun accountLogout(params: Bundle, toastMessage: String) {
        if (System.currentTimeMillis() - lastTime < 500) {
            return
        }

        //TODO add loading
        LoginManager.getInstance().whenLogout()

        val path = ARouterConstants.PATH_ACTIVITY_LOGIN
        val flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        getCurActivity()?.let { ARouterUtils.navigation(it, flags, path, params) }
        if (toastMessage.isNotEmpty()) {
            ToastUtils.show(toastMessage)
        }
        lastTime = System.currentTimeMillis()
    }
}