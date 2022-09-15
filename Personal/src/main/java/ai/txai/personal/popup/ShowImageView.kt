package ai.txai.personal.popup

import ai.txai.common.glide.GlideUtils.loadImage
import ai.txai.common.glide.ImageOptions
import ai.txai.personal.R
import ai.txai.personal.databinding.PersonalShowImageViewLayoutBinding
import ai.txai.database.utils.CommonData
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.bumptech.glide.Glide
import com.gyf.immersionbar.ImmersionBar
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BasePopupView
import com.lxj.xpopup.enums.PopupAnimation

class ShowImageView(context: Context): BasePopupView(context) {
    companion object {
        private const val TAG = "ShowImageView"
    }
    private lateinit var binding: PersonalShowImageViewLayoutBinding
    private var uri: Uri? = null
    private var bitmap: Bitmap? = null
    private var changeListener: OnChangePhotoClickListener? = null

    private var popupView: BasePopupView = XPopup.Builder(context)
        .isCenterHorizontal(true)
        .popupAnimation(PopupAnimation.ScaleAlphaFromCenter)
        .dismissOnTouchOutside(false)
        .asCustom(this)

    override fun getInnerLayoutId(): Int {
        return R.layout.personal_show_image_view_layout
    }

    override fun doAfterDismiss() {
        super.doAfterDismiss()
        ImmersionBar.with(context as Activity)
            .transparentStatusBar()
            .statusBarColor(android.R.color.transparent)
            .statusBarAlpha(0.0f)
            .statusBarDarkFont(true)
            .init()
    }

    override fun doAfterShow() {
        super.doAfterShow()
        ImmersionBar.with(context as Activity)
            .transparentStatusBar()
            .statusBarColor(android.R.color.transparent)
            .statusBarAlpha(0.0f)
            .statusBarDarkFont(false)
            .init()
        val user = CommonData.getInstance().currentUser()
        val option = ImageOptions.build {
            setImageUrl(user?.avatar ?: "")
        }
        if (option.imageUrl.isEmpty()) {
            return
        }

        context?.let { binding.imgIc.loadImage(it, option) }

        if (this.uri != null) {
            Glide.with(context).load(uri).into(binding.imgIc)
        }
    }

    override fun onCreate() {
        super.onCreate()
        binding = PersonalShowImageViewLayoutBinding.bind(popupContentView)
        binding.backIc.setOnClickListener { dismiss() }
        binding.changePhotoTv.setOnClickListener {
            dismiss()
            changeListener?.onChangePhoto()
        }
    }

    fun setOnChangePhotoListener(listener: OnChangePhotoClickListener) {
        changeListener = listener
    }

    fun showPop(uri: Uri?) {
        this.uri = uri
        popupView.show()
    }

    interface OnChangePhotoClickListener {
        fun onChangePhoto()
    }
}