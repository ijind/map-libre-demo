package com.luno.base.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager

class ClipView: View {
    private val paint: Paint = Paint()

    //画裁剪区域边框的画笔
    private val borderPaint: Paint = Paint()

    //裁剪框水平方向间距
    private var mHorizontalPadding = 0f

    //裁剪框边框宽度
    private var clipBorderWidth = 0

    //裁剪圆框的半径
    private var clipRadiusWidth = 0

    //裁剪框矩形宽度
    private var clipWidth = 0

    //裁剪框类别，（圆形、矩形），默认为圆形
    private var clipType = ClipType.CIRCLE
    private var xfermode: Xfermode? = null

    constructor(context: Context): super(context) {
        initView(context, null, null)
    }

    constructor(context: Context?, attrs: AttributeSet): super(context, attrs) {
        initView(context, attrs, null)
    }

    constructor(context: Context?, attrs: AttributeSet, defStyle: Int)
            : super(context, attrs, defStyle) {
        initView(context, attrs, defStyle)
    }

    fun initView(context: Context?, attrs: AttributeSet?, defStyle: Int?) {
        paint.isAntiAlias = true
        borderPaint.style = Paint.Style.STROKE
        borderPaint.color = Color.WHITE
        borderPaint.strokeWidth = clipBorderWidth.toFloat()
        borderPaint.isAntiAlias = true
        xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //通过Xfermode的DST_OUT来产生中间的透明裁剪区域，一定要另起一个Layer（层）
        canvas.saveLayer(0f, 0f, this.width.toFloat(),
            this.height.toFloat(), null)
        //设置背景
        canvas.drawColor(Color.parseColor("#a8000000"))
        paint.xfermode = xfermode
        //绘制圆形裁剪框
        if (clipType == ClipType.CIRCLE) {
            //中间的透明的圆
            canvas.drawCircle((this.width / 2).toFloat(), (this.height / 2).toFloat(),
                clipRadiusWidth.toFloat(), paint)
            //白色的圆边框
            canvas.drawCircle((this.width / 2).toFloat(), (this.height / 2).toFloat(),
                clipRadiusWidth.toFloat(), borderPaint)
        } else if (clipType == ClipType.RECTANGLE) { //绘制矩形裁剪框
            //绘制中间的矩形
            canvas.drawRect(
                mHorizontalPadding, (this.height / 2 - clipWidth / 2).toFloat(),
                this.width - mHorizontalPadding,
                (this.height / 2 + clipWidth / 2).toFloat(), paint)
            //绘制白色的矩形边框
            canvas.drawRect(
                mHorizontalPadding, (this.height / 2 - clipWidth / 2).toFloat(),
                this.width - mHorizontalPadding,
                (this.height / 2 + clipWidth / 2).toFloat(), borderPaint)
        }
        //出栈，恢复到之前的图层，意味着新建的图层会被删除，新建图层上的内容会被绘制到canvas (or the previous layer)
        canvas.restore()
    }

    /**
     * 获取裁剪区域的Rect
     *
     * @return
     */
    fun getClipRect(): Rect {
        val rect = Rect()
        //宽度的一半 - 圆的半径
        rect.left = this.width / 2 - clipRadiusWidth
        //宽度的一半 + 圆的半径
        rect.right = this.width / 2 + clipRadiusWidth
        //高度的一半 - 圆的半径
        rect.top = this.height / 2 - clipRadiusWidth
        //高度的一半 + 圆的半径
        rect.bottom = this.height / 2 + clipRadiusWidth
        return rect
    }

    /**
     * 设置裁剪框边框宽度
     *
     * @param clipBorderWidth
     */
    fun setClipBorderWidth(clipBorderWidth: Int) {
        this.clipBorderWidth = clipBorderWidth
        borderPaint.strokeWidth = clipBorderWidth.toFloat()
        invalidate()
    }

    /**
     * 设置裁剪框水平间距
     *
     * @param mHorizontalPadding
     */
    fun setHorizontalPadding(horizontalPadding: Float) {
        this.mHorizontalPadding = horizontalPadding
        clipRadiusWidth = (getScreenWidth(context) - 2 * horizontalPadding).toInt() / 2
        clipWidth = clipRadiusWidth * 2
    }

    /**
     * 获得屏幕高度
     *
     * @param context
     * @return
     */
    fun getScreenWidth(context: Context): Int {
        return context.resources.displayMetrics.widthPixels
    }


    /**
     * 设置裁剪框类别
     *
     * @param clipType
     */
    fun setClipType(clipType: ClipType) {
        this.clipType = clipType
    }

    /**
     * 裁剪框类别，圆形、矩形
     */
    enum class ClipType {
        CIRCLE, RECTANGLE
    }
}