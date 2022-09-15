package ai.txai.common.dialog

import ai.txai.common.R
import ai.txai.common.databinding.CommonDialogConfirmWithTitleIconBinding
import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BasePopupView
import com.lxj.xpopup.core.CenterPopupView

@SuppressLint("ViewConstructor")
class ConfirmWithTitleAndIconDialog(
    activity: Activity,
    val iconRes: Int,
    val title: String,
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
        return R.layout.common_dialog_confirm_with_title_icon
    }

    override fun onCreate() {
        super.onCreate()
        var bind = CommonDialogConfirmWithTitleIconBinding.bind(contentView)
        bind.confirmButton.setVisibleButtonClickListener {
            dismissPop()
            listener?.onClick(this)
        }
        bind.ivIcon.setImageResource(iconRes)
        bind.dialogTitle.text = title
        bind.dialogContent.text = content
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