package ai.txai.personal.view

import ai.txai.common.base.BaseScrollFragment
import ai.txai.common.glide.GlideUtils.loadImage
import ai.txai.common.glide.ImageOptions
import ai.txai.common.log.LOG
import ai.txai.common.permission.PermissionSettings
import ai.txai.common.permission.PermissionUtils
import ai.txai.common.permission.dialog.PermissionDialog
import ai.txai.common.utils.*
import ai.txai.common.widget.popupview.PhotoStyleView
import ai.txai.common.widget.txaiedittext.TxaiEditText
import ai.txai.database.user.User
import ai.txai.database.utils.CommonData
import ai.txai.personal.R
import ai.txai.personal.databinding.PersonalUserFragmentLayoutBinding
import ai.txai.personal.popup.ShowImageView
import ai.txai.personal.viewmodel.PersonalViewModel
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.blankj.utilcode.util.RegexUtils
import java.io.File

class UserInfoFragment: BaseScrollFragment<PersonalUserFragmentLayoutBinding, PersonalViewModel>() {
    companion object {
        private const val TAG = "UserInfoFragment"
        private const val TAKE_PHOTO = 1
        private const val CHOOSE_PHOTO = 2
        private const val CLIP_PHOTO = 3

        private const val AVATAR = "avatar"
        private const val FILE_PROVIDER_AUTHORITY = "ai.txai.personal.fileprovider"
    }
    private var photoView: PhotoStyleView? = null
    private var showImageView: ShowImageView? = null
    private lateinit var activityLauncher: ActivityResultLauncher<Intent>

    private var imageUri: Uri? = null
    private var avatarUri: Uri? = null
    private var type: Int = 0
    private var avatarPath = ""
    private var path: String = ""
    private var isSetAvatar = false
    private var curUser: User? = null

    override fun initViewObservable() {
        super.initViewObservable()
        curUser = CommonData.getInstance().currentUser()
        initView()
        initAvatar()
        initListener()
        addObserver()
        activityLauncher = initActivityLauncher()
    }

    private fun initListener() {
        itemBinding.userEditAvatarTv.setDebounceClickListener {
            photoView?.showPopup()
        }
        itemBinding.userAvatarImg.setDebounceClickListener {
            if (isSetAvatar && avatarUri != null) {
                showImageView?.showPop(avatarUri)
            } else {
                showImageView?.show()
            }
        }

        positiveClickListener(R.string.personal_user_info_save) {
            val nickNameEdit = itemBinding.userNickNameTv.getInputText()
            val nickName = nickNameEdit.ifEmpty { "" }
            if (nickName.trim().isEmpty()) {
                ToastUtils.show(R.string.personal_user_nick_hint)
                return@positiveClickListener
            }

            if (avatarPath.isNotEmpty()) {
                showLoading("")
                viewModel.uploadUserImg(AVATAR, File(avatarPath))
            } else {
                checkUpdateUserInfo("")
            }
        }
    }

    // 昵称规则 不能以数字 空格开头， 中间不能有连续的空格 需要添加过滤规则
    private fun addObserver() {
        viewModel.getUploadFileInfo().observe(this) {
            checkUpdateUserInfo(it.getPath())
        }

        viewModel.getUpdateUserInfoResult().observe(this) {
            hideLoading()
            if (it == null) return@observe

            if (it) {
                curUser?.let { user ->
                    val nickname = itemBinding.userNickNameTv.getInputText()
                    if (nickname.isNotEmpty()) {
                        user.nickname = nickname
                    }
                    val imageInfo = viewModel.getUploadFileInfo().value
                    if (imageInfo != null && imageInfo.getUrl().isNotEmpty()) {
                        user.avatar = imageInfo.getUrl()
                    }
                    val email = itemBinding.userEmailEdit.getInputText()
                    user.email = email

                    CommonData.getInstance().whenLogin(curUser)
                }
                ToastUtils.show("Successfully saved")
                clearAvatarFile()
            }
        }
    }

    private fun initView() {
        bottomShadowVisible(false)

        curUser?.let {
            itemBinding.userNickNameTv.setInputText(it.nickname)
            val endIndex = it.phoneNumber.toString().length - 5
            val newNumber = FormatUtils.replaceRangeToStar(endIndex, it.phoneNumber.toString())
            val phoneNumber = "+${it.countryCode} $newNumber"
            itemBinding.userPhoneItem.setRightContent(phoneNumber)
            itemBinding.userEmailEdit.setInputText(it.email ?: "")
        }

        photoView = PhotoStyleView(this.requireActivity())
        photoView!!.setPhotoClickListener(object: PhotoStyleView.OnPhotoStyleClickListener {
            override fun onClick(style: Int) {
                when(style) {
                    PhotoStyleView.TAKE_PHOTO -> takePhoto()
                    PhotoStyleView.CHOOSE_PHOTO -> openAlbum()
                }
                photoView?.dismissPopup()
            }
        })

        showImageView = ShowImageView(this.requireActivity())
        showImageView!!.setOnChangePhotoListener(object: ShowImageView.OnChangePhotoClickListener {
            override fun onChangePhoto() {
                photoView?.showPopup()
            }
        })

        if (itemBinding.userNickNameTv.getInputText().isNotEmpty()) {
            positiveEnableClick(true)
        }

        itemBinding.userNickNameTv.setTxaiEditTextListener(object: TxaiEditText.TxaiEditTextListener {
            override fun onInputAfter(s: Editable) {
                if (s.toString().trim().isNotEmpty()) {
                    positiveEnableClick(true)
                } else {
                    positiveEnableClick(false)
                }
            }

            override fun onFocusChange(focus: Boolean) {
            }

        })
    }

    private fun initAvatar() {
        LOG.d(TAG, "avatar ${curUser?.avatar}")
        val option = ImageOptions.build {
            setImageUrl(curUser?.avatar ?: "")
        }
        if (option.imageUrl.isEmpty()) return

        context?.let { itemBinding.userAvatarImg.loadImage(it, option) }
    }

    private fun initActivityLauncher(): ActivityResultLauncher<Intent> {
        val activityLauncher = registerForActivityResult(
            ActivityResultContracts.
            StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                when (this.type) {
                    TAKE_PHOTO -> {
                        imageUri?.let { launchClipActivity(it, path) }
                    }
                    CHOOSE_PHOTO -> {
                        val uri: Uri? = result.data?.data
                        val path = uri?.let { buildPathByUri(it) } ?: ""
                        this.path = path
                        uri?.let { launchClipActivity(it, path) }
                    }
                    else -> {
                        result.data?.let {
                            clearAvatarFile()

                            itemBinding.userAvatarImg.loadImage(requireContext(),
                                ImageOptions.build { setImageUrl(it.data.toString()) })
                            isSetAvatar = true
                            avatarUri = it.data
                            avatarPath = it.getStringExtra("avatar_path") ?: ""
                        }
                        LOG.d(TAG, "result ${result.data?.data}")
                    }
                }
            }
        }
        return activityLauncher
    }

    private fun buildPathByUri(uri: Uri): String {
        clearPhotoFile()
        var finalPath = ""
        activity?.let { context ->
            val newFile = BitmapUtils.createImageFile(context)
            val bitmap: Bitmap? = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            newFile?.let { file ->
                finalPath = bitmap?.let { it ->
                    BitmapUtils.compressBitmap(file.absolutePath, it, 80) } ?: ""
            }
        }
        return finalPath
    }

    private fun takePhoto() {
        PermissionUtils.requestCameraPermission(context, requestResult)
    }

    private fun openAlbum() {
        val openAlbumIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        openAlbumIntent.setDataAndType(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            "image/jpeg")
        type = CHOOSE_PHOTO
        activityLauncher.launch(openAlbumIntent)
    }

    private fun launchClipActivity(uri: Uri, path: String) {
        if (path.isEmpty()) return
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = uri
        intent.putExtra("path", path)
        context?.let { intent.setClass(it, ClipImageActivity::class.java) }
        activityLauncher.launch(intent)
        type = CLIP_PHOTO
    }

    private fun clearAvatarFile() {
        avatarUri = null
        if (avatarPath.isNotEmpty()) {
            val file = File(avatarPath)
            if (file.exists()) file.delete()
            avatarPath = ""
        }
        clearPhotoFile()
    }

    private fun clearPhotoFile() {
        if (path.isNotEmpty()) {
            val file = File(path)
            if (file.exists()) file.delete()
            path = ""
        }
    }

    private fun checkUpdateUserInfo(avatar: String) {
        val nickname = itemBinding.userNickNameTv.getInputText()
        val emailEdit = itemBinding.userEmailEdit.getInputText()
        val email = emailEdit.ifEmpty { "" }
        checkEmail(email) {
            showLoading("")
            viewModel.updateUserInfo(avatar, nickname, email)
        }
    }

    private fun checkEmail(email: String, operation: () -> Unit) {
        if (email.isEmpty()) {
            operation.invoke()
            return
        }

        if (RegexUtils.isEmail(email)) {
            operation.invoke()
        } else {
            ToastUtils.show(R.string.personal_email_prompt)
        }
    }

    private val requestResult = object: PermissionUtils.RequestResultWithNotice {
        override fun onGranted(permissions: MutableList<String>?) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra("com.google.assistant.extra.USE_FRONT_CAMERA", true)
            intent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true)
            intent.putExtra("android.intent.extras.LENS_FACING_FRONT", 1)
            intent.putExtra("android.intent.extras.CAMERA_FACING", 1)

            // Samsung
            intent.putExtra("camerafacing", "front")
            intent.putExtra("previous_mode", "front")

            // Huawei
            intent.putExtra("default_camera", "1")
            intent.putExtra("default_mode", "com.huawei.camera2.mode.photo.PhotoMode")
            if (intent.resolveActivity(requireActivity().packageManager) != null) {
                LOG.d(TAG, "start capture")
                clearPhotoFile()
                val imgFile = BitmapUtils.createImageFile(requireActivity())
                imgFile?.let { file ->
                    path = file.absolutePath
                    imageUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        context?.let {
                            FileProvider.getUriForFile(it, FILE_PROVIDER_AUTHORITY, file)
                        }
                    } else {
                        Uri.fromFile(imgFile)
                    }
                }
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                activityLauncher.launch(intent)
                type = TAKE_PHOTO
            }
        }

        override fun onDenied(permissions: MutableList<String>?) {

        }

        override fun onDeniedNotAsk(permissionSettings: PermissionSettings?) {
            PermissionDialog.show(activity, permissionSettings)
        }

        override fun onShowNotice(permissionSettings: PermissionSettings?) {
            PermissionDialog.show(activity, permissionSettings)
        }

    }

    override fun getCustomTitle() = R.string.personal_user_info_title

    override fun initItemBinding(parent: ViewGroup): PersonalUserFragmentLayoutBinding {
        return PersonalUserFragmentLayoutBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
    }
}