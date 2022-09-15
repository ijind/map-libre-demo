package ai.txai.setting.popup

import ai.txai.common.utils.setDebounceClickListener
import ai.txai.common.widget.txaiButton.TxaiButton
import ai.txai.database.GreenDaoHelper
import ai.txai.setting.R
import android.annotation.SuppressLint
import android.app.Activity
import android.widget.TextView
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BasePopupView
import com.lxj.xpopup.core.CenterPopupView

@SuppressLint("ViewConstructor")
class SettingLogoutView(activity: Activity,
                        private val listener: OnLogoutConfirmListener): CenterPopupView(activity) {
    private var popupView: BasePopupView = XPopup.Builder(context)
        .isCenterHorizontal(true)
        .autoFocusEditText(false)
        .dismissOnTouchOutside(false)
        .asCustom(this)

    override fun getImplLayoutId(): Int {
        return R.layout.setting_logout_layout
    }

    override fun onCreate() {
        super.onCreate()
        val logoutBtn = popupContentView.findViewById<TxaiButton>(R.id.logout_btn)
        logoutBtn.setNegativeClickListener { dismissPop() }
        logoutBtn.setPositiveClickListener {
            listener.onLogoutConfirm()
            dismissPop()
        }
    }

    fun showPop() {
        popupView.show()
    }

    fun dismissPop() {
        popupView.dismiss()
    }

    interface OnLogoutConfirmListener {
        fun onLogoutConfirm()
    }
}