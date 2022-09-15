package ai.txai.common.countrycode

import ai.txai.common.R
import android.view.View
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import androidx.core.content.res.ResourcesCompat
import android.view.MotionEvent
import androidx.annotation.NonNull


class SideBar: View {
    companion object {
        val sAlphabet = arrayOf(
            "#", "A", "B", "C", "D", "E", "F", "G", "H", "I",
            "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S",
            "T", "U", "V", "W", "X", "Y", "Z")
    }

    private var choose = -1

    private var touchingLetterChangedListener: OnTouchingLetterChangedListener? = null

    private val letterPaint by lazy { Paint() }
    private val chooseColor by lazy { ResourcesCompat.
    getColor(resources, R.color.commonview_orange_00, null) }
    private val unChooseColor by lazy { ResourcesCompat.
    getColor(resources, R.color.commonview_grey_99, null) }
    private val textSize by lazy { resources.getDimensionPixelSize(
        R.dimen.global_text_size_level_8) }

    constructor(context: Context): super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?):super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int):
            super(context, attrs, defStyle) {
        init(context)
    }

    private fun init(context: Context) {
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val height = height
        val width = width
        val singleHeight: Int = height / sAlphabet.size
        for (i in sAlphabet.indices) {
            letterPaint.color = unChooseColor
            //            paint.setTypeface(Typeface.DEFAULT_BOLD);
            letterPaint.isAntiAlias = true
            letterPaint.textSize = textSize.toFloat()
            if (i == choose) {
                letterPaint.textSize = 24f
                letterPaint.color = chooseColor
                letterPaint.isFakeBoldText = true
            }
            val xpos: Float = width / 2 - letterPaint.measureText(sAlphabet[i]) / 2
            val ypos = (i + 1) * singleHeight.toFloat()
            canvas.drawText(sAlphabet[i], xpos, ypos, letterPaint)
            letterPaint.reset()
        }
    }

    override fun dispatchTouchEvent(@NonNull event: MotionEvent): Boolean {
        val action = event.action
        val y = event.y
        val listener = touchingLetterChangedListener
        val c = (y / height * sAlphabet.size).toInt()
        when (action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> if (c >= 0 && c < sAlphabet.size) {
                //toastChooseDialog(c);
                listener?.onTouchingLetterChanged(c)
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> invalidate()
            else -> {}
        }
        return true
    }

    fun setOnTouchingLetterChangedListener(onTouchingLetterChangedListener:
                                           OnTouchingLetterChangedListener) {
        touchingLetterChangedListener = onTouchingLetterChangedListener
    }

    interface OnTouchingLetterChangedListener {
        fun onTouchingLetterChanged(sectionIndex: Int)
    }
}