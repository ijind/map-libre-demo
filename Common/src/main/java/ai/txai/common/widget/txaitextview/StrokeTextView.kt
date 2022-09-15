package ai.txai.common.widget.txaitextview

import ai.txai.common.R
import ai.txai.commonview.databinding.CommonviewStrokeTextViewLayoutBinding
import android.content.Context
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat

class StrokeTextView: ConstraintLayout {
    private lateinit var binding: CommonviewStrokeTextViewLayoutBinding

    constructor(context: Context, attrs: AttributeSet): this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    private fun init(context: Context, attributeSet: AttributeSet) {
        val view = inflate(context, R.layout.commonview_stroke_text_view_layout, this)
        binding = CommonviewStrokeTextViewLayoutBinding.bind(view)
        val typeArray = context.obtainStyledAttributes(attributeSet, R.styleable.StrokeTextView)
        val strokeTv = typeArray.getString(R.styleable.StrokeTextView_text)
        binding.belowTv.text = strokeTv ?: ""
        binding.aboveTv.text = strokeTv ?: ""

        val textSize = typeArray.getDimensionPixelOffset(R.styleable.StrokeTextView_textSize, 32)
        binding.belowTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize.toFloat())
        binding.aboveTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize.toFloat())

        val defaultColor = ResourcesCompat.getColor(resources, R.color.commonview_black_normal, null)
        val textColor = typeArray.getColor(R.styleable.StrokeTextView_textColor, defaultColor)
        binding.aboveTv.setTextColor(textColor)

        val defaultStrokeColor = ResourcesCompat.getColor(resources, R.color.white, null)
        val belowColor = typeArray.getColor(R.styleable.StrokeTextView_strokeColor, defaultStrokeColor)
        binding.belowTv.setTextColor(belowColor)

        val textWidth = typeArray.getFloat(R.styleable.StrokeTextView_tvFontWeight, 2f)
        val strokeWidth = typeArray.getFloat(R.styleable.StrokeTextView_tvStrokeWidth, 5f)
        val abovePaint = binding.aboveTv.paint
        abovePaint.style = Paint.Style.FILL_AND_STROKE
        abovePaint.strokeWidth = textWidth
        val belowPaint = binding.belowTv.paint
        belowPaint.style = Paint.Style.FILL_AND_STROKE
        belowPaint.strokeWidth = textWidth + strokeWidth
        invalidate()

        typeArray.recycle()
    }

    fun setText(text: String) {
        binding.belowTv.text = text
        binding.aboveTv.text = text
    }
}