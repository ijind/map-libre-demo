package ai.txai.payment.view

import ai.txai.common.widget.VerificationCodeView
import ai.txai.payment.R
import android.content.Context
import android.widget.ImageView
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BasePopupView
import com.lxj.xpopup.core.BottomPopupView
import com.lxj.xpopup.util.KeyboardUtils

class CvvView(activity: Context) : BottomPopupView(activity) {
    private var cvvInputView: VerificationCodeView? = null
    private var dismissListener: DismissListener? = null
    private var inputDownListener: VerificationCodeView.InputDownListener? = null
    private var popupView: BasePopupView = XPopup.Builder(activity)
        .autoOpenSoftInput(true)
        .autoFocusEditText(true)
        .asCustom(this)
        .show()

    override fun getImplLayoutId(): Int {
        return R.layout.payment_input_cvv_dialog
    }

    override fun dismissOrHideSoftInput() {
        if (KeyboardUtils.sDecorViewInvisibleHeightPre == 0) {
            popupView.dismiss()
            dismissListener?.whenDismiss(true)
        } else KeyboardUtils.hideSoftInput(this)
    }

    override fun onCreate() {
        super.onCreate()
        cvvInputView = findViewById(R.id.login_sms_code_view)
        cvvInputView?.setInputDownListener(object : VerificationCodeView.InputDownListener {
            override fun onInputDown(cvvCode: String) {
                inputDownListener?.onInputDown(cvvCode)
                dismissPop()
            }
        })
        findViewById<ImageView>(R.id.cvv_close).setOnClickListener {
            popupView.dismiss()
            dismissListener?.whenDismiss(true)
        }
    }

    fun showPop() {
        popupView.show()
        post {
            cvvInputView?.clearText()
        }
    }

    fun dismissPop() {
        popupView.dismiss()
        dismissListener?.whenDismiss(false)
    }

    fun setInputDownListener(listener: VerificationCodeView.InputDownListener?) {
        inputDownListener = listener
    }

    fun setDismissListener(listener: DismissListener?) {
        dismissListener = listener
    }

    interface DismissListener {
        fun whenDismiss(backPressed:Boolean)
    }
}