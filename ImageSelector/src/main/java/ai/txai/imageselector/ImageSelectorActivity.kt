package ai.txai.imageselector

import ai.txai.common.mvvm.BaseMvvmActivity
import ai.txai.common.mvvm.BaseViewModel
import ai.txai.common.utils.setDebounceClickListener
import ai.txai.imageselector.adapter.FolderAdapter
import ai.txai.imageselector.adapter.ImageAdapter
import ai.txai.imageselector.databinding.ImageSelectorImageSelectViewBinding
import ai.txai.imageselector.entry.Folder
import ai.txai.imageselector.entry.Image
import ai.txai.imageselector.entry.RequestConfig
import ai.txai.imageselector.model.ImageModel
import ai.txai.imageselector.utils.*
import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.provider.Settings
import android.view.KeyEvent
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.EnvironmentCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ImageSelectorActivity:
    BaseMvvmActivity<ImageSelectorImageSelectViewBinding, BaseViewModel>() {
    companion object {
        private const val PERMISSION_WRITE_EXTERNAL_REQUEST_CODE = 0x00000011
        private const val PERMISSION_CAMERA_REQUEST_CODE = 0x00000012

        private const val CAMERA_REQUEST_CODE = 0x00000010
    }

    private var adapter: ImageAdapter? = null
    private var layoutManager: GridLayoutManager? = null

    private var folders: ArrayList<Folder>? = null
    private var folder: Folder? = null
    private var applyLoadImage = false
    private var applyCamera = false

    private var cameraUri: Uri? = null
    private var cameraImagePath: String? = null
    private var takeTime: Long = 0

    private var isShowTime = false
    private var isSingle = false
    private var canPreview = true
    private var maxCount = 0

    private var useCamera = true
    private var onlyTakePhoto = false

    private val hideHandler = Handler()
    private val hideTime = Runnable { hideTime() }

    //用于接收从外面传进来的已选择的图片列表。当用户原来已经有选择过图片，现在重新打开选择器，允许用
    // 户把先前选过的图片传进来，并把这些图片默认为选中状态。
    private var selectedImages: ArrayList<String>? = null

    private lateinit var activityLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent
        val config: RequestConfig? = intent.getParcelableExtra(ImageSelector.KEY_CONFIG)
        config?.let {
            maxCount = it.maxSelectCount
            isSingle = it.isSingle
            canPreview = it.canPreview
            useCamera = it.useCamera
            selectedImages = it.selected
            onlyTakePhoto = it.onlyTakePhoto
        }
        activityLauncher = initActivityLauncher()
        if (onlyTakePhoto) {
            // 仅拍照
            checkPermissionAndCamera()
        } else {
            initListener()
            initImageList()
            checkPermissionAndLoadImages()
            setSelectImageCount(0)
        }
    }

    private fun initListener() {
        binding.imageSelectorBackImg.setDebounceClickListener { finish() }
        binding.previewTv.setDebounceClickListener {
            val images = ArrayList<Image>()
            images.addAll(adapter!!.selectImages)
            toPreviewActivity(images, 0)
        }

        binding.imageSelectorConfirm.setPositiveClickListener { confirm() }

        binding.goAlbumsLayout.setDebounceClickListener {
            val intent = Intent(this, AlbumsActivity::class.java)
            activityLauncher.launch(intent)
        }

        binding.rvImage.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                changeTime()
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                changeTime()
            }
        })
    }

    private fun initActivityLauncher(): ActivityResultLauncher<Intent> {
        val activityLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.let { intent ->
                    val images = intent.
                    getParcelableArrayListExtra<Image>(ImageSelector.SELECT_FOLDER_RESULT)
                    val name = intent.getStringExtra(ImageSelector.SELECT_FOLDER_NAME)
                    if (images != null) {
                        val folder = Folder(name, images)
                        folder.isUseCamera = false
                        setFolder(folder)
                    }
                }
            }
        }

        return activityLauncher
    }

    /**
     * 初始化图片列表
     */
    private fun initImageList() {
        // 判断屏幕方向
        val configuration = resources.configuration
        layoutManager = if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            GridLayoutManager(this, 3)
        } else {
            GridLayoutManager(this, 5)
        }
        binding.rvImage.layoutManager = layoutManager
//        adapter = createAdapter(binding.rvImage)
        adapter = ImageAdapter(this, maxCount, isSingle, canPreview)
        binding.rvImage.adapter = adapter

        (binding.rvImage.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        if (!folders.isNullOrEmpty()) {
            setFolder(folders!![0])
        }

        adapter!!.setOnImageSelectListener(object: ImageAdapter.OnImageSelectListener {
            override fun onImageSelect(image: Image?, isSelect: Boolean, selectCount: Int) {
                setSelectImageCount(selectCount)
            }
        })

        adapter!!.setOnItemClickListener(object : ImageAdapter.OnItemClickListener {
            override fun onItemClick(image: Image?, position: Int) {
                toPreviewActivity(adapter!!.data, position)
            }

            override fun onCameraClick() {
                checkPermissionAndCamera()
            }
        })
    }

    /**
     * 设置选中的文件夹，同时刷新图片列表
     *
     * @param folder
     */
    private fun setFolder(folder: Folder?) {
        if (folder != null && adapter != null && folder != this.folder) {
            this.folder = folder
            binding.imageSelectorTitleTv.text = folder.name
            binding.rvImage.scrollToPosition(0)
            adapter!!.refresh(folder.images, folder.isUseCamera)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setSelectImageCount(count: Int) {
        if (count == 0) {
            binding.imageSelectorConfirm.setPositiveEnable(false)
            if (!isSingle && maxCount > 0) {
                val text = "${getString(R.string.image_selector_confirm)}(0/$maxCount)"
                binding.imageSelectorConfirm.setPositiveText(text)
            }
            binding.previewTv.isEnabled = false
            val prevColor = ResourcesCompat.getColor(resources, R.color.commonview_grey_c3, null)
            binding.previewTv.setTextColor(prevColor)
        } else {
            binding.imageSelectorConfirm.setPositiveEnable(true)
            val text = getString(R.string.image_selector_send) + "(" + count + ")"
            binding.imageSelectorConfirm.setPositiveText(text)
            binding.previewTv.isEnabled = true
            val prevColor = ResourcesCompat.getColor(resources, R.color.commonview_black_normal, null)
            binding.previewTv.setTextColor(prevColor)
            if (isSingle) {
                val confirmText = getString(R.string.image_selector_confirm)
                binding.imageSelectorConfirm.setPositiveText(confirmText)

            } else if (maxCount > 0) {
                val confirmText = getString(R.string.image_selector_confirm) +
                        "(" + count + "/" + maxCount + ")"
                binding.imageSelectorConfirm.setPositiveText(confirmText)
            }
        }
    }

    /**
     * 隐藏时间条
     */
    private fun hideTime() {
        if (isShowTime) {
            ObjectAnimator.ofFloat(binding.tvTime, "alpha", 1f, 0f).setDuration(300).start()
            isShowTime = false
        }
    }

    /**
     * 显示时间条
     */
    private fun showTime() {
        if (!isShowTime) {
            ObjectAnimator.ofFloat(binding.tvTime, "alpha", 0f, 1f).setDuration(300).start()
            isShowTime = true
        }
    }

    /**
     * 改变时间条显示的时间（显示图片列表中的第一个可见图片的时间）
     */
    private fun changeTime() {
        val firstVisibleItem = getFirstVisibleItem()
        val image = adapter!!.getFirstVisibleImage(firstVisibleItem)
        if (image != null) {
            val time = DateUtils.getImageTime(this, image.time)
            binding.tvTime.text = time
            showTime()
            hideHandler.removeCallbacks(hideTime)
            hideHandler.postDelayed(hideTime, 1500)
        }
    }

    private fun getFirstVisibleItem(): Int {
        return layoutManager!!.findFirstVisibleItemPosition()
    }

    private fun confirm() {
        if (adapter == null) {
            return
        }
        //因为图片的实体类是Image，而我们返回的是String数组，所以要进行转换。
        val selectImages = adapter!!.selectImages
        val images = ArrayList<String?>()
        for (image in selectImages) {
            images.add(image.path)
        }
        saveImageAndFinish(images, false)
    }

    private fun saveImageAndFinish(images: ArrayList<String?>, isCameraImage: Boolean) {
        //点击确定，把选中的图片通过Intent传给上一个Activity。
        setResult(images, isCameraImage)
        finish()
    }

    private fun setResult(images: ArrayList<String?>, isCameraImage: Boolean) {
        val intent = Intent()
        intent.putStringArrayListExtra(ImageSelector.SELECT_RESULT, images)
        intent.putExtra(ImageSelector.IS_CAMERA_IMAGE, isCameraImage)
        setResult(RESULT_OK, intent)
    }

    private fun toPreviewActivity(images: ArrayList<Image>?, position: Int) {
        if (images.isNullOrEmpty()) return

        PreviewActivity.openActivity(
            this, images,
            adapter!!.selectImages, isSingle, maxCount, position)
    }

    override fun onStart() {
        super.onStart()
        if (applyLoadImage) {
            applyLoadImage = false
            checkPermissionAndLoadImages()
        }
        if (applyCamera) {
            applyCamera = false
            checkPermissionAndCamera()
        }
    }

    /**
     * 处理图片预览页返回的结果
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @SuppressLint("NotifyDataSetChanged")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ImageSelector.RESULT_CODE) {
            if (data != null && data.getBooleanExtra(ImageSelector.IS_CONFIRM, false)) {
                //如果用户在预览页点击了确定，就直接把用户选中的图片返回给用户。
                confirm()
            } else {
                //否则，就刷新当前页面。
                adapter!!.notifyDataSetChanged()
                setSelectImageCount(adapter!!.selectImages.size)
            }
        } else if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                val images = ArrayList<String?>()
                var savePictureUri: Uri? = null
                if (VersionUtils.isAndroidQ()) {
                    savePictureUri = cameraUri
                    images.add(UriUtils.getPathForUri(this, cameraUri))
                } else {
                    savePictureUri = Uri.fromFile(File(cameraImagePath))
                    images.add(cameraImagePath)
                }
                ImageUtil.savePicture(this, savePictureUri, takeTime)
                saveImageAndFinish(images, true)
            } else {
                if (onlyTakePhoto) {
                    finish()
                }
            }
        }
    }

    /**
     * 横竖屏切换处理
     *
     * @param newConfig
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (layoutManager != null && adapter != null) {
            //切换为竖屏
            if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                layoutManager!!.spanCount = 3
            } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                layoutManager!!.spanCount = 5
            }
            adapter!!.notifyDataSetChanged()
        }
    }

    /**
     * 检查权限并加载SD卡里的图片。
     */
    private fun checkPermissionAndLoadImages() {
        if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
//            Toast.makeText(this, "没有图片", Toast.LENGTH_LONG).show();
            return
        }
        val hasWriteExternalPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (hasWriteExternalPermission == PackageManager.PERMISSION_GRANTED) {
            //有权限，加载图片。
            loadImageForSDCard()
        } else {
            //没有权限，申请权限。
            ActivityCompat.requestPermissions(
                this@ImageSelectorActivity,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                PERMISSION_WRITE_EXTERNAL_REQUEST_CODE
            )
        }
    }

    /**
     * 检查权限并拍照。
     */
    private fun checkPermissionAndCamera() {
        val hasCameraPermission =
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val hasWriteExternalPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (hasCameraPermission == PackageManager.PERMISSION_GRANTED
            && hasWriteExternalPermission == PackageManager.PERMISSION_GRANTED
        ) {
            //有调起相机拍照。
            openCamera()
        } else {
            //没有权限，申请权限。
            ActivityCompat.requestPermissions(
                this@ImageSelectorActivity,
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                PERMISSION_CAMERA_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_WRITE_EXTERNAL_REQUEST_CODE) {
            if (grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                //允许权限，加载图片。
                loadImageForSDCard()
            } else {
                //拒绝权限，弹出提示框。
                showExceptionDialog(true)
            }
        } else if (requestCode == PERMISSION_CAMERA_REQUEST_CODE) {
            if (grantResults.size > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                //允许权限，有调起相机拍照。
                openCamera()
            } else {
                //拒绝权限，弹出提示框。
                showExceptionDialog(false)
            }
        }
    }

    /**
     * 发生没有权限等异常时，显示一个提示dialog.
     */
    private fun showExceptionDialog(applyLoad: Boolean) {
        AlertDialog.Builder(this)
            .setCancelable(false)
            .setTitle(R.string.image_selector_hint)
            .setMessage(R.string.image_selector_permissions_hint)
            .setNegativeButton(R.string.image_selector_cancel,
                DialogInterface.OnClickListener { dialog, _ ->
                    dialog.cancel()
                    finish()
                }).setPositiveButton(R.string.image_selector_confirm,
                DialogInterface.OnClickListener { dialog, _ ->
                    dialog.cancel()
                    startAppSettings()
                    if (applyLoad) {
                        applyLoadImage = true
                    } else {
                        applyCamera = true
                    }
                }).show()
    }

    /**
     * 从SDCard加载图片。
     */
    private fun loadImageForSDCard() {
        ImageModel.loadImageForSDCard(this) { folders ->
            this.folders = folders
            runOnUiThread {
                if (this.folders != null && this.folders!!.isNotEmpty()) {
                    this.folders!![0].isUseCamera = useCamera
                    setFolder(this.folders!![0])
                    if (selectedImages != null && adapter != null) {
                        adapter!!.setSelectedImages(selectedImages)
                        selectedImages = null
                        setSelectImageCount(adapter!!.selectImages.size)
                    }
                }
            }
        }
    }

    /**
     * 调起相机拍照
     */
    private fun openCamera() {
        val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (captureIntent.resolveActivity(packageManager) != null) {
            var photoFile: File? = null
            var photoUri: Uri? = null
            if (VersionUtils.isAndroidQ()) {
                photoUri = createImagePathUri()
            } else {
                try {
                    photoFile = createImageFile()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                if (photoFile != null) {
                    cameraImagePath = photoFile.absolutePath
                    photoUri = if (VersionUtils.isAndroidN()) {
                        //通过FileProvider创建一个content类型的Uri
                        FileProvider.getUriForFile(
                            this,
                            "$packageName.imageSelectorProvider", photoFile
                        )
                    } else {
                        Uri.fromFile(photoFile)
                    }
                }
            }
            cameraUri = photoUri
            if (photoUri != null) {
                captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                captureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                startActivityForResult(captureIntent, CAMERA_REQUEST_CODE)
                takeTime = System.currentTimeMillis()
            }
        }
    }

    /**
     * 创建一条图片地址uri,用于保存拍照后的照片
     *
     * @return 图片的uri
     */
    fun createImagePathUri(): Uri? {
        val status = Environment.getExternalStorageState()
        val timeFormatter = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val time = System.currentTimeMillis()
        val imageName = timeFormatter.format(Date(time))
        // ContentValues是我们希望这条记录被创建时包含的数据信息
        val values = ContentValues(2)
        values.put(MediaStore.Images.Media.DISPLAY_NAME, imageName)
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        // 判断是否有SD卡,优先使用SD卡存储,当没有SD卡时使用手机存储
        return if (status == Environment.MEDIA_MOUNTED) {
            contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        } else {
            contentResolver.insert(MediaStore.Images.Media.INTERNAL_CONTENT_URI, values)
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = String.format("JPEG_%s.jpg", timeStamp)
        val storageDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES
        )
        if (!storageDir.exists()) {
            storageDir.mkdir()
        }
        val tempFile = File(storageDir, imageFileName)
        return if (Environment.MEDIA_MOUNTED != EnvironmentCompat.getStorageState(tempFile)) {
            null
        } else tempFile
    }

    /**
     * 启动应用的设置
     */
    private fun startAppSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        )
        intent.data = Uri.parse("package:$packageName")
        startActivity(intent)
    }

    override fun initViewBinding(): ImageSelectorImageSelectViewBinding {
        return ImageSelectorImageSelectViewBinding.inflate(layoutInflater)
    }
}