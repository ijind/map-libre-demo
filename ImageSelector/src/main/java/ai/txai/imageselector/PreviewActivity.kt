package ai.txai.imageselector

import ai.txai.common.mvvm.BaseMvvmActivity
import ai.txai.common.mvvm.BaseViewModel
import ai.txai.common.utils.AndroidUtils
import ai.txai.common.utils.ToastUtils
import ai.txai.imageselector.adapter.ImagePagerAdapter
import ai.txai.imageselector.databinding.ImageSelectorPreviewLayoutBinding
import ai.txai.imageselector.entry.Image
import ai.txai.imageselector.utils.DateUtils
import ai.txai.imageselector.utils.ImageSelector
import ai.txai.imageselector.utils.VersionUtils
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.core.content.res.ResourcesCompat
import androidx.viewpager.widget.ViewPager.OnPageChangeListener

class PreviewActivity: BaseMvvmActivity<ImageSelectorPreviewLayoutBinding, BaseViewModel>() {
    private lateinit var images: ArrayList<Image>
    private lateinit var selectImages: ArrayList<Image>
    private var isShowBar = true
    private var isSingle = false
    private var maxCount = 0
    private var selectDrawable = R.drawable.image_selector_selected_ic
    private var unSelectDrawable = R.drawable.image_selector_unselect_ic

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusBarFontStyle(false)

        if (VersionUtils.isAndroidP()) {
            //设置页面全屏显示
            val lp = window.attributes
            lp.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            window.attributes = lp
        }

        tempImages?.let { images = it } ?: run { images = ArrayList() }
        tempImages = null
        tempSelectImages?.let { selectImages = it } ?: run { selectImages = ArrayList() }
        tempSelectImages = null

        maxCount = intent.getIntExtra(ImageSelector.MAX_SELECT_COUNT, 0)
        isSingle = intent.getBooleanExtra(ImageSelector.IS_SINGLE, false)
        val curPosition = intent.getIntExtra(ImageSelector.POSITION, 0)
        if (isSelectContainImage(images[curPosition])
            && !images[curPosition].isFromPreview.toBoolean()) {
            binding.imgSelect.isEnabled = false
        }

        initListener()
        initViewPager()
        changeSelect(images[0])
        binding.vpImage.currentItem = intent.getIntExtra(ImageSelector.POSITION, 0)
    }

    private fun initListener() {
        binding.btnBack.setOnClickListener { finish() }
        binding.imgSelect.setOnClickListener { clickSelect() }
        binding.confirmBtn.setPositiveClickListener { finish() }
    }

    /**
     * 初始化ViewPager
     */
    private fun initViewPager() {
        val adapter = ImagePagerAdapter(this, images)
        binding.vpImage.adapter = adapter
        adapter.setOnItemClickListener(object: ImagePagerAdapter.OnItemClickListener {
            override fun onItemClick(position: Int, image: Image?) {
                if (isShowBar) {
                    hideBar()
                } else {
                    showBar()
                }
            }
        })
        binding.vpImage.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                changeSelect(images[position])
                binding.previewTitleTv.text = DateUtils.buildDate(images[position].time)
                binding.imgSelect.isEnabled = !(isSelectContainImage(images[position])
                        && !images[position].isFromPreview.toBoolean())
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    /**
     * 显示和隐藏状态栏
     *
     * @param show
     */
    private fun setStatusBarVisible(show: Boolean) {
        if (show) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        } else {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_FULLSCREEN)
        }
    }

    /**
     * 显示头部和底部栏
     */
    private fun showBar() {
        isShowBar = true
        setStatusBarVisible(true)
        val animator = ObjectAnimator.ofFloat(
            binding.rlTopBar, "translationY",
            binding.rlTopBar.translationY, 0f
        ).setDuration(300)

        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                super.onAnimationStart(animation)
                binding.rlTopBar.visibility = View.VISIBLE
            }
        })
        animator.start()
        showBottomBar()
    }

    private fun showBottomBar() {
        ObjectAnimator.ofFloat(binding.rlBottomBar, "translationY",
            binding.rlBottomBar.translationY, 0f)
            .setDuration(300).start()
    }

    private fun hideBottomBar() {
        ObjectAnimator.ofFloat(binding.rlBottomBar,
            "translationY", 0f, binding.rlBottomBar.height.toFloat())
            .setDuration(300).start()
    }
    /**
     * 隐藏头部和底部栏
     */
    private fun hideBar() {
        isShowBar = false
        val animator =
            ObjectAnimator.ofFloat(binding.rlTopBar, "translationY", 0f,
                -binding.rlTopBar.height.toFloat())
                .setDuration(300)
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                binding.rlTopBar.visibility = View.GONE
            }
        })
        animator.start()
        hideBottomBar()
        //添加延时，保证rlTopBar完全隐藏后再隐藏StatusBar。
        AndroidUtils.delayOperation(100) { setStatusBarVisible(false) }
    }

    private fun clickSelect() {
        val position = binding.vpImage.currentItem
        if (images.size <= position) return

        val image = images[position]
        if (isSelectContainImage(image)) {
            selectImages.remove(image)
        } else if (isSingle) {
            selectImages.clear()
            image.isFromPreview = "true"
            selectImages.add(image)
        } else if (selectImages.size >= maxCount) {
            val prompt = "Up to $maxCount pictures can be selected at a time"
            ToastUtils.show(prompt)
        } else if (maxCount <= 0 || selectImages.size < maxCount) {
            image.isFromPreview = "true"
            selectImages.add(image)
        }
        changeSelect(image)
    }

    @SuppressLint("SetTextI18n")
    private fun changeSelect(image: Image) {
        if (isSelectContainImage(image)) {
            binding.imgSelect.setImageResource(selectDrawable)
            binding.confirmBtn.setPositiveEnable(true)
            if (isSingle) {
                binding.confirmBtn.setPositiveText(R.string.image_selector_confirm)
            } else if (maxCount > 0) {
                val count = selectImages.indexOf(image) + 1
                val confirmText = getString(R.string.image_selector_confirm) +
                        "(" + count + "/" + maxCount + ")"
                binding.confirmBtn.setPositiveText(confirmText)
            }
        } else {
            binding.imgSelect.setImageResource(unSelectDrawable)
            binding.confirmBtn.setPositiveEnable(false)
            if (!isSingle && maxCount > 0) {
                val confirmText = "${getString(R.string.image_selector_confirm)}(${selectImages.size}/$maxCount)"
                binding.confirmBtn.setPositiveText(confirmText)
            }
        }
    }

    private fun isSelectContainImage(image: Image): Boolean {
        if (selectImages.isNullOrEmpty()) return false

        var isContain = false
        selectImages.forEach {
            if (it.path == image.path) isContain = true
        }

        return isContain
    }

    override fun finish() {
        //Activity关闭时，通过Intent把用户的操作(确定/返回)传给ImageSelectActivity。
        val intent = Intent()
        intent.putExtra(ImageSelector.IS_CONFIRM, false)
        setResult(ImageSelector.RESULT_CODE, intent)
        super.finish()
    }

    companion object {
        //tempImages和tempSelectImages用于图片列表数据的页面传输。
        //之所以不要Intent传输这两个图片列表，因为要保证两位页面操作的是同一个列表数据，同时可以避免数据量大时，
        // 用Intent传输发生的错误问题。
        private var tempImages: ArrayList<Image> ? = null
        private var tempSelectImages: ArrayList<Image> ? = null

        fun openActivity(
            activity: Activity, images: ArrayList<Image>,
            selectImages: ArrayList<Image>, isSingle: Boolean,
            maxSelectCount: Int, position: Int) {
            tempImages = images
            tempSelectImages = selectImages
            val intent = Intent(activity, PreviewActivity::class.java)
            intent.putExtra(ImageSelector.MAX_SELECT_COUNT, maxSelectCount)
            intent.putExtra(ImageSelector.IS_SINGLE, isSingle)
            intent.putExtra(ImageSelector.POSITION, position)
            activity.startActivityForResult(intent, ImageSelector.RESULT_CODE)
        }
    }

    override fun initViewBinding(): ImageSelectorPreviewLayoutBinding {
        return ImageSelectorPreviewLayoutBinding.inflate(layoutInflater)
    }
}