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
class ConfirmDialog(
    activity: Activity,
    val content: String,
    isDestroyOnDismiss: Boolean
) : CenterPopupView(activity) {
    private var popupView: BasePopupView = XPopup.Builder(context)
        .isCenterHorizontal(true)
        .autoFocusEditText(false)
        .dismissOnBackPressed(false)
        .dismissOnTouchOutside(false)
        .isDestroyOnDismiss(isDestroyOnDismiss)
        .navigationBarColor(Color.BLACK)
        .asCustom(this)

    private var listener: OnClickListener? = null

    override fun getImplLayoutId(): Int {
        return R.layout.common_dialog_confirm
    }

    override fun onCreate() {
        super.onCreate()
        val confirmTv = popupContentView.findViewById<TxaiButton>(R.id.confirm_button)
        confirmTv.setPositiveClickListener {
            dismissPop()
            listener?.onClick(this)
        }

        val contentTv = popupContentView.findViewById<TextView>(R.id.dialog_content)
        contentTv.text = content
    }

    fun showPop() {
        popupView.show()
    }

    fun dismissPop() {
        popupView.dismiss()
    }

    fun setOkListener(listener: OnClickListener?) {
        this.listener = listener
    }
}