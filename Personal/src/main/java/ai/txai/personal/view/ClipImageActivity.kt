package ai.txai.personal.view

import ai.txai.common.log.LOG
import ai.txai.common.utils.BitmapUtils
import ai.txai.personal.R
import ai.txai.personal.widget.ClipViewLayout
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.gyf.immersionbar.ImmersionBar
import java.io.IOException
import java.io.OutputStream


class ClipImageActivity: AppCompatActivity(), View.OnClickListener {
    companion object {
        private const val TAG = "ClipImageView"
    }
    private lateinit var clipViewLayout: ClipViewLayout
    private lateinit var btnCancel: TextView
    private lateinit var btnOk: TextView
    private var imgPath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ImmersionBar.with(this)
            .transparentStatusBar()
            .statusBarColor(android.R.color.transparent)
            .statusBarAlpha(0.0f)
            .statusBarDarkFont(false)
            .init()
        setContentView(R.layout.personal_clip_image_layout)
        initView()
    }

    private fun initView() {
        clipViewLayout = findViewById(R.id.common_clip_rectangle_layout)
        btnCancel = findViewById(R.id.common_cancel_tv)
        btnOk = findViewById(R.id.common_confirm_tv)
        btnCancel.setOnClickListener(this)
        btnOk.setOnClickListener(this)

        clipViewLayout.visibility = View.VISIBLE
        imgPath = intent.getStringExtra("path") ?: ""
        clipViewLayout.setImageSrc(intent.data, imgPath)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.common_cancel_tv -> finish()
            R.id.common_confirm_tv -> generateUriAndReturn()
        }
    }

    private fun generateUriAndReturn() {
        val zoomedCropBitmap = clipViewLayout.clip()
        if (zoomedCropBitmap == null) {
            LOG.e(TAG, "zoomedCropBitmap == null")
            return
        }
        val file = BitmapUtils.createImageFile(this)
        val path = file?.absolutePath ?: ""
        val saveUri: Uri? = file?.toUri()
        if (saveUri != null) {
            var outputStream: OutputStream? = null
            try {
                outputStream = contentResolver.openOutputStream(saveUri)
                if (outputStream != null) {
                    zoomedCropBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                }
            } catch (ex: IOException) {
                LOG.e(TAG, "Cannot open file: $saveUri")
            } finally {
                if (outputStream != null) {
                    try {
                        outputStream.close()
                        zoomedCropBitmap.recycle()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
            val intent = Intent()
            intent.putExtra("avatar_path", path)
            intent.data = saveUri
            setResult(RESULT_OK, intent)
            finish()
        }
    }
}