package ai.txai.common.glide

import ai.txai.common.R
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.bumptech.glide.request.RequestListener
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import java.io.File

class ImageOptions private constructor(
    val imageUrl: String,
    val width: Int,
    val height: Int,
    val placeHolderResId: Int,
    val borderSize: Int,
    val borderColorId: Int,
    val radius: Int,
    val roundType: RoundedCornersTransformation.CornerType,
    val blur: Int,
    val requestListener: RequestListener<File>?){

    companion object {
        inline fun build(block: Builder.() -> Unit) = Builder().apply(block).build()
    }

    constructor(builder: Builder): this(
        builder.imageUrl,
        builder.width,
        builder.height,
        builder.placeHolderResId,
        builder.borderSize,
        builder.borderColorId,
        builder.radius,
        builder.roundType,
        builder.blur,
        builder.requestListener
    )

    class Builder {
        var imageUrl: String = ""
        private set

        var width: Int = 0
        private set

        var height: Int = 0
        private set

        @DrawableRes
        var placeHolderResId: Int = R.mipmap.ic_default_avatar
        private set

        var borderSize: Int = 0
        private set

        @ColorRes
        var borderColorId: Int = 0
        private set

        var radius: Int = 0
        private set

        // Round corner position
        var roundType: RoundedCornersTransformation.CornerType =
            RoundedCornersTransformation.CornerType.ALL
        private set

        // Common 1~100
        var blur: Int = 0
        private set

        var requestListener: RequestListener<File>? = null
        private set

        fun setImageUrl(url: String): Builder {
            imageUrl = url
            return this
        }

        fun setWidth(width: Int): Builder {
            this.width = width
            return this
        }

        fun setHeight(height: Int): Builder {
            this.height = height
            return this
        }

        fun setPlaceHolderResId(@DrawableRes placeHolderResId: Int): Builder {
            this.placeHolderResId = placeHolderResId
            return this
        }

        fun setBorderSize(borderSize: Int): Builder {
            this.borderSize = borderSize
            return this
        }

        fun setBorderColorId(@ColorRes borderColorId: Int): Builder {
            this.borderColorId = borderColorId
            return this
        }

        fun setRadius(radius: Int): Builder {
            this.radius = radius
            return this
        }

        fun setRoundType(roundType: RoundedCornersTransformation.CornerType): Builder {
            this.roundType = roundType
            return this
        }

        fun setBlur(blur: Int): Builder {
            this.blur = blur
            return this
        }

        fun setRequestListener(requestListener: RequestListener<File>): Builder {
            this.requestListener = requestListener
            return this
        }

        fun build() = ImageOptions(this)
    }
}