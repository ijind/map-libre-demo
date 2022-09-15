package ai.txai.common.widget.popupview

import ai.txai.common.R
import ai.txai.common.utils.setDebounceClickListener
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.TextView
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BasePopupView
import com.lxj.xpopup.core.BottomPopupView
import com.lxj.xpopup.enums.PopupPosition

class CustomerServiceView(activity: Context): BottomPopupView(activity) {
    companion object {
        private const val TXAI_EMAIL = "robotxai@xxx.ai"
    }

    private var popupView: BasePopupView = XPopup.Builder(context)
        .isCenterHorizontal(true)
        .autoFocusEditText(false)
        .popupPosition(PopupPosition.Bottom)
        .asCustom(this)

    override fun getImplLayoutId(): Int {
        return R.layout.commonview_customer_service_layout
    }

    override fun onCreate() {
        super.onCreate()
        val cancel = popupContentView.findViewById<TextView>(R.id.cancel_choose_tv)
        cancel.setDebounceClickListener {
            dismissPop()
        }
        val sendEmail = popupContentView.findViewById<TextView>(R.id.email_tv)
        sendEmail.setDebounceClickListener {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:$TXAI_EMAIL")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(Intent.createChooser(intent, ""))
            dismissPop()
        }
    }

    fun showPop() {
        popupView.show()
    }

    fun dismissPop() {
        popupView.dismiss()
    }
}