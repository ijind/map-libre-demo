package ai.txai.imageselector.adapter

import ai.txai.imageselector.entry.Image
import ai.txai.imageselector.utils.ImageUtil
import androidx.viewpager.widget.PagerAdapter
import com.lxj.xpopup.photoview.PhotoView
import ai.txai.imageselector.utils.VersionUtils
import android.content.Context
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.SimpleTarget
import android.graphics.Bitmap
import android.graphics.Matrix
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.request.transition.Transition
import com.lxj.xpopup.photoview.PhotoViewAttacher
import java.lang.Exception
import java.util.ArrayList

class ImagePagerAdapter(private val mContext: Context, imgList: List<Image>) : PagerAdapter() {
    private val viewList: MutableList<PhotoView> = ArrayList(4)
    private var imgList = ArrayList<Image>()
    private var listener: OnItemClickListener? = null
    private val isAndroidQ = VersionUtils.isAndroidQ()
    private fun createImageViews() {
        for (i in 0..3) {
            val imageView = PhotoView(mContext)
            imageView.adjustViewBounds = true
            viewList.add(imageView)
        }
    }

    override fun getCount(): Int {
        return imgList.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun destroyItem(container: ViewGroup, position: Int, view: Any) {
        if (view is PhotoView) {
            view.setImageDrawable(null)
            viewList.add(view)
            container.removeView(view)
        }
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val currentView = viewList.removeAt(0)
        val image = imgList[position]
        container.addView(currentView)
        if (image.isGif) {
            currentView.scaleType = ImageView.ScaleType.FIT_CENTER
            Glide.with(mContext).load(if (isAndroidQ) image.uri else image.path)
                .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE))
                .override(720, 1080)
                .into(currentView)
        } else {
            Glide.with(mContext).asBitmap()
                .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE))
                .load(if (isAndroidQ) image.uri else image.path)
                .into(object : SimpleTarget<Bitmap?>(720, 1080) {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap?>?
                    ) {
                        val bw = resource.width
                        val bh = resource.height
                        if (bw > 4096 || bh > 4096) {
                            val bitmap = ImageUtil.zoomBitmap(resource, 4096, 4096)
                            setBitmap(currentView, bitmap)
                        } else {
                            setBitmap(currentView, resource)
                        }
                    }
                })
        }
        currentView.setOnClickListener {
            if (listener != null) {
                listener!!.onItemClick(position, image)
            }
        }
        return currentView
    }

    private fun setBitmap(imageView: PhotoView, bitmap: Bitmap?) {
        imageView.setImageBitmap(bitmap)
        if (bitmap != null) {
            val bw = bitmap.width
            val bh = bitmap.height
            val vw = imageView.width
            val vh = imageView.height
            if (bw != 0 && bh != 0 && vw != 0 && vh != 0) {
                if (1.0f * bh / bw > 1.0f * vh / vw) {
                    imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                    val offset = (1.0f * bh * vw / bw - vh) / 2
                    adjustOffset(imageView, offset)
                } else {
                    imageView.scaleType = ImageView.ScaleType.FIT_CENTER
                }
            }
        }
    }

    fun setOnItemClickListener(l: OnItemClickListener?) {
        listener = l
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int, image: Image?)
    }

    private fun adjustOffset(view: PhotoView, offset: Float) {
        val attacher = view.getAttacher()
        try {
            val field = PhotoViewAttacher::class.java.getDeclaredField("mBaseMatrix")
            field.isAccessible = true
            val matrix = field[attacher] as Matrix
            matrix.postTranslate(0f, offset)
            val method = PhotoViewAttacher::class.java.getDeclaredMethod("resetMatrix")
            method.isAccessible = true
            method.invoke(attacher)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    init {
        createImageViews()
        this.imgList.addAll(imgList)
    }
}