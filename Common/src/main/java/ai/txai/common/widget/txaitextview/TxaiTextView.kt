package ai.txai.common.widget.txaitextview

import ai.txai.common.R
import android.content.Context
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.text.TextUtils
import android.util.AttributeSet

class TxaiTextView: androidx.appcompat.widget.AppCompatTextView {
    companion object {
        private const val BOLD = 0x00000001
        private const val ROBOT = 0x00000010
        private const val NO_SPACE = 0x00000100
        private const val MARQUEE = 0x00001000
        //BOLD and SEMIBOLD choose one, if both are chosen, SEMIBOLD shall work
        private const val SEMIBOLD = 0x00010000
    }

    private var refreshMeasure = false
    private var txaiTextStyle = -1
    private val styleMap by lazy { createStyleMap() }

    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        val typeArray = context.obtainStyledAttributes(attrs, R.styleable.TxaiTextView)
        txaiTextStyle = typeArray.getInt(R.styleable.TxaiTextView_txaiFontStyle, -1)
        initText()
        typeArray.recycle()
    }

    private fun initText() {
        styleMap.forEach {
            operationTextByStyle(it.key, it.value)
        }
    }

    private fun createStyleMap() = mutableMapOf<Int, () -> Unit>().apply {
        put(BOLD) {
            letterSpacing = 0.01f
            paint.style = Paint.Style.FILL_AND_STROKE
            paint.strokeWidth = 2f
            val fontFamily = "sans-serif"
            val typeFace = Typeface.create(fontFamily, Typeface.NORMAL)
            typeface = typeFace
            invalidate()
        }
        put(ROBOT) {
            paint.style = Paint.Style.FILL_AND_STROKE
            paint.strokeWidth = 2f
            val assets = context.assets
            val typeface = Typeface.createFromAsset(assets, "fonts/Roboto-Regular.ttf")
            setTypeface(typeface)
            invalidate()
        }
        put(MARQUEE) {
            //设置单行
            setSingleLine()
            //设置Ellipsize
            ellipsize = TextUtils.TruncateAt.MARQUEE
            //获取焦点
            isFocusable = true
            //走马灯的重复次数，-1代表无限重复
            marqueeRepeatLimit = -1
            //强制获得焦点
            isFocusableInTouchMode = true
            setHorizontallyScrolling(true)
            isSelected = true
        }
        put(SEMIBOLD) {
            paint.style = Paint.Style.FILL_AND_STROKE
            val fontFamily = "sans-serif-medium"
            val typeFace = Typeface.create(fontFamily, Typeface.NORMAL)
            typeface = typeFace
            invalidate()
        }
    }

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        if (!containsStyle(MARQUEE)) {
            super.onFocusChanged(focused, direction, previouslyFocusedRect)
            return
        }

        if (focused) {
            super.onFocusChanged(focused, direction, previouslyFocusedRect)
        }
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        if (!containsStyle(MARQUEE)) {
            super.onWindowFocusChanged(hasWindowFocus)
            return
        }

        if (hasWindowFocus) {
            super.onWindowFocusChanged(hasWindowFocus)
        }
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        super.setText(text, type)
        // 每次文本内容改变时，需要测量两次，确保计算的高度没有问题
        operationTextByStyle(NO_SPACE) {
            refreshMeasure = true
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        operationTextByStyle(NO_SPACE) {
            removeSpace(widthMeasureSpec, heightMeasureSpec)
        }
    }

    private fun removeSpace(width: Int, height: Int) {
        var paddingTop = 0
        val linesText = getLinesText()
        val paint = paint
        val rect = Rect()
        val text = linesText[0] ?: return
        paint.getTextBounds(text, 0, text.length, rect)
        val fontMetricsInt = Paint.FontMetricsInt()
        paint.getFontMetricsInt(fontMetricsInt)
        paddingTop = fontMetricsInt.top - rect.top

        // 设置TextView向上的padding (小于0, 即把TextView文本内容向上移动)
        setPadding(
            leftPaddingOffset,
            paddingTop + topPaddingOffset + 2,
            rightPaddingOffset,
            bottomPaddingOffset
        )
        val endText = linesText[linesText.size - 1]
        endText?.let { paint.getTextBounds(it, 0, it.length, rect) }

        // 再减去最后一行文本的底部空白，得到的就是TextView内容上线贴边的的高度，到达消除文本上下留白的问题
        setMeasuredDimension(
            measuredWidth, measuredHeight - (fontMetricsInt.bottom - rect.bottom) + 2
        )
        if (refreshMeasure) {
            refreshMeasure = false
            measure(width, height)
        }
    }

    private fun getLinesText(): Array<String?> {
        var start = 0
        var end = 0
        val texts = arrayOfNulls<String>(lineCount)
        val text = text.toString()
        val layout = layout
        for (i in 0 until lineCount) {
            end = layout.getLineEnd(i)
            val line = text.substring(start, end) //指定行的内容
            start = end
            texts[i] = line
        }
        return texts
    }

    private fun operationTextByStyle(style: Int, operation: (() -> Unit)?) {
        val contains = containsStyle(style)
        if (contains) operation?.invoke()
    }

    private fun containsStyle(style: Int) = (txaiTextStyle and style) != 0
}