package ai.txai.common.widget

import ai.txai.common.log.LOG
import ai.txai.common.manager.LifeCycleManager
import ai.txai.common.utils.AndroidUtils
import ai.txai.common.utils.DeviceUtils
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ScrollView
import android.widget.Scroller
import androidx.dynamicanimation.animation.DynamicAnimation
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class DragView: ScrollView {
    companion object {
        private const val TAG = "MapScrollView"
    }
    private var moveY = 0f
    private val screenHeight by lazy { resources.displayMetrics.heightPixels }
    private val screenWidth = resources.displayMetrics.widthPixels
    private val defaultPosition = screenHeight * 0.57f
    private val initY by lazy { height - measuredHeight.toFloat() }

    private var velocityTracker: VelocityTracker? = null


    constructor(context: Context): super(context) {
        initView(context)
    }

    constructor(context: Context, attributeSet: AttributeSet): super(context, attributeSet) {
        initView(context)
    }

    private fun initView(context: Context) {
        y = defaultPosition
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val view = getChildAt(0) as ViewGroup
        val count = view.childCount
        var totalHeight = 0
        for (i in 4 until count) {
            LOG.d(TAG, "on measure child height ${view.getChildAt(i).measuredHeight}}")
            totalHeight += view.getChildAt(i).measuredHeight
            val marginLayout = view.getChildAt(i).layoutParams as MarginLayoutParams
            totalHeight += marginLayout.topMargin
            totalHeight += marginLayout.bottomMargin
        }
        setMeasuredDimension(widthMeasureSpec, totalHeight)
        LOG.d(TAG, "on measure total height $totalHeight}")
    }

//    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
//        super.onLayout(changed, left, top, right, bottom)
//        var totalHeight = 0
//        val view = getChildAt(0) as ViewGroup
//        val count = view.childCount
//        for (i in 0 until count) {
//            val childView = view.getChildAt(i)
//            val marginLayout = view.getChildAt(i).layoutParams as MarginLayoutParams
//            childView.layout(childView.paddingLeft, totalHeight,
//                screenWidth, totalHeight + childView.measuredHeight)
//            totalHeight += childView.measuredHeight
//            totalHeight += marginLayout.topMargin
//            totalHeight += marginLayout.bottomMargin
//        }
//        LOG.d(TAG, "on layout total height $totalHeight}")
//    }

    private var dy = 0f
    private var isMove = false
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev == null) return super.dispatchTouchEvent(ev)

        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain()
        }
        velocityTracker!!.addMovement(ev)
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                moveY = ev.y
            }
            MotionEvent.ACTION_MOVE -> {
                dy = ev.y - moveY
                val curY = y + dy
                LOG.d(TAG, "cur y $curY")
                if (curY - initY < 0) return super.dispatchTouchEvent(ev)

                translationY = y + (ev.y - moveY)
                viewListener?.onTranslationChange(translationY)
            }
            MotionEvent.ACTION_UP -> {
                if (y > defaultPosition) {
                    val animation = ObjectAnimator.ofFloat(this,
                        "translationY", y, defaultPosition)
                    animation.duration = 300
                    animation.interpolator = AccelerateDecelerateInterpolator()
                    animation.start()
                    animation.addListener(object: AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator?) {
                            super.onAnimationEnd(animation)
                            viewListener?.onTranslationChange(0f)
                        }
                    })
                } else {
                    velocityTracker!!.computeCurrentVelocity(1000)
                    val velocityY = velocityTracker!!.yVelocity
                    val dis = abs(0.3 * velocityY)
                    var finalY = if (y - dis < 0 ) initY else max(initY, (y - dis).toFloat())
                    if (dy > 0) {
                        finalY = defaultPosition
                    } else {
                        if (finalY < screenHeight / 2) finalY = initY
                    }
                    val animation = ObjectAnimator.ofFloat(this,
                        "translationY", y, finalY)
                    animation.duration = 300
                    animation.interpolator = DecelerateInterpolator()
                    animation.start()
                }
                velocityTracker?.recycle()
                velocityTracker = null
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private var lastTime = 0L
    private var viewListener: OnViewStatusChangeListener? = null

    fun setViewStatusChangeListener(listener: OnViewStatusChangeListener) {
        viewListener = listener
    }

    fun defaultHeight(): Float {
        return screenHeight - defaultPosition
    }

    interface OnViewStatusChangeListener {
        fun onAlphaChanged(y: Float)

        fun onTranslationChange(translationY: Float)
    }
}