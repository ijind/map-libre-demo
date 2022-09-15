package ai.txai.common.widget

import ai.txai.common.R
import android.content.Context
import android.util.AttributeSet
import android.widget.ScrollView

class CustomScrollView: ScrollView {
    private var maxHeight: Int = 200

    constructor(context: Context): super(context)

    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        initialize(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr) {
                initialize(context, attrs)
            }

    private fun initialize(context: Context, attrs: AttributeSet) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomScrollView)
        maxHeight = typedArray.getLayoutDimension(R.styleable.CustomScrollView_custom_maxHeight,
            maxHeight)
        typedArray.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var height = heightMeasureSpec
        if (maxHeight > 0) {
            height = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST)
        }
        super.onMeasure(widthMeasureSpec, height)
    }
}