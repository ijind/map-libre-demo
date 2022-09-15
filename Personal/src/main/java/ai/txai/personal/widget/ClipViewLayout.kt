package ai.txai.personal.widget

import ai.txai.common.utils.AndroidUtils
import ai.txai.common.utils.LogUtils
import ai.txai.personal.R
import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.graphics.drawable.Drawable
import android.media.ExifInterface
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.net.Uri
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.ViewTreeObserver
import android.widget.ImageView
import com.luno.base.widget.ClipView
import java.io.IOException
import java.lang.Exception

class ClipViewLayout: RelativeLayout {

    companion object {
        private const val TAG = "ClipViewLayout"
        //动作标志：无
        private const val NONE = 0

        //动作标志：拖动
        private const val DRAG = 1

        //动作标志：缩放
        private const val ZOOM = 2
    }
    //裁剪原图
    private lateinit var imageView: ImageView

    //裁剪框
    private lateinit var clipView: ClipView

    //裁剪框水平方向间距，xml布局文件中指定
    private var mHorizontalPadding = 0f

    //裁剪框垂直方向间距，计算得出
    private var mVerticalPadding = 0f

    //图片缩放、移动操作矩阵
    private val imgMatrix = Matrix()

    //图片原来已经缩放、移动过的操作矩阵
    private val savedMatrix: Matrix = Matrix()

    //初始化动作标志
    private var mode = NONE

    //记录起始坐标
    private val start: PointF = PointF()

    //记录缩放时两指中间点坐标
    private val mid: PointF = PointF()
    private var oldDist = 1f

    //用于存放矩阵的9个值
    private val matrixValues = FloatArray(9)

    //最小缩放比例
    private var minScale = 0f

    //最大缩放比例
    private val maxScale = 4f


    constructor(context: Context): super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int)
            : super(context, attrs, defStyle) {
        init(context, attrs)
    }

    fun init(context: Context, attrs: AttributeSet?) {
        val array: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.ClipViewLayout)

        //获取剪切框距离左右的边距, 默认为50dp
        mHorizontalPadding = array.getDimensionPixelSize(
            R.styleable.ClipViewLayout_horizontalPadding,
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                50f, resources.displayMetrics).toInt()).toFloat()
        //获取裁剪框边框宽度，默认1dp
        val clipBorderWidth: Int = array.getDimensionPixelSize(
            R.styleable.ClipViewLayout_clipBorderWidth,
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                1f,
                resources.displayMetrics).toInt())
        //裁剪框类型(圆或者矩形)
        val clipType: Int = array.getInt(R.styleable.ClipViewLayout_clipType, 1)

        //回收
        array.recycle()
        clipView = ClipView(context)
        //设置裁剪框类型
        clipView.setClipType(if (clipType == 1) ClipView.ClipType.CIRCLE
        else ClipView.ClipType.RECTANGLE
        )
        //设置剪切框边框
        clipView.setClipBorderWidth(clipBorderWidth)
        //设置剪切框水平间距
        clipView.setHorizontalPadding(mHorizontalPadding)
        imageView = ImageView(context)
        //相对布局布局参数
        val lp: ViewGroup.LayoutParams = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        this.addView(imageView, lp)
        this.addView(clipView, lp)
    }


    /**
     * 初始化图片
     */
    fun setImageSrc(uri: Uri?, path: String) {
        //需要等到imageView绘制完毕再初始化原图
        val observer: ViewTreeObserver = imageView.viewTreeObserver
        observer.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                initSrcPic(uri, path)
                imageView.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }

    /**
     * 初始化图片
     * step 1: decode 出 720*1280 左右的照片  因为原图可能比较大 直接加载出来会OOM
     * step 2: 将图片缩放 移动到imageView 中间
     */
    fun initSrcPic(uri: Uri?, path: String) {
        if (uri == null) {
            return
        }
        LogUtils.d("evan", "**********clip_view uri*******  $uri")
        //val path: String = BitmapUtils.getRealFilePathFromUri(context, uri) ?: ""
        LogUtils.d("evan", "**********clip_view path*******  $path")
        if (path.isEmpty()) {
            LogUtils.d(TAG, "path is null")
            return
        }

        //这里decode出720*1280 左右的照片,防止OOM
        val decodeBitmap: Bitmap? = decodeSampledBitmap(path, 720, 1280)

        val rotation = getExifOrientation(path)
        val m = Matrix()
        m.setRotate(rotation.toFloat())
        LogUtils.d(TAG, "init bitmap ${decodeBitmap?.width} height = ${decodeBitmap?.height}")
        if (decodeBitmap == null) return
        val bitmap = Bitmap.createBitmap(decodeBitmap, 0, 0,
            decodeBitmap.width, decodeBitmap.height, m, true)
        val rect = clipView.getClipRect()
        LogUtils.d(TAG, "init rect ${rect.width()} height = ${rect.height()}")
        //图片的缩放比
        var scale: Float
        if (bitmap.width >= bitmap.height) { //宽图
            scale = AndroidUtils.div(imageView.width.toDouble(),
                bitmap.width.toDouble(), 2).toFloat()
            //如果高缩放后小于裁剪区域 则将裁剪区域与高的缩放比作为最终的缩放比
            //高的最小缩放比

            minScale = AndroidUtils.div(rect.height().toDouble(),
                bitmap.height.toDouble(), 2).toFloat()
            LogUtils.d(TAG, "rect ${rect.height()} width ${imageView.width}")
            if (scale < minScale) {
                scale = minScale
            }
            LogUtils.d(TAG, "height <= width $scale  $minScale")
        } else { //高图
            //高的缩放比
            scale = AndroidUtils.div(imageView.height.toDouble(),
                bitmap.height.toDouble(), 2).toFloat()
            //如果宽缩放后小于裁剪区域 则将裁剪区域与宽的缩放比作为最终的缩放比
            //宽的最小缩放比
            minScale = AndroidUtils.div(rect.width().toDouble(),
                bitmap.width.toDouble(), 2).toFloat()
            if (scale < minScale) {
                scale = minScale
            }
            LogUtils.d(
                TAG, "height > width $scale  $minScale" +
                    "img ${imageView.height}, bitmap ${bitmap.height}")
        }
        // 缩放
        imgMatrix.postScale(scale, scale)
        // 平移,将缩放后的图片平移到imageview的中心
        //imageView的中心x
        val midX: Int = imageView.width / 2
        //imageView的中心y
        val midY: Int = imageView.height / 2
        //bitmap的中心x
        val imageMidX = bitmap.width * scale / 2
        //bitmap的中心y
        val imageMidY = bitmap.height * scale / 2
        imgMatrix.postTranslate((midX - imageMidX), (midY - imageMidY))
        imageView.scaleType = ImageView.ScaleType.MATRIX
        imageView.imageMatrix = imgMatrix
        imageView.setImageBitmap(bitmap)
    }

    /**
     * 查询图片旋转角度
     */
    private fun getExifOrientation(filepath: String): Int {
        var degree = 0
        var exif: ExifInterface? = null
        try {
            exif = ExifInterface(filepath)
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
        if (exif != null) {
            val orientation: Int = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1)
            if (orientation != -1) {
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> degree = 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> degree = 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> degree = 270
                }
            }
        }
        return degree
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        LogUtils.d(TAG, "on touch e${event.action}")
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                savedMatrix.set(imgMatrix)
                //设置开始点位置
                start.set(event.x, event.y)
                mode = DRAG
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                //开始放下时候两手指间的距离
                oldDist = spacing(event)
                if (oldDist > 10f) {
                    savedMatrix.set(imgMatrix)
                    midPoint(mid, event)
                    mode = ZOOM
                }
            }
            MotionEvent.ACTION_UP -> {}
            MotionEvent.ACTION_POINTER_UP -> mode = NONE
            MotionEvent.ACTION_MOVE -> {
                if (mode == DRAG) { //拖动
                    imgMatrix.set(savedMatrix)
                    val dx: Float = event.x - start.x
                    val dy: Float = event.y - start.y
                    mVerticalPadding = clipView.getClipRect().top.toFloat()
                    imgMatrix.postTranslate(dx, dy)
                    //检查边界
                    checkBorder()
                } else if (mode == ZOOM) { //缩放
                    //缩放后两手指间的距离
                    val newDist = spacing(event)
                    if (newDist > 10f) {
                        //手势缩放比例
                        var scale = newDist / oldDist
                        if (scale < 1) { //缩小
                            LogUtils.d(TAG, " zoom ${getScale()}, min $minScale")
                            if (getScale() > minScale) {
                                imgMatrix.set(savedMatrix)
                                mVerticalPadding = clipView.getClipRect().top.toFloat()
                                imgMatrix.postScale(scale, scale, mid.x, mid.y)
                                //缩放到最小范围下面去了，则返回到最小范围大小
                                while (getScale() < minScale) {
                                    //返回到最小范围的放大比例
                                    scale = 1 + 0.01f
                                    imgMatrix.postScale(scale, scale, mid.x, mid.y)
                                }
                            }
                            //边界检查
                            checkBorder()
                        } else { //放大
                            if (getScale() <= maxScale) {
                                imgMatrix.set(savedMatrix)
                                mVerticalPadding = clipView.getClipRect().top.toFloat()
                                imgMatrix.postScale(scale, scale, mid.x, mid.y)
                            }
                        }
                    }
                }
                imageView.imageMatrix = imgMatrix
            }
        }
        return true
    }

    /**
     * 根据当前图片的Matrix获得图片的范围
     */
    private fun getMatrixRectF(matrix: Matrix): RectF {
        val rect = RectF()
        val d: Drawable? = imageView.drawable
        if (d != null) {
            rect.set(0f, 0f, d.intrinsicWidth.toFloat(),
                d.intrinsicHeight.toFloat())
            matrix.mapRect(rect)
        }
        return rect
    }

    /**
     * 边界检测
     */
    private fun checkBorder() {
        val rect: RectF = getMatrixRectF(imgMatrix)
        var deltaX = 0f
        var deltaY = 0f
        val width: Int = imageView.width
        val height: Int = imageView.height
        // 如果宽或高大于屏幕，则控制范围 ; 这里的0.001是因为精度丢失会产生问题，但是误差一般很小，所以我们直接加了一个0.01
        if (rect.width() >= width - 2 * mHorizontalPadding) {
            if (rect.left > mHorizontalPadding) {
                deltaX = -rect.left + mHorizontalPadding
            }
            if (rect.right < width - mHorizontalPadding) {
                deltaX = width - mHorizontalPadding - rect.right
            }
        }
        if (rect.height() >= height - 2 * mVerticalPadding) {
            if (rect.top > mVerticalPadding) {
                deltaY = -rect.top + mVerticalPadding
            }
            if (rect.bottom < height - mVerticalPadding) {
                deltaY = height - mVerticalPadding - rect.bottom
            }
        }
        imgMatrix.postTranslate(deltaX, deltaY)
    }

    /**
     * 获得当前的缩放比例
     */
    private fun getScale(): Float {
        imgMatrix.getValues(matrixValues)
        return matrixValues[Matrix.MSCALE_X]
    }


    /**
     * 多点触控时，计算最先放下的两指距离
     */
    private fun spacing(event: MotionEvent): Float {
        val x: Float = event.getX(0) - event.getX(1)
        val y: Float = event.getY(0) - event.getY(1)
        return Math.sqrt((x * x + y * y).toDouble()).toFloat()
    }

    /**
     * 多点触控时，计算最先放下的两指中心坐标
     */
    private fun midPoint(point: PointF, event: MotionEvent) {
        val x: Float = event.getX(0) + event.getX(1)
        val y: Float = event.getY(0) + event.getY(1)
        point.set(x / 2, y / 2)
    }


    /**
     * 获取剪切图
     */
    fun clip(): Bitmap? {
        imageView.setDrawingCacheEnabled(true)
        imageView.buildDrawingCache()
        val rect: Rect = clipView.getClipRect()
        var cropBitmap: Bitmap? = null
        var zoomedCropBitmap: Bitmap? = null
        try {
            cropBitmap = Bitmap.createBitmap(
                imageView.getDrawingCache(),
                rect.left,
                rect.top,
                rect.width(),
                rect.height()
            )
            val width = resources.displayMetrics.widthPixels
            val height = clipView.getClipRect().height()
            zoomedCropBitmap = zoomBitmap(cropBitmap, width, height)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        cropBitmap?.recycle()
        // 释放资源
        imageView.destroyDrawingCache()
        return zoomedCropBitmap
    }


    /**
     * 图片等比例压缩
     *
     * @param filePath
     * @param reqWidth  期望的宽
     * @param reqHeight 期望的高
     * @return
     */
    private fun decodeSampledBitmap(
        filePath: String?, reqWidth: Int,
        reqHeight: Int
    ): Bitmap? {

        // First decode with inJustDecodeBounds=true to check dimensions
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        //options.inPreferredConfig = Bitmap.Config.RGB_565
        //bitmap is null
        BitmapFactory.decodeFile(filePath, options)

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(
            options, reqWidth,
            reqHeight)

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeFile(filePath, options)
    }

    /**
     * 计算InSampleSize
     * 宽的压缩比和高的压缩比的较小值  取接近的2的次幂的值
     * 比如宽的压缩比是3 高的压缩比是5 取较小值3  而InSampleSize必须是2的次幂，取接近的2的次幂4
     *
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int, reqHeight: Int
    ): Int {
        // Raw height and width of image
        val height: Int = options.outHeight
        val width: Int = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and
            // width
            val heightRatio = Math.round(
                height.toFloat()
                        / reqHeight.toFloat()
            )
            val widthRatio = Math.round(width.toFloat() / reqWidth.toFloat())

            // Choose the smallest ratio as inSampleSize value, this will
            // guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            val ratio = if (heightRatio < widthRatio) heightRatio else widthRatio
            // inSampleSize只能是2的次幂  将ratio就近取2的次幂的值
            if (ratio < 3) inSampleSize = ratio else if (ratio < 6.5) inSampleSize =
                4 else if (ratio < 8) inSampleSize = 8 else inSampleSize = ratio
        }
        return inSampleSize
    }

    /**
     * 图片缩放到指定宽高
     *
     *
     * 非等比例压缩，图片会被拉伸
     *
     * @param bitmap 源位图对象
     * @param w      要缩放的宽度
     * @param h      要缩放的高度
     * @return 新Bitmap对象
     */
    private fun zoomBitmap(bitmap: Bitmap, w: Int, h: Int): Bitmap? {
        val width: Int = bitmap.width
        val height: Int = bitmap.height
        val matrix = Matrix()
        val scaleWidth = (w.toFloat() / width)
        val scaleHeight = (h.toFloat() / height)
        matrix.postScale(scaleWidth, scaleHeight)
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false)
    }

}