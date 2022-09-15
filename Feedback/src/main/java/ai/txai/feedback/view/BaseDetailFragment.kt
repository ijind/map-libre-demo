package ai.txai.feedback.view

import ai.txai.common.base.BaseScrollFragment
import ai.txai.common.base.helper.ScrollBindingHelper
import ai.txai.common.data.FileInfo
import ai.txai.common.log.LOG
import ai.txai.common.permission.PermissionSettings
import ai.txai.common.permission.PermissionUtils
import ai.txai.common.permission.dialog.PermissionDialog
import ai.txai.common.router.ARouterConstants
import ai.txai.common.utils.AndroidUtils.dip2px
import ai.txai.common.utils.BitmapUtils
import ai.txai.common.widget.popupview.PhotoStyleView
import ai.txai.common.widget.txaiedittext.TxaiEditText
import ai.txai.feedback.R
import ai.txai.feedback.adapter.UploadImageAdapter
import ai.txai.feedback.databinding.FeedbackCommonLayoutBinding
import ai.txai.feedback.viewmodel.FeedbackViewModel
import ai.txai.imageselector.utils.ImageSelector
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.*
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toUri
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.KeyboardUtils
import java.io.File
import kotlin.math.abs

open class BaseDetailFragment:
    BaseScrollFragment<FeedbackCommonLayoutBinding, FeedbackViewModel>() {
    val selectFileList = ArrayList<String>()
    val uploadFileList = ArrayList<FileInfo>()
    val uploadedFilePathList = ArrayList<FileInfo>()
    val uploadImageList = ArrayList<File>()

    var isGoSend = false

    private var photoView: PhotoStyleView? = null

    private var imageUri: Uri? = null
    private var launchType: Int = 0
    private var path: String = ""
    private var keyboardHeight = 0
    private var contentViewY = 0f

    private var isUploading = false

    private lateinit var activityLauncher: ActivityResultLauncher<Intent>
    private lateinit var uploadAdapter: UploadImageAdapter
    private val feedbackViewModel: FeedbackViewModel by activityViewModels()

    companion object {
        private const val TAG = "FeedbackDetailFragment"
        private const val REQUEST_CODE = 10001

        private const val TAKE_PHOTO = 1
        private const val CHOOSE_PHOTO = 2
        private const val FILE_PROVIDER_AUTHORITY = "ai.txai.personal.fileprovider"

        private const val MAX_COUNT = 3

        private const val FROM_TAKE_PHOTO = 1
        private const val FROM_OPEN_ALBUM = 2
        private const val UPLOAD_DONE = 3

        const val SEND_LOGS_KEY = "send_log"
        const val REPORT_ISSUE = "Report an issue"
    }

    override fun initViewObservable() {
        super.initViewObservable()
        ScrollBindingHelper.titleBarBackgroundColor(binding, R.color.white)
        initView()

        addSoftKeyObserver()

        viewModel.getUploadImageInfo().observe(this) { imageInfo ->
            if (imageInfo == null) {
                isGoSend = false
                return@observe
            }

            isUploading = false
            for (i in 0 until imageInfo.size) {
                uploadImageList.removeAt(0)
            }

            imageInfo.forEach {
                val fileInfo = FileInfo()
                fileInfo.setFileTag(0)
                fileInfo.setFilePath(it.getPath())
                uploadedFilePathList.add(fileInfo)
            }

            for (i in 0 until uploadedFilePathList.size) {
                if (uploadedFilePathList[i].getFileTag() == UPLOAD_DONE) {
                    continue
                }

                val child = itemBinding.uploadImageListView.getChildAt(i)
                child?.let {
                    val holder = itemBinding.uploadImageListView.getChildViewHolder(child)
                    if (holder != null)  {
                        holder as UploadImageAdapter.UploadImageVH
                        val fileInfo = uploadedFilePathList[i]
                        fileInfo.setFileTag(UPLOAD_DONE)
                        holder.binding.removeImg.visibility = View.VISIBLE
                    }
                }
            }

            if (uploadImageList.size > 0) {
                uploadUserImg(uploadImageList, false)
            } else if (isGoSend) {
                invokeSendOperation()
            }
        }
    }

    override fun initViewModel(): FeedbackViewModel? {
        return feedbackViewModel
    }

    protected open fun initView() {
        context?.let {
            uploadAdapter = UploadImageAdapter(it)
            itemBinding.uploadImageListView.adapter = uploadAdapter
            val manager = LinearLayoutManager(it)
            manager.orientation = LinearLayoutManager.HORIZONTAL
            itemBinding.uploadImageListView.layoutManager = manager
            uploadAdapter.setItemRemoveListener(object: UploadImageAdapter.OnItemRemoveListener {
                override fun onItemRemove(position: Int) {
                    uploadFileList.removeAt(position)
                    selectFileList.removeAt(position)
                    uploadedFilePathList.removeAt(position)
                    if (uploadFileList.size < MAX_COUNT) {
                        itemBinding.uploadView.uploadCameraLayout.visibility = View.VISIBLE
//                        itemBinding.spaceHolderView.visibility = View.VISIBLE
                    }
                }
            })
        }
        activityLauncher = initActivityLauncher()
        photoView = PhotoStyleView(requireActivity())
        photoView!!.setPhotoClickListener(object: PhotoStyleView.OnPhotoStyleClickListener {
            override fun onClick(style: Int) {
                when(style) {
                    PhotoStyleView.TAKE_PHOTO ->
                        PermissionUtils.requestCameraPermission(context, requestResult)
                    PhotoStyleView.CHOOSE_PHOTO -> openAlbum()
                }
                photoView?.dismissPopup()
            }
        })

        ScrollBindingHelper.operationClickListener(binding, getOperationString()) {
            val describe = itemBinding.feedbackDescribeEdit.getInputText().trim()
            if (describe.length < 8) {
                showToast("Please write at least 8 words", false)
                return@operationClickListener
            }

            isGoSend = true
            invokeSendOperation()
        }

        operationClickEnable(false)

        itemBinding.uploadView.uploadCameraLayout.setOnClickListener {
            photoView?.showPopup()
        }

        itemBinding.feedbackDescribeEdit.setTxaiEditTextListener(object: TxaiEditText.TxaiEditTextListener {
            override fun onInputAfter(s: Editable) {
                val count = s.length
                if (count > 0) {
                    operationClickEnable(true)
                } else {
                    operationClickEnable(false)
                }
            }

            override fun onFocusChange(focus: Boolean) {

            }

        })

        itemBinding.feedbackContactEdit.setOnFocusChangeListener { _, isFocus ->
            if (isFocus && KeyboardUtils.isSoftInputVisible(requireActivity())) {
                val rootView = binding.scroll
                val dis = rootView.height - itemBinding.feedbackContactEdit.bottom - keyboardHeight
                if (keyboardHeight > 0) {
                    rootView.y = -(abs(dis) + dip2px(requireActivity(), 10f).toFloat())
                }
            }
        }

        binding.scroll.viewTreeObserver.addOnGlobalLayoutListener(object:
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                contentViewY = binding.scroll.y
                LOG.d(TAG, "init y $contentViewY")
                binding.scroll.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }

    private fun initActivityLauncher(): ActivityResultLauncher<Intent> {
        val activityLauncher = registerForActivityResult(
            ActivityResultContracts.
            StartActivityForResult()) { result ->
            LOG.d(TAG, "result ${result.resultCode}")
            if (result.resultCode == Activity.RESULT_OK) {
                when(launchType) {
                    TAKE_PHOTO -> {
                        if (imageUri == null) return@registerForActivityResult

                        activity?.let { context ->
                            val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver,
                                imageUri)
                            bitmap?.let { BitmapUtils.compressBitmap(path, it, 10) }
                            selectFileList.add(path)
                            checkUploadFile(imageUri!!, path, FROM_TAKE_PHOTO)
                            val cacheList = ArrayList<File>()
                            cacheList.add(File(path))
                            uploadUserImg(cacheList)
                        }

                    }
                }
                LOG.d(TAG, "result ${result.data?.data}")
            }
        }
        return activityLauncher
    }

    private fun checkUploadFile(uri: Uri, path: String, tag: Int) {
        if (uploadFileList.size < MAX_COUNT) {
            val fileInfo = FileInfo()
            fileInfo.setFilePath(path)
            fileInfo.setFileTag(tag)
            uploadFileList.add(fileInfo)
            val uriList = ArrayList<Uri>()
            uriList.add(uri)
            uploadAdapter.setData(uriList)
            if (uploadFileList.size == MAX_COUNT) {
                itemBinding.uploadView.uploadCameraLayout.visibility = View.GONE
//                itemBinding.spaceHolderView.visibility = View.GONE
            }
        }
    }

    private fun openAlbum() {
        var selectedCount = 0
        uploadFileList.forEach {
            if (it.getFileTag() == FROM_OPEN_ALBUM) selectedCount += 1
        }
        val maxCount = MAX_COUNT - uploadFileList.size + selectedCount
        ImageSelector.builder()
            .useCamera(false)
            .setSingle(false)
            .setMaxSelectCount(maxCount)
            .setSelected(selectFileList)
            .canPreview(true)
            .start(this, REQUEST_CODE)
        launchType = CHOOSE_PHOTO
    }

    protected open fun invokeSendOperation() {

    }

    protected open fun getOperationString(): Int {
        return  0
    }

    fun getPromptText(text: String): SpannableStringBuilder {
        val foreTextF = "For general inquiries and frequently asked questions, please visit "
        val serviceText = "Service and Help"
        val builder = SpannableStringBuilder()
        builder.append(foreTextF).append(serviceText).append(".")
        val start = foreTextF.length
        val end  = builder.toString().length - 1
        val serviceHelperSpan = object: ClickableSpan(), NoCopySpan {
            override fun onClick(widget: View) {
                val bundle = Bundle()
                bundle.putString("showType", "helper")
                viewModel.router(ARouterConstants.PATH_ACTIVITY_SETTING, bundle)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
                ds.color = ResourcesCompat.getColor(resources,
                    R.color.commonview_orange_00, null)
            }
        }
        builder.setSpan(serviceHelperSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        if (text.isNotEmpty()) {
            val reportIssueForeText = "\nFor app running issues, please use "
            val issueStart = builder.toString().length + reportIssueForeText.length
            builder.append(reportIssueForeText).append(text).append(".")
            val issueEnd = builder.toString().length - 1
            val reportIssueSpan = object: ClickableSpan(), NoCopySpan {
                override fun onClick(widget: View) {
                    viewModel.getFeedbackStatus().value = FeedbackViewModel.CHOOSE_ISSUE
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = false
                    ds.color = ResourcesCompat.getColor(resources,
                        R.color.commonview_orange_00, null)
                }
            }

            builder.setSpan(reportIssueSpan, issueStart, issueEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        return builder
    }

    private fun buildPathByUri(uri: Uri): String {
        var finalPath = ""
        activity?.let { context ->
            val newFile = BitmapUtils.createImageFile(context)
            val bitmap: Bitmap? = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            newFile?.let { file ->
                bitmap?.let { it ->
                    finalPath = BitmapUtils.compressBitmap(file.absolutePath, it, 10)
                }
            }
        }
        return finalPath
    }

    private fun uploadUserImg(list: ArrayList<File>, isAdd: Boolean = true) {
        if (isAdd) {
            uploadImageList.addAll(list)
        }
        LOG.d(TAG, "uploadUserImg !!! ${uploadImageList.size}")
        if (!isUploading) {
            viewModel.uploadUserImg(uploadImageList)
            isUploading = true
        }
    }

    private fun addSoftKeyObserver() {
        activity?.let {
            KeyboardUtils.registerSoftInputChangedListener(it) { height: Int ->
                keyboardHeight = height
                val decorView = binding.scroll
                if (!itemBinding.feedbackContactEdit.hasFocus()) {
                    if (decorView.y != contentViewY) {
                        decorView.y = contentViewY
                    }
                    return@registerSoftInputChangedListener
                }

                val bottom = itemBinding.feedbackContactEdit.bottom
                val dis = decorView.height - bottom - height - binding.bigTitle.root.measuredHeight
                val titleHeight = binding.titleLayout.measuredHeight
                if (height > 0) {
                    if (dis >= 0) {
                        return@registerSoftInputChangedListener
                    }
                    decorView.y = -(abs(dis) + dip2px(it, 10f).toFloat()) + titleHeight
                } else {
                    decorView.y = contentViewY
                }
            }
        }
    }

    private val requestResult = object: PermissionUtils.RequestResult {
        override fun onGranted(permissions: MutableList<String>?) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(requireActivity().packageManager) != null) {
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
                launchType = TAKE_PHOTO
            }
        }

        override fun onDenied(permissions: MutableList<String>?) {

        }

        override fun onDeniedNotAsk(permissionSettings: PermissionSettings?) {
            activity?.let { PermissionDialog.show(it, permissionSettings) }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_CODE || data == null) {
            return
        }

        //获取选择器返回的数据
        val imagesList = data.getStringArrayListExtra(ImageSelector.SELECT_RESULT)
        val cacheList = ArrayList<File>()
        imagesList?.forEach { path ->
            if (!selectFileList.contains(path)) {
                selectFileList.add(path)
                val file = File(path)
                val uri = file.toUri()
                val finalPath = buildPathByUri(uri)
                checkUploadFile(uri, finalPath, FROM_OPEN_ALBUM)
                cacheList.add(File(finalPath))
            }
        }
        uploadUserImg(cacheList)
        LOG.d(TAG, "result imagesList ${imagesList?.size}")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        itemBinding.feedbackDescribeEdit.clear()
        viewModel.getUploadFeedbackStatus().value = null
        viewModel.getUploadIssueStatus().value = null
        viewModel.getUploadImageInfo().value = null
        activity?.let { KeyboardUtils.unregisterSoftInputChangedListener(it.window) }
    }

    override fun getCustomTitle(): Int {
        return 0
    }

    override fun initItemBinding(parent: ViewGroup): FeedbackCommonLayoutBinding {
        return FeedbackCommonLayoutBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
    }
}