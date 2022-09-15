package com.gomap.robotaxi

import ai.txai.common.base.BaseActivity
import ai.txai.common.permission.checker.StandardChecker
import ai.txai.common.router.ARouterConstants
import ai.txai.common.router.ARouterUtils
import ai.txai.common.scheme.SchemeDispatchManager
import ai.txai.common.thread.Dispatcher
import ai.txai.commonbiz.main.MainV2Activity
import ai.txai.database.GreenDaoHelper
import ai.txai.database.utils.CommonData
import android.annotation.SuppressLint
import android.os.Bundle
import com.blankj.utilcode.util.StringUtils
import com.gyf.immersionbar.ImmersionBar
import kotlinx.coroutines.*

/**
 * Time: 2/23/22
 * Author Hay
 */
@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        window.setBackgroundDrawable(null)
        super.onCreate(savedInstanceState)
        ImmersionBar.with(this)
            .transparentStatusBar()
            .statusBarColor(android.R.color.transparent)
            .statusBarAlpha(0.0f)
            .statusBarDarkFont(false)
            .init()
        setContentView(R.layout.activity_splash)
        SchemeDispatchManager.getInstance().setUri(intent?.data, intent)
        init()
    }

    private fun init() {
        CoroutineScope(Dispatcher.IO).launch {
            val user = CommonData.getInstance().currentUser()
            var activityPath = ARouterConstants.PATH_ACTIVITY_LOGIN
            if (user != null && !StringUtils.isEmpty(user.uid)) {
                GreenDaoHelper.getInstance().waitUserDb()
                activityPath = ARouterConstants.PATH_ACTIVITY_V2_MAIN
            }
            withContext(Dispatchers.Main) {
                if (containActivity(MainV2Activity::class.java)) {
                    if (SchemeDispatchManager.getInstance().dispatch(this@SplashActivity)) {
                        finish()
                        return@withContext
                    }
                }
                ARouterUtils.navigation(this@SplashActivity, activityPath, Bundle())
                finish()
            }
        }
    }

    override fun onBackPressed() {}
}