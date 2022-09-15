package ai.txai.common.widget.popupview

import ai.txai.common.R
import android.content.Context
import android.view.View
import android.widget.TextView
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BasePopupView
import com.lxj.xpopup.core.BottomPopupView
import com.lxj.xpopup.enums.PopupPosition

class PhotoStyleView(private val activity: Context): BottomPopupView(activity) {
    companion object {
        private const val TAG = "PhotoStyleView"
        const val TAKE_PHOTO = 1
        const val CHOOSE_PHOTO = 2
    }
    private var photoListener: OnPhotoStyleClickListener? = null

    private var popupView: BasePopupView = XPopup.Builder(context)
        .isCenterHorizontal(true)
        .autoFocusEditText(false)
        .popupPosition(PopupPosition.Bottom)
        .asCustom(this)

    override fun getImplLayoutId(): Int {
        return R.layout.commonview_photo_style_view
    }

    override fun onCreate() {
        initView(popupContentView)
    }

    private fun initView(view: View) {
        val takePhoto = view.findViewById<TextView>(R.id.take_photo)
        takePhoto.setOnClickListener { photoListener?.onClick(TAKE_PHOTO) }
        val choosePhoto = view.findViewById<TextView>(R.id.choose_photo)
        choosePhoto.setOnClickListener { photoListener?.onClick(CHOOSE_PHOTO) }
        val cancelChoose = view.findViewById<TextView>(R.id.cancel_choose_tv)
        cancelChoose.setOnClickListener { popupView.dismiss() }
    }

    fun showPopup() {
        popupView.show()
    }

    fun dismissPopup() {
        popupView.smartDismiss()
    }

    fun setPhotoClickListener(listener: OnPhotoStyleClickListener) {
        photoListener = listener
    }

    interface OnPhotoStyleClickListener {
        fun onClick(style: Int)
    }
}