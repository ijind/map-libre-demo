package ai.txai.common.glide

import ai.txai.common.thread.Dispatcher
import ai.txai.common.utils.AndroidUtils
import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import jp.wasabeef.glide.transformations.BlurTransformation
import jp.wasabeef.glide.transformations.CropCircleWithBorderTransformation
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import jp.wasabeef.glide.transformations.internal.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object GlideUtils {
    private const val ERROR_IMAGE_ID = 0

    private fun createRequestOptions(placeholderId: Int, errorId: Int): RequestOptions{
        return RequestOptions().placeholder(placeholderId).error(errorId)
    }

    fun ImageView.loadImage(context: Context, options: ImageOptions) {
        if (options.imageUrl.isEmpty()) return

        val requestOptions = createRequestOptions(options.placeHolderResId, ERROR_IMAGE_ID)
        Glide.with(context).load(options.imageUrl).apply(requestOptions).into(this)
    }

    fun ImageView.loadImageForTargetSize(context: Context, options: ImageOptions) {
        if (options.imageUrl.isEmpty() || options.width <= 0 || options.height <= 0) return

        val requestOptions = createRequestOptions(options.placeHolderResId, ERROR_IMAGE_ID)
            .override(options.width, options.height)
            .priority(Priority.HIGH)
        Glide.with(context).load(options.imageUrl).apply(requestOptions).into(this)
    }

    fun ImageView.loadImageNoMemoryCache(context: Context, options: ImageOptions) {
        if (options.imageUrl.isEmpty()) return

        val requestOptions = createRequestOptions(options.placeHolderResId, ERROR_IMAGE_ID)
            .skipMemoryCache(true)
        Glide.with(context).load(options.imageUrl).apply(requestOptions).into(this)
    }

    fun preloadImage(context: Context, url: String) {
        if (url.isEmpty()) return

        Glide.with(context).load(url).preload()
    }

    fun ImageView.loadCircleImage(context: Context, options: ImageOptions) {
        if (options.imageUrl.isEmpty()) return

        val requestOptions = createRequestOptions(options.placeHolderResId, ERROR_IMAGE_ID)
            .centerCrop()
            .circleCrop()
            .priority(Priority.HIGH)
        Glide.with(context).load(options.imageUrl).apply(requestOptions).into(this)
    }

    fun ImageView.loadCircleWithBorderImage(context: Context, options: ImageOptions) {
        if (options.imageUrl.isEmpty()) return

        val requestOptions = RequestOptions.bitmapTransform(
            MultiTransformation(CenterCrop(),
                CropCircleWithBorderTransformation(Utils.toDp(options.borderSize),
                    options.borderColorId)))
            .placeholder(options.placeHolderResId)
            .error(ERROR_IMAGE_ID)
        Glide.with(context).load(options.imageUrl).apply(requestOptions).into(this)
    }

    fun ImageView.loadRoundedCornersImage(context: Context, options: ImageOptions) {
        if (options.imageUrl.isEmpty()) return

        val requestOptions = RequestOptions.bitmapTransform(
            MultiTransformation(CenterCrop(),
            RoundedCornersTransformation(Utils.toDp(options.radius), 0,
                options.roundType)))
            .placeholder(options.placeHolderResId)
            .error(ERROR_IMAGE_ID)
        Glide.with(context).load(options.imageUrl).apply(requestOptions).into(this)
    }

    fun ImageView.loadBlurImage(context: Context, options: ImageOptions) {
        if (options.imageUrl.isEmpty() || options.blur <= 0) return

        val requestOptions = RequestOptions.bitmapTransform(
            MultiTransformation(CenterCrop(),
                BlurTransformation(options.blur)))
            .placeholder(options.placeHolderResId)
            .error(ERROR_IMAGE_ID)
        Glide.with(context).load(options.imageUrl).apply(requestOptions).into(this)
    }

    fun clearMemory(context: Context) {
        AndroidUtils.delayOperation(0) {
            Glide.get(context).clearMemory()
        }
    }

    fun clearDiskCache(context: Context) {
        CoroutineScope(Dispatcher.IO).launch {
            Glide.get(context).clearDiskCache()
        }
    }
}