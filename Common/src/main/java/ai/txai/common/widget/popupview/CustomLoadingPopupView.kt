package ai.txai.common.widget.popupview

import ai.txai.common.R
import android.content.Context
import android.graphics.Color
import android.text.TextUtils
import android.widget.TextView
import com.blankj.utilcode.util.ThreadUtils
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BasePopupView
import com.lxj.xpopup.core.CenterPopupView

class CustomLoadingPopupView(context: Context) : CenterPopupView(context) {
    private var popupView: BasePopupView = XPopup.Builder(context)
        .isCenterHorizontal(true)
        .autoFocusEditText(false)
        .dismissOnTouchOutside(false)
        .dismissOnBackPressed(false)
        .hasShadowBg(false)
        .navigationBarColor(Color.BLACK)
        .asCustom(this)

    var timeoutListener: TimeoutListener? = null

    override fun getImplLayoutId(): Int {
        return R.layout.commonview_loading_layout
    }

    var runnable: Runnable = Runnable {
        timeoutListener?.onTimeout()
        dismissPopup()
    }


    override fun doAfterShow() {
        super.doAfterShow()
        ThreadUtils.runOnUiThreadDelayed(runnable, 30000)
    }

    override fun doAfterDismiss() {
        super.doAfterDismiss()
        ThreadUtils.getMainHandler().removeCallbacks(runnable)
    }

    fun showPopup(text: String = "") {
        popupView.show()
        if (!TextUtils.isEmpty(text)) {
            popupView.post {
                findViewById<TextView>(R.id.loading_tv).text = text
            }
        }
    }

    override fun onDestroy() {
        ThreadUtils.getMainHandler().removeCallbacks(runnable)
        super.onDestroy()
    }

    fun dismissPopup() {
        if (!isDismiss) {
            popupView.dismiss()
        }
        timeoutListener = null
    }

    interface TimeoutListener {
        fun onTimeout()
    }
}