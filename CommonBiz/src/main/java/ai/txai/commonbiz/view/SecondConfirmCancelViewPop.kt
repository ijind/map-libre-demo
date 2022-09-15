package ai.txai.commonbiz.view

import ai.txai.common.widget.txaiButton.TxaiButton
import ai.txai.commonbiz.R
import android.content.Context
import android.view.View
import android.widget.TextView
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BasePopupView
import com.lxj.xpopup.core.BottomPopupView
import com.lxj.xpopup.enums.PopupPosition

/**
 * Time: 2/24/22
 * Author Hay
 */
class SecondConfirmCancelViewPop(activity: Context) : BottomPopupView(activity) {
    private var popupView: BasePopupView = XPopup.Builder(context)
        .isCenterHorizontal(true)
        .autoFocusEditText(false)
        .popupPosition(PopupPosition.Bottom)
        .asCustom(this)
    var okListener:OnClickListener? =  null
    var secondConfirmBtn: TxaiButton? = null

    override fun getImplLayoutId(): Int {
        return R.layout.bottom_main_trip_second_confirm_cancel
    }

    override fun onCreate() {
        super.onCreate()
        secondConfirmBtn = findViewById(R.id.second_confirm_btn)
        secondConfirmBtn?.setPositiveClickListener(OnClickListener {
            dismissPop()
            okListener?.onClick(it)
        })
    }

    fun showPop() {
        popupView.show()
    }

    fun dismissPop() {
        popupView.dismiss()
    }
    fun setOnCancelClickListener(listener: OnClickListener?) {
        listener?.let { secondConfirmBtn?.setNegativeClickListener(it) }
    }

    fun setOnOkClickListener(listener: OnClickListener?) {
        this.okListener = listener
    }


    fun setNotice(text: String?, text1: String?) {
        findViewById<TextView>(R.id.tv_notice).text = text
        findViewById<TextView>(R.id.tv_notice_1).text = text1
    }

    fun setNavigateText(text: String?) {
        secondConfirmBtn?.setNegativeText(text ?: "")
    }

    fun setPositive(text: String?) {
        secondConfirmBtn?.setPositiveText(text ?: "")
    }
}