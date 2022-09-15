package ai.txai.common.widget.txaiimageview

import ai.txai.common.R
import ai.txai.common.utils.LogUtils
import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.AttributeSet
import androidx.annotation.DrawableRes

import androidx.annotation.ColorRes

import androidx.annotation.ColorInt
import androidx.core.content.res.ResourcesCompat
import java.lang.Exception


class CircleImageView: androidx.appcompat.widget.AppCompatImageView {
    companion object {
        private val SCALE_TYPE = ScaleType.CENTER_CROP
        private const val COLOR_DRAWABLE_DIMENSION = 2

        private const val DEFAULT_BORDER_WIDTH = 0
        private const val DEFAULT_BORDER_COLOR: Int = Color.BLACK
        private const val DEFAULT_FILL_COLOR: Int = Color.TRANSPARENT
        private const val DEFAULT_BORDER_OVERLAY = false
    }

    private val BITMAP_CONFIG = Bitmap.Config.ARGB_8888
    private val mDrawableRect: RectF = RectF()
    private val mBorderRect: RectF = RectF()

    private val shaderMatrix = Matrix()
    private val mBitmapPaint = Paint()
    private val mBorderPaint = Paint()
    private val mFillPaint = Paint()

    private var mBorderColor = DEFAULT_BORDER_COLOR
    private var borderWidth = DEFAULT_BORDER_WIDTH
    private var mFillColor = DEFAULT_FILL_COLOR

    private var bitmap: Bitmap? = null
    private var bitmapShader: BitmapShader? = null
    private var mBitmapWidth = 0
    private var mBitmapHeight = 0

    private var mDrawableRadius = 0f
    private var mBorderRadius = 0f

    private var mColorFilter: ColorFilter? = null

    private var mReady = false
    private var mSetupPending = false
    private var mBorderOverlay = false
    private var mDisableCircularTransformation = false

    constructor(context: Context): super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        initView(context, attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int)
            : super(context, attrs, defStyle) {
        initView(context, attrs, defStyle)
    }

    fun initView(context: Context, attrs: AttributeSet?, defStyle: Int) {
        val a: TypedArray =
            context.obtainStyledAttributes(attrs, R.styleable.CircleImageView, defStyle, 0)
        borderWidth = a.getDimensionPixelSize(
            R.styleable.CircleImageView_civ_border_width,
            DEFAULT_BORDER_WIDTH
        )
        mBorderColor =
            a.getColor(R.styleable.CircleImageView_civ_border_color, DEFAULT_BORDER_COLOR)
        mBorderOverlay =
            a.getBoolean(R.styleable.CircleImageView_civ_border_overlay, DEFAULT_BORDER_OVERLAY)
        mFillColor = a.getColor(R.styleable.CircleImageView_civ_fill_color, DEFAULT_FILL_COLOR)
        a.recycle()
        init()
    }

    private fun init() {
        super.setScaleType(SCALE_TYPE)
        mReady = true
        if (mSetupPending) {
            setup()
            mSetupPending = false
        }
    }

    override fun getScaleType(): ScaleType? {
        return SCALE_TYPE
    }

    override fun setScaleType(scaleType: ScaleType) {
        require(scaleType == SCALE_TYPE) { String.format("ScaleType %s not supported.", scaleType) }
    }

    override fun setAdjustViewBounds(adjustViewBounds: Boolean) {
        require(!adjustViewBounds) { "adjustViewBounds not supported." }
    }

    override fun onDraw(canvas: Canvas) {
        if (mDisableCircularTransformation) {
            super.onDraw(canvas)
            return
        }
        if (bitmap == null) {
            return
        }
        if (mFillColor != Color.TRANSPARENT) {
            canvas.drawCircle(
                mDrawableRect.centerX(),
                mDrawableRect.centerY(),
                mDrawableRadius,
                mFillPaint
            )
        }
        canvas.drawCircle(
            mDrawableRect.centerX(),
            mDrawableRect.centerY(),
            mDrawableRadius,
            mBitmapPaint
        )
        if (borderWidth > 0) {
            canvas.drawCircle(
                mBorderRect.centerX(),
                mBorderRect.centerY(),
                mBorderRadius,
                mBorderPaint
            )
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        setup()
    }

    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        super.setPadding(left, top, right, bottom)
        setup()
    }

    override fun setPaddingRelative(start: Int, top: Int, end: Int, bottom: Int) {
        super.setPaddingRelative(start, top, end, bottom)
        setup()
    }

    fun getBorderColor(): Int {
        return mBorderColor
    }

    fun setBorderColor(@ColorInt borderColor: Int) {
        if (borderColor == mBorderColor) {
            return
        }
        mBorderColor = borderColor
        mBorderPaint.color = mBorderColor
        invalidate()
    }

    fun setBorderColorResource(@ColorRes borderColorRes: Int) {
        val color = ResourcesCompat.getColor(context.resources, borderColorRes, null)
        setBorderColor(color)
    }

    /**
     * Return the color drawn behind the circle-shaped drawable.
     *
     * @return The color drawn behind the drawable
     *
     */
    @Deprecated("Fill color support is going to be removed in the future")
    fun getFillColor(): Int {
        return mFillColor
    }

    /**
     * Set a color to be drawn behind the circle-shaped drawable. Note that
     * this has no effect if the drawable is opaque or no drawable is set.
     *
     * @param fillColor The color to be drawn behind the drawable
     *
     */
    @Deprecated("Fill color support is going to be removed in the future")
    fun setFillColor(@ColorInt fillColor: Int) {
        if (fillColor == mFillColor) {
            return
        }
        mFillColor = fillColor
        mFillPaint.color = fillColor
        invalidate()
    }

    /**
     * Set a color to be drawn behind the circle-shaped drawable. Note that
     * this has no effect if the drawable is opaque or no drawable is set.
     *
     * @param fillColorRes The color resource to be resolved to a color and
     * drawn behind the drawable
     *
     */
    @Deprecated("Fill color support is going to be removed in the future")
    fun setFillColorResource(@ColorRes fillColorRes: Int) {
        setFillColor(context.resources.getColor(fillColorRes))
    }

    fun getBorderWidth(): Int {
        return borderWidth
    }

    fun setBorderWidth(borderWidth: Int) {
        if (borderWidth == borderWidth) {
            return
        }
        this.borderWidth = borderWidth
        setup()
    }

    fun isBorderOverlay(): Boolean {
        return mBorderOverlay
    }

    fun setBorderOverlay(borderOverlay: Boolean) {
        if (borderOverlay == mBorderOverlay) {
            return
        }
        mBorderOverlay = borderOverlay
        setup()
    }

    fun isDisableCircularTransformation(): Boolean {
        return mDisableCircularTransformation
    }

    fun setDisableCircularTransformation(disableCircularTransformation: Boolean) {
        if (mDisableCircularTransformation == disableCircularTransformation) {
            return
        }
        mDisableCircularTransformation = disableCircularTransformation
        initializeBitmap()
    }

    override fun setImageBitmap(bm: Bitmap?) {
        super.setImageBitmap(bm)
        initializeBitmap()
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        initializeBitmap()
    }

    override fun setImageResource(@DrawableRes resId: Int) {
        super.setImageResource(resId)
        initializeBitmap()
    }

    override fun setImageURI(uri: Uri?) {
        super.setImageURI(uri)
        initializeBitmap()
    }

    override fun setColorFilter(cf: ColorFilter) {
        if (cf === mColorFilter) {
            return
        }
        mColorFilter = cf
        applyColorFilter()
        invalidate()
    }

    override fun getColorFilter(): ColorFilter? {
        return mColorFilter
    }

    private fun applyColorFilter() {
        mBitmapPaint.colorFilter = mColorFilter
    }

    private fun getBitmapFromDrawable(drawable: Drawable?): Bitmap? {
        if (drawable == null) {
            LogUtils.d("CircleImage", "get drawable null")
            return null
        }
        return if (drawable is BitmapDrawable) {
            drawable.bitmap
        } else try {
            val bitmap: Bitmap = if (drawable is ColorDrawable) {
                Bitmap.createBitmap(COLOR_DRAWABLE_DIMENSION, COLOR_DRAWABLE_DIMENSION, BITMAP_CONFIG)
            } else {
                Bitmap.createBitmap(
                    drawable.intrinsicWidth,
                    drawable.intrinsicHeight, BITMAP_CONFIG) }
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun initializeBitmap() {
        bitmap = if (mDisableCircularTransformation) {
            null
        } else {
            getBitmapFromDrawable(drawable)
        }
        setup()
    }

    private fun setup() {
        if (!mReady) {
            mSetupPending = true
            return
        }
        if (width == 0 && height == 0) {
            return
        }
        LogUtils.d("CircleImage", "set up $bitmap")
        bitmap?.let {
            bitmapShader = BitmapShader(it, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            mBitmapPaint.isAntiAlias = true
            mBitmapPaint.shader = bitmapShader
            mBorderPaint.style = Paint.Style.STROKE
            mBorderPaint.isAntiAlias = true
            mBorderPaint.color = mBorderColor
            mBorderPaint.strokeWidth = borderWidth.toFloat()
            mFillPaint.style = Paint.Style.FILL
            mFillPaint.isAntiAlias = true
            mFillPaint.color = mFillColor
            mBitmapHeight = it.height
            mBitmapWidth = it.width
            mBorderRect.set(calculateBounds())
            mBorderRadius = Math.min(
                (mBorderRect.height() - borderWidth) / 2.0f,
                (mBorderRect.width() - borderWidth) / 2.0f
            )
            mDrawableRect.set(mBorderRect)
            if (!mBorderOverlay && borderWidth > 0) {
                mDrawableRect.inset(borderWidth - 1.0f, borderWidth - 1.0f)
            }
            mDrawableRadius = Math.min(mDrawableRect.height() / 2.0f, mDrawableRect.width() / 2.0f)
            applyColorFilter()
            updateShaderMatrix()
            invalidate()
        }
    }

    private fun calculateBounds(): RectF {
        val availableWidth = width - paddingLeft - paddingRight
        val availableHeight = height - paddingTop - paddingBottom
        val sideLength = Math.min(availableWidth, availableHeight)
        val left = paddingLeft + (availableWidth - sideLength) / 2f
        val top = paddingTop + (availableHeight - sideLength) / 2f
        return RectF(left, top, left + sideLength, top + sideLength)
    }

    private fun updateShaderMatrix() {
        val scale: Float
        var dx = 0f
        var dy = 0f
        shaderMatrix.set(null)
        if (mBitmapWidth * mDrawableRect.height() > mDrawableRect.width() * mBitmapHeight) {
            scale = mDrawableRect.height() / mBitmapHeight.toFloat()
            dx = (mDrawableRect.width() - mBitmapWidth * scale) * 0.5f
        } else {
            scale = mDrawableRect.width() / mBitmapWidth.toFloat()
            dy = (mDrawableRect.height() - mBitmapHeight * scale) * 0.5f
        }
        shaderMatrix.setScale(scale, scale)
        shaderMatrix.postTranslate(
            (dx + 0.5f).toInt() + mDrawableRect.left,
            (dy + 0.5f).toInt() + mDrawableRect.top
        )
        bitmapShader?.setLocalMatrix(shaderMatrix)
    }
}