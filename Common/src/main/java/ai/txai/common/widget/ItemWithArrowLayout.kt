package ai.txai.common.widget

import ai.txai.common.R
import ai.txai.commonview.databinding.CommonviewItemWithArrowIcLayoutBinding
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewTreeObserver
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat

class ItemWithArrowLayout: ConstraintLayout {
    companion object {
        private const val ITEM_TOP = 1
        private const val ITEM_MIDDLE = 2
        private const val ITEM_BOTTOM = 3
    }

    private lateinit var binding: CommonviewItemWithArrowIcLayoutBinding

    constructor(context: Context, attributeSet: AttributeSet): super(context, attributeSet) {
        init(context, attributeSet)
    }

    constructor(context: Context, attributeSet: AttributeSet, defStyle: Int) :
            super(context, attributeSet, defStyle) {
        init(context, attributeSet)
    }

    private fun init(context: Context, attributeSet: AttributeSet) {
        val view = inflate(context, R.layout.commonview_item_with_arrow_ic_layout, this)
        binding = CommonviewItemWithArrowIcLayoutBinding.bind(view)
        val typeArray = context.obtainStyledAttributes(attributeSet, R.styleable.ItemWithArrowLayout)
        initItemContent(typeArray)
        initBackground(typeArray)
        initPadding(typeArray)
        initMargin(typeArray)
        typeArray.recycle()
    }

    private fun initItemContent(typedArray: TypedArray) {
        binding.itemTitleTv.text = typedArray.getString(
            R.styleable.ItemWithArrowLayout_itemTitle).toString()
        var titleSize = typedArray.getDimensionPixelOffset(
            R.styleable.ItemWithArrowLayout_itemTitleSize, -1).toFloat()
        if (titleSize < 0) {
            titleSize = resources.getDimensionPixelSize(R.dimen.global_text_size_level_5).toFloat()
        }
        binding.itemTitleTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleSize)

        val defaultTitleColor = ResourcesCompat.getColor(resources, R.color.commonview_black_normal, null)
        val colorTitle = typedArray.getColor(
            R.styleable.ItemWithArrowLayout_itemTitleColor, defaultTitleColor)
        binding.itemTitleTv.setTextColor(colorTitle)

        binding.itemContentPreTv.text = typedArray.getString(
            R.styleable.ItemWithArrowLayout_itemPreContent) ?: ""
        binding.itemContentPreTv.setTextSize(TypedValue.COMPLEX_UNIT_PX,
            typedArray.getDimensionPixelOffset(
                R.styleable.ItemWithArrowLayout_itemPreContentSize, 24).toFloat())

        val colorPreContent = typedArray.getColor(
            R.styleable.ItemWithArrowLayout_itemPreContentColor, Color.BLACK)
        binding.itemContentPreTv.setTextColor(colorPreContent)

        val preContentBgId = typedArray.getResourceId(
            R.styleable.ItemWithArrowLayout_itemPreContentBg, Color.TRANSPARENT)
        binding.itemContentPreTv.setBackgroundResource(preContentBgId)

        binding.itemContentTv.text = typedArray.getString(
            R.styleable.ItemWithArrowLayout_itemContent) ?: ""
        binding.itemContentTv.setTextSize(TypedValue.COMPLEX_UNIT_PX,
            typedArray.getDimensionPixelOffset(
                R.styleable.ItemWithArrowLayout_itemContentSize, 28).toFloat())

        val colorContent = typedArray.getColor(
            R.styleable.ItemWithArrowLayout_itemContentColor, Color.BLACK)
        binding.itemContentTv.setTextColor(colorContent)

        val resId = typedArray.getResourceId(
            R.styleable.ItemWithArrowLayout_itemContentBg, Color.TRANSPARENT)
        binding.itemContentTv.setBackgroundResource(resId)

        val arrowId = typedArray.getResourceId(
            R.styleable.ItemWithArrowLayout_arrowIc, R.mipmap.ic_open_arrow)
        binding.itemArrowIc.setImageResource(arrowId)

        val isArrowVisible = typedArray.getBoolean(R.styleable.ItemWithArrowLayout_arrowVisible,
            false)
        binding.itemArrowIc.visibility = if (isArrowVisible) View.VISIBLE else View.GONE

        val titleImg = typedArray.getResourceId(R.styleable.ItemWithArrowLayout_itemTitleImg, -1)
        if (titleImg > 0) {
            binding.titleIconImg.visibility = View.VISIBLE
            binding.titleIconImg.setImageResource(titleImg)
        }
        val lineVisible = typedArray.getBoolean(R.styleable.ItemWithArrowLayout_itemLineVisible, true)
        if (lineVisible) binding.itemBottomLine.visibility = View.VISIBLE
    }

    private fun initBackground(typedArray: TypedArray) {
        val bgStyle = typedArray.getInteger(R.styleable.ItemWithArrowLayout_backgroundStyle, ITEM_MIDDLE)
        val itemBgId = when(bgStyle) {
            ITEM_TOP -> { R.drawable.commonview_item_top_click_bg }
            ITEM_BOTTOM -> { R.drawable.commonview_item_bottom_click_bg }
            else -> { R.drawable.commonview_item_middle_click_bg }
        }
        binding.globalContentLayout.setBackgroundResource(itemBgId)
    }

    private fun initPadding(typedArray: TypedArray) {
        val left = typedArray.getDimensionPixelOffset(R.styleable.ItemWithArrowLayout_android_paddingLeft, 0)
        val start = typedArray.getDimensionPixelOffset(R.styleable.ItemWithArrowLayout_android_paddingStart, 0)
        val right = typedArray.getDimensionPixelOffset(R.styleable.ItemWithArrowLayout_android_paddingRight, 0)
        val end = typedArray.getDimensionPixelOffset(R.styleable.ItemWithArrowLayout_android_paddingEnd, 0)
        var finalLeft = left
        var finalRight = right
        if (start > left) {
            finalLeft = start
        }
        if (end > right) {
            finalRight = end
        }
        binding.itemContentLayout.setPadding(finalLeft, 0, finalRight, 0)
        binding.globalContentLayout.setPadding(0, 0, 0, 0)
    }

    private fun initMargin(typedArray: TypedArray) {
        val left = typedArray.getDimensionPixelOffset(R.styleable.ItemWithArrowLayout_android_layout_marginLeft, 0)
        val start = typedArray.getDimensionPixelOffset(R.styleable.ItemWithArrowLayout_android_layout_marginStart, 0)
        val right = typedArray.getDimensionPixelOffset(R.styleable.ItemWithArrowLayout_android_layout_marginRight, 0)
        val end = typedArray.getDimensionPixelOffset(R.styleable.ItemWithArrowLayout_android_layout_marginEnd, 0)
        var finalLeft = left
        var finalRight = right
        if (start > left) {
            finalLeft = start
        }
        if (end > right) {
            finalRight = end
        }

        binding.root.viewTreeObserver.addOnGlobalLayoutListener(object: ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.root.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val lpRoot = binding.globalContentLayout.layoutParams as MarginLayoutParams
                lpRoot.marginStart = 0
                lpRoot.marginEnd = 0
                binding.globalContentLayout.layoutParams = lpRoot

                val lpContent= binding.itemContentLayout.layoutParams as MarginLayoutParams
                lpContent.marginStart = finalLeft
                lpContent.marginEnd = finalRight
                binding.itemContentLayout.layoutParams = lpContent
            }
        })
    }

    fun setItemTitle(string: String) {
        binding.itemTitleTv.text = string
    }

    fun setRightContent(resId: Int) {

    }

    fun setRightContent(string: String) {
        binding.itemContentTv.text = string
    }

    fun setRightContentVisible(visible: Boolean) {
        binding.itemContentTv.visibility = if (visible) View.VISIBLE else View.GONE
    }

    fun setPreContentVisible(visible: Boolean) {
        binding.itemContentPreTv.visibility = if (visible) View.VISIBLE else View.GONE
    }

    fun setArrowIc(resId: Int) {
        binding.itemArrowIc.setImageResource(resId)
    }

    fun visibleBottomLine(visible: Boolean) {
        binding.itemBottomLine.visibility = View.GONE
        if (visible) {
            binding.itemBottomLine.visibility = View.VISIBLE
        }
    }
}