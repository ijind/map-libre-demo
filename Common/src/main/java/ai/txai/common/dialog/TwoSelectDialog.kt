package ai.txai.common.dialog

import ai.txai.common.R
import ai.txai.common.utils.setDebounceClickListener
import ai.txai.common.widget.txaiButton.TxaiButton
import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.widget.TextView
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BasePopupView
import com.lxj.xpopup.core.CenterPopupView

@SuppressLint("ViewConstructor")
class TwoSelectDialog(
    activity: Activity,
    private val listener: OnClickListener,
    val title: String,
    val navigateText:String,
    val positiveText:String
) : CenterPopupView(activity) {
    private var popupView: BasePopupView = XPopup.Builder(context)
        .isCenterHorizontal(true)
        .autoFocusEditText(false)
        .dismissOnTouchOutside(false)
        .navigationBarColor(Color.BLACK)
        .asCustom(this)

    override fun getImplLayoutId(): Int {
        return R.layout.common_dialog_two_select_layout
    }

    override fun onCreate() {
        super.onCreate()
        val selectBtn = popupContentView.findViewById<TxaiButton>(R.id.select_btn)
        selectBtn.setNegativeText(navigateText)
        selectBtn.setNegativeClickListener {
            listener.onNegative()
            dismissPop()
        }
        selectBtn.setPositiveText(positiveText)
        selectBtn.setPositiveClickListener {
            listener.onPositive()
            dismissPop()
        }

        val titleTv = popupContentView.findViewById<TextView>(R.id.title_tv)
        titleTv.text = title
    }

    fun showPop() {
        popupView.show()
    }

    fun dismissPop() {
        popupView.dismiss()
    }

    interface OnClickListener {
        fun onPositive()
        fun onNegative()
    }
}