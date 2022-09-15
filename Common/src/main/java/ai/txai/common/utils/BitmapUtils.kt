package ai.txai.common.utils

import ai.txai.common.log.LOG
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.ExifInterface
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception
import java.lang.Math.round
import java.lang.NullPointerException
import java.text.SimpleDateFormat
import java.util.*


object BitmapUtils {
    private const val TAG = "BitmapUtils"

    fun createImageFile(activity: Activity): File? {
        var timeStamp = AndroidUtils.buildNowDate()
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir: File? = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        var imageFile: File? = null
        try {
            imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return imageFile
    }

    fun readPictureDegree(path: String): Float {
        var degree = 0f
        try {
            val exifInterface = ExifInterface(path)
            val orientation = exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 ->
                    degree = 90f
                ExifInterface.ORIENTATION_ROTATE_180 ->
                    degree = 180f
                ExifInterface.ORIENTATION_ROTATE_270 ->
                    degree = 270f
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return degree
    }

    fun rotatingImageView(angle: Float, bitmap: Bitmap): Bitmap {
        var returnBm: Bitmap?
        val matrix = Matrix()
        matrix.postRotate(angle)
        returnBm =
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        if (returnBm == null) {
            returnBm = bitmap
        }
        if (bitmap != returnBm) {
            bitmap.recycle()
        }
        return returnBm
    }

    fun getCompressPhoto(path: String): Bitmap? {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(path, options)
        options.inSampleSize = calculateInSampleSize(options, 800, 1100)
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeFile(path, options)
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int, reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val heightRatio = round(height.toFloat() / reqHeight.toFloat())
            val widthRatio = round(width.toFloat() / reqWidth.toFloat())
            inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
        }
        return inSampleSize
    }

    fun compressBitmap(path: String, bitmap: Bitmap, quality: Int): String {
        val oldFile = File(path)
        val targetPath = oldFile.absolutePath
        val outFile = File(targetPath)
        var out: FileOutputStream? = null
        try {
            if (!outFile.exists()) {
                outFile.parentFile?.mkdirs()
            } else {
                outFile.delete()
            }
            out = FileOutputStream(outFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
        } catch (e: NullPointerException) {
            return path
        } finally {
            try {
                out?.close()
                //bitmap.recycle()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        LOG.d(TAG, "compress file name out ${outFile.length()}")
        return outFile.absolutePath
    }

    fun clearPicturesDir() {
        var path = AndroidUtils.getApplicationContext().getExternalFilesDir(
            Environment.DIRECTORY_PICTURES)?.path
        if (path.isNullOrEmpty()) return

        if (path.endsWith(File.separator)) {
            path = "$path${File.separator}"
        }

        val fileDir = File(path)
        if (!fileDir.exists() || !fileDir.isDirectory) return

        val fileArray = fileDir.listFiles() ?: return

        for (i in fileArray.indices) {
            val file = fileArray[i]
            if (file.isFile) {
                file.delete()
            } else if (file.isDirectory) {
                clearPicturesDir()
            }
        }
    }

    fun bitmapFromDrawableRes(context: Context, @DrawableRes resourceId: Int) =
        convertDrawableToBitmap(AppCompatResources.getDrawable(context, resourceId))

    private fun convertDrawableToBitmap(sourceDrawable: Drawable?): Bitmap? {
        if (sourceDrawable == null) {
            return null
        }
        return if (sourceDrawable is BitmapDrawable) {
            sourceDrawable.bitmap
        } else {
            // copying drawable object to not manipulate on the same reference
            val constantState = sourceDrawable.constantState ?: return null
            val drawable = constantState.newDrawable().mutate()
            val bitmap: Bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth, drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
    }

    fun drawRoundRect(color: Int, radius: Float,
                              width: Float, height: Float): Bitmap {
        val paint = Paint()
        paint.isAntiAlias = true
        paint.color = color
        paint.style = Paint.Style.FILL

        val rect = RectF(0f, 0f, width, height)
        val source = Bitmap.createBitmap(width.toInt(), height.toInt(), Bitmap.Config.ARGB_8888)
        val scaleWidth = (source.width / 8).toInt()
        val scaleHeight = (source.height / 8).toInt()
        val bitmap = Bitmap.createScaledBitmap(source, scaleWidth, scaleHeight, false)
        val canvas = Canvas(bitmap)
        canvas.drawRoundRect(rect, radius, radius, paint)
        return bitmap
    }
}