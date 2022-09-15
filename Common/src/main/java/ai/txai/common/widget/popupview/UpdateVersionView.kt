package ai.txai.common.widget.popupview

import ai.txai.common.R
import ai.txai.common.databinding.CommonUpdateVersionLayoutBinding
import ai.txai.common.utils.setDebounceClickListener
import android.content.Context
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BasePopupView
import com.lxj.xpopup.enums.PopupAnimation

class UpdateVersionView(context: Context): BasePopupView(context) {
    companion object {
        private const val TAG = "UpgradeVersionView"
        const val NORMAL_TYPE = 1
        const val FORCE_TYPE = 2
    }
    private lateinit var binding: CommonUpdateVersionLayoutBinding
    private var confirmListener: OnUpgradeConfirmListener? = null
    private var versionTv: String = ""
    private var contentTv: String = ""
    private var updateType = NORMAL_TYPE

    private var popupView: BasePopupView = XPopup.Builder(context)
        .isCenterHorizontal(true)
        .popupAnimation(PopupAnimation.ScaleAlphaFromCenter)
        .dismissOnTouchOutside(false)
        .dismissOnBackPressed(false)
        .asCustom(this)

    override fun getInnerLayoutId(): Int {
        return R.layout.common_update_version_layout
    }

    override fun doAfterShow() {
        super.doAfterShow()
        if (versionTv.isNotEmpty()) {
            binding.newVersionTv.text = versionTv
        }
        if (contentTv.isNotEmpty()) {
            binding.newVersionInfoTv.text = contentTv
        }
    }

    override fun onCreate() {
        super.onCreate()
        binding = CommonUpdateVersionLayoutBinding.bind(popupContentView)
        binding.updateBtn.setNegativeClickListener { dismissPop() }
        binding.updateBtn.setPositiveClickListener {
            confirmListener?.onUpgradeConfirm()
            if (updateType != FORCE_TYPE) {
                dismissPop()
            }
        }
    }

    fun setOnUpgradeConfirmListener(listener: OnUpgradeConfirmListener) {
        confirmListener = listener
    }

    fun showPop(version: String, content: String, type: Int) {
        if (popupView.isShow) return
        versionTv = version
        contentTv = content
        updateType = type
        popupView.show()
    }

    fun dismissPop() {
       if (popupView.isDismiss) return
       popupView.dismiss()
    }

    interface OnUpgradeConfirmListener {
        fun onUpgradeConfirm()
    }
}