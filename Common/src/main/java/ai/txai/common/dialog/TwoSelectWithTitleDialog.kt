package ai.txai.common.dialog

import ai.txai.common.R
import ai.txai.common.databinding.CommonDialogTwoSelectWithTitleLayoutBinding
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
class TwoSelectWithTitleDialog(
    activity: Activity,
    private val listener: OnClickListener,
    val title: String,
    val content: String,
    val navigateText: String,
    val positiveText: String
) : CenterPopupView(activity) {
    private var popupView: BasePopupView = XPopup.Builder(context)
        .isCenterHorizontal(true)
        .autoFocusEditText(false)
        .dismissOnTouchOutside(false)
        .navigationBarColor(Color.BLACK)
        .asCustom(this)

    override fun getImplLayoutId(): Int {
        return R.layout.common_dialog_two_select_with_title_layout
    }

    override fun onCreate() {
        super.onCreate()
        val twoSelectBtn = popupContentView.findViewById<TxaiButton>(R.id.two_select_btn)
        twoSelectBtn.setNegativeText(navigateText)
        twoSelectBtn.setNegativeClickListener {
            listener.onNegative()
            dismissPop()
        }
        twoSelectBtn.setPositiveText(positiveText)
        twoSelectBtn.setPositiveClickListener {
            listener.onPositive()
            dismissPop()
        }

        val titleTv = popupContentView.findViewById<TextView>(R.id.title_tv)
        titleTv.text = title
        val contentTv = popupContentView.findViewById<TextView>(R.id.content_tv)
        contentTv.text = content
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