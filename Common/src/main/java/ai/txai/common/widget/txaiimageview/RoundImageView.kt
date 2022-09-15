package ai.txai.common.widget.txaiimageview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet


class RoundImageView: androidx.appcompat.widget.AppCompatImageView {
    private var radius = 16

    constructor(context: Context) : super(context)

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    fun setRadius(radius: Int) {
        this.radius = radius
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        val path = Path()
        path.addRoundRect(
            RectF(0f, 0f, width.toFloat(), height.toFloat()),
            radius.toFloat(),
            radius.toFloat(),
            Path.Direction.CW
        )
        canvas.clipPath(path)
        super.onDraw(canvas)
    }
}
