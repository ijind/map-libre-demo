package ai.txai.common.widget.txaiButton

import ai.txai.common.R
import ai.txai.common.utils.AndroidUtils
import ai.txai.common.utils.setDebounceClickListener
import ai.txai.commonview.databinding.CommonviewTxaiButtonLayoutBinding
import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.annotation.IntRange
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat

class TxaiButton: ConstraintLayout {
    private lateinit var binding: CommonviewTxaiButtonLayoutBinding
    private var buttonType = BUTTON_TYPE_POSITIVE
    private var buttonLevel = BUTTON_LEVEL_1
    private var buttonStyle = BUTTON_STYLE_1

    constructor(context: Context, attributeSet: AttributeSet): super(context, attributeSet) {
        init(context, attributeSet)
    }

    constructor(context: Context, attributeSet: AttributeSet, defStyle: Int) :
            super(context, attributeSet, defStyle) {
        init(context, attributeSet)
    }

    private fun init(context: Context, attributeSet: AttributeSet) {
        val view = inflate(context, R.layout.commonview_txai_button_layout, this)
        binding = CommonviewTxaiButtonLayoutBinding.bind(view)
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.TxaiButton)
        initClickable(typedArray)
        buttonType = typedArray.getInteger(R.styleable.TxaiButton_button_type, BUTTON_TYPE_POSITIVE)
        buttonStyle = typedArray.getInteger(R.styleable.TxaiButton_button_style, BUTTON_STYLE_1)
        initButtonStyle()
        val buttonHeight = initButtonLevelAndText(context, typedArray)
        initButtonView(typedArray, buttonHeight)
        typedArray.recycle()
    }

    private fun initClickable(typedArray: TypedArray) {
        val clickable = typedArray.getBoolean(R.styleable.TxaiButton_android_clickable, true)
        if (!clickable) {
            binding.mainNegativeLayout.isClickable = false
            binding.mainPositiveLayout.isClickable = false
        }
    }

    private fun initButtonLevelAndText(context: Context, typedArray: TypedArray): Int {
        // Default value 1: means button's height is 44dp matching level_1
        buttonLevel = typedArray.getInteger(R.styleable.TxaiButton_button_level, BUTTON_LEVEL_1)
        var buttonHeight = AndroidUtils.dip2px(context, 44f)
        var textSize = resources.getDimensionPixelSize(R.dimen.global_text_size_level_5)
        var negativeTextColor = ResourcesCompat.getColor(resources, R.color.commonview_orange_00, null)
        when(buttonLevel) {
            BUTTON_LEVEL_2 -> {
                buttonHeight = AndroidUtils.dip2px(context, 36f)
                textSize = resources.getDimensionPixelSize(R.dimen.global_text_size_level_6)
            }
            BUTTON_LEVEL_3 -> {
                binding.mainNegativeLayout.setBackgroundResource(R.drawable.commonview_part_negative_click_bg)
                binding.mainPositiveLayout.setBackgroundResource(R.drawable.commonview_part_positive_click_bg)
                buttonHeight = AndroidUtils.dip2px(context, 26f)
                textSize = resources.getDimensionPixelSize(R.dimen.global_text_size_level_7)
                negativeTextColor = ResourcesCompat.getColor(resources, R.color.commonview_black_normal, null)
            }
        }
        binding.mainPositiveBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize.toFloat())
        binding.mainNegativeBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize.toFloat())
        binding.mainNegativeBtn.setTextColor(negativeTextColor)

        return buttonHeight
    }

    private fun initButtonStyle() {
        when(buttonStyle) {
            BUTTON_STYLE_2 -> {
                changeButtonWeight(1f)
            }
            BUTTON_STYLE_3 -> {
                changeButtonWeight(2f)
            }
            else -> {
                if (buttonType == BUTTON_TYPE_NEGATIVE) {
                    onlyNegativeVisible()
                } else {
                    onlyPositiveVisible()
                }
            }
        }
    }

    private fun initButtonView(typedArray: TypedArray, height: Int) {
        val negativeIconId = typedArray.getResourceId(R.styleable.TxaiButton_negativeIcon, -1)
        if (negativeIconId > 0) {
            val leftIcon = ResourcesCompat.getDrawable(resources, negativeIconId, null)
            leftIcon?.let { it.setBounds(0, 0, it.minimumWidth, it.minimumHeight) }
            binding.mainNegativeBtn.setCompoundDrawables(leftIcon, null, null, null)
        }
        val defaultNegativeText = resources.getString(R.string.commonview_cancel)
        val negativeText = typedArray.getString(R.styleable.TxaiButton_negativeText) ?: defaultNegativeText
        binding.mainNegativeBtn.text = negativeText
        binding.mainNegativeLayout.minimumHeight = height

        val positiveIconId = typedArray.getResourceId(R.styleable.TxaiButton_positiveIcon, -1)
        if (positiveIconId > 0) {
            val leftIcon = ResourcesCompat.getDrawable(resources, positiveIconId, null)
            leftIcon?.let { it.setBounds(0, 0, it.minimumWidth, it.minimumHeight) }
            binding.mainPositiveBtn.setCompoundDrawables(leftIcon, null, null, null)
        }
        val defaultPositiveText = resources.getString(R.string.commonview_confirm)
        val positiveText = typedArray.getString(R.styleable.TxaiButton_positiveText) ?: defaultPositiveText
        binding.mainPositiveBtn.text = positiveText
        binding.mainPositiveLayout.minimumHeight = height
    }

    private fun changeButtonWeight(positiveWeight: Float, negativeWeight: Float = 1f) {
        val lpNegative = binding.mainNegativeLayout.layoutParams as LayoutParams
        lpNegative.horizontalWeight = negativeWeight
        binding.mainNegativeLayout.layoutParams = lpNegative

        val lpPositive = binding.mainPositiveLayout.layoutParams as LayoutParams
        lpPositive.horizontalWeight = positiveWeight
        binding.mainPositiveLayout.layoutParams = lpPositive
    }

    private fun operationByButtonVisibility(
        negativeVisibleOperation: () -> Unit,
        positiveVisibleOperation: () -> Unit) {
        if (binding.mainNegativeLayout.visibility == VISIBLE) {
            negativeVisibleOperation.invoke()
        }

        if (binding.mainPositiveLayout.visibility == VISIBLE) {
            positiveVisibleOperation.invoke()
        }
    }

    private fun onlyNegativeVisible() {
        binding.mainPositiveLayout.visibility = View.GONE
        updateNegativeMarginEnd(0f)
    }

    private fun onlyPositiveVisible() {
        binding.mainNegativeLayout.visibility = View.GONE
        updatePositiveMarginStart(0f)
    }

    private fun updateNegativeMarginEnd(marginEnd: Float) {
        val lp = binding.mainNegativeLayout.layoutParams as MarginLayoutParams
        lp.marginEnd = AndroidUtils.dip2px(context, marginEnd)
        binding.mainNegativeLayout.layoutParams = lp
    }

    private fun updatePositiveMarginStart(marginStart: Float) {
        val lp = binding.mainPositiveLayout.layoutParams as MarginLayoutParams
        lp.marginStart = AndroidUtils.dip2px(context, marginStart)
        binding.mainPositiveLayout.layoutParams = lp
    }

    fun updateButtonStyle(@IntRange(from = 1, to = 3) style: Int) {
        buttonStyle = style
        initButtonStyle()
    }

    fun setNegativeEnable(enable: Boolean) {
        binding.mainNegativeLayout.isEnabled = enable
        var color = ResourcesCompat.getColor(resources, R.color.commonview_grey_c3, null)
        if (enable) {
            color = ResourcesCompat.getColor(resources, R.color.commonview_orange_00, null)
            if (buttonLevel == BUTTON_LEVEL_3) {
                color = ResourcesCompat.getColor(resources, R.color.commonview_black_normal, null)
            }
        }
        binding.mainNegativeBtn.setTextColor(color)
    }

    fun setPositiveEnable(enable: Boolean) {
        binding.mainPositiveLayout.isEnabled = enable
        var color = ResourcesCompat.getColor(resources, R.color.white, null)
        if (enable) {
            color = ResourcesCompat.getColor(resources, R.color.commonview_black_normal, null)
        }
        binding.mainPositiveBtn.setTextColor(color)
    }

    //如果在只显示一个按钮的情况下（也就是样式为single_button）则可通过该函数
    //设置显示状态为visible的按钮的禁用状态，无需考虑是positive还是negative
    fun setVisibleButtonEnable(enable: Boolean) {
        operationByButtonVisibility({ setNegativeEnable(enable) }, { setPositiveEnable(enable) })
    }

    // for kotlin
    fun setNegativeClickListener(negativeOperation: () -> Unit) {
        binding.mainNegativeLayout.setDebounceClickListener {
            negativeOperation.invoke()
        }
    }

    // for java
    fun setNegativeClickListener(negativeClickListener: OnClickListener) {
        binding.mainNegativeLayout.setDebounceClickListener {
            negativeClickListener.onClick(it)
        }
    }

    // for kotlin
    fun setPositiveClickListener(positiveOperation: () -> Unit) {
        binding.mainPositiveLayout.setDebounceClickListener {
            positiveOperation.invoke()
        }
    }

    // for java
    fun setPositiveClickListener(positiveClickListener: OnClickListener) {
        binding.mainPositiveLayout.setDebounceClickListener {
            positiveClickListener.onClick(it)
        }
    }

    //如果在只显示一个按钮的情况下（也就是样式为single_button）则可通过该函数
    //设置显示状态为visible的按钮的点击事件，无需考虑是positive还是negative
    fun setVisibleButtonClickListener(operationClick: () -> Unit) {
        operationByButtonVisibility({ setNegativeClickListener { operationClick.invoke() } },
            { setPositiveClickListener { operationClick.invoke() } })
    }

    // for java
    fun setVisibleButtonClickListener(clickListener: OnClickListener) {
        operationByButtonVisibility({ setNegativeClickListener(clickListener) },
            { setPositiveClickListener(clickListener) })
    }

    fun setNegativeText(resId: Int) {
        setNegativeText(resources.getString(resId))
    }

    fun setNegativeText(negativeText: String) {
        binding.mainNegativeBtn.text = negativeText
    }

    fun setPositiveText(resId: Int) {
        setPositiveText(resources.getString(resId))
    }

    fun setPositiveText(positiveText: String) {
        binding.mainPositiveBtn.text = positiveText
    }

    fun setNegativeVisibility(visible: Boolean) {
        binding.mainNegativeLayout.visibility = if (visible) VISIBLE else GONE
        if (buttonStyle != BUTTON_STYLE_1) {
            if (!visible) {
                updatePositiveMarginStart(0f)
            }
        } else {
            if (visible && buttonType == BUTTON_TYPE_POSITIVE) {
                updateNegativeMarginEnd(5f)
                updatePositiveMarginStart(5f)
            }
        }
    }

    fun setPositiveVisibility(visible: Boolean) {
        binding.mainPositiveLayout.visibility = if (visible) VISIBLE else GONE
        if (buttonStyle != BUTTON_STYLE_1) {
            if (!visible) {
                updateNegativeMarginEnd(0f)
            }
        } else {
            if (visible && buttonType == BUTTON_TYPE_NEGATIVE) {
                updateNegativeMarginEnd(5f)
                updatePositiveMarginStart(5f)
            }
        }
    }
}