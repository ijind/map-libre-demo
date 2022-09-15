package ai.txai.commonbiz.view

import ai.txai.common.glide.GlideUtils.loadImage
import ai.txai.common.glide.ImageOptions
import ai.txai.common.router.ARouterConstants
import ai.txai.common.router.ARouterUtils
import ai.txai.common.utils.AndroidUtils
import ai.txai.commonbiz.R
import ai.txai.commonbiz.databinding.BizPersonalCenterLayoutBinding
import ai.txai.commonbiz.main.MainV2Activity
import ai.txai.database.user.User
import ai.txai.database.utils.CommonData
import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.gyf.immersionbar.ImmersionBar
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.DrawerPopupView
import com.lxj.xpopup.enums.PopupPosition
import java.lang.ref.WeakReference

@SuppressLint("ViewConstructor")
class PersonalCenterView(context: Activity): DrawerPopupView(context) {
    private var binding: BizPersonalCenterLayoutBinding? = null
    private val activity = WeakReference(context).get()

    private val popupView = XPopup.Builder(context)
        .isCenterHorizontal(true)
        .autoFocusEditText(false)
        .popupPosition(PopupPosition.Left)
        .asCustom(this)

    override fun getImplLayoutId(): Int {
        return R.layout.biz_personal_center_layout
    }

    override fun onCreate() {
        super.onCreate()
        if (activity == null) return

        ImmersionBar.with(activity, dialog)
            .transparentStatusBar()
            .statusBarColor(android.R.color.transparent)
            .statusBarAlpha(0.0f)
            .statusBarDarkFont(true)
            .init()

        binding = BizPersonalCenterLayoutBinding.bind(popupContentView)
        updateUserInfo(CommonData.getInstance().currentUser())
        binding?.let {
            it.editProfileLayout.setOnClickListener {
                Log.d(MainV2Activity.TAG, "edit profile click")
                ARouterUtils.navigation(activity, ARouterConstants.PATH_ACTIVITY_PERSONAL, Bundle())
            }

            if (AndroidUtils.isExistClass("ai.txai.feedback.view.FeedbackActivity")) {
                it.feedback.visibility = View.VISIBLE
            }

            it.myTrips.setOnClickListener {
                Log.d(MainV2Activity.TAG, "myTrips click")
                val bundle = Bundle()
                bundle.putBoolean("content_has_toolbar", true)
                ARouterUtils.navigation(activity, ARouterConstants.PATH_FRAGMENT_TRIP_LIST, bundle)
            }

            it.help.setOnClickListener {
                val bundle = Bundle()
                bundle.putString("showType", "helper")
                ARouterUtils.navigation(activity, ARouterConstants.PATH_ACTIVITY_SETTING, bundle)
                Log.d(MainV2Activity.TAG, "help click")
            }

            it.feedback.setOnClickListener {
                ARouterUtils.navigation(activity, ARouterConstants.PATH_ACTIVITY_FEEDBACK)
            }

            it.settings.setOnClickListener {
                ARouterUtils.navigation(activity, ARouterConstants.PATH_ACTIVITY_SETTING)
            }
        }
    }

    fun showPop() {
        popupView.show()
    }

    fun dismissPop() {
        popupView.dismiss()
    }

    fun updateUserInfo(user: User) {
        if (activity == null || popupView == null || popupView.isDismiss) return

        binding?.let {
            it.name.text = user.nickname
            val options = ImageOptions.Builder()
                .setImageUrl(user.avatar)
                .build()
            it.drawerLeftAvatar.loadImage(activity, options)
        }
    }
}