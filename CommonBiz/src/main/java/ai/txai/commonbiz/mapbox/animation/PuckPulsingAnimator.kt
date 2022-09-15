package ai.txai.commonbiz.mapbox.animation

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.core.view.animation.PathInterpolatorCompat
import com.mapbox.bindgen.Value
import com.mapbox.maps.extension.style.expressions.generated.Expression
import com.mapbox.maps.plugin.animation.animator.Evaluators
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

internal class PuckPulsingAnimator : PuckAnimator<Double>(Evaluators.DOUBLE) {

  var enabled = false
  var maxRadius: Double = 115.0
  @ColorInt
  var pulsingColor: Int = Color.parseColor("#4B7DF6")
  var pulseFadeEnabled = true

  init {
    duration = PULSING_DEFAULT_DURATION
    repeatMode = RESTART
    repeatCount = INFINITE
    interpolator = PULSING_DEFAULT_INTERPOLATOR
  }

  fun animateInfinite() {
    animate(0.0, maxRadius)
  }

  override fun updateLayer(fraction: Float, value: Double) {
    var opacity = 1.0f
    if (pulseFadeEnabled) {
      opacity = 1.0f - (value / maxRadius).toFloat()
    }
    opacity = if (fraction <= 0.1f) 0f else opacity
    val rgbaArray = colorToRgbaArray(pulsingColor)
    rgbaArray[3] = opacity ?: 1f
    locationRenderer?.emphasisCircleRadius(value)
    locationRenderer?.emphasisCircleColor(buildRGBAExpression(rgbaArray))
  }

  companion object {

    fun buildRGBAExpression(colorArray: FloatArray): Expression {
      return Expression.rgba(colorArray[0].toDouble(), colorArray[1].toDouble(),colorArray[2].toDouble(),colorArray[3].toDouble())
    }

    fun colorIntToRgbaExpression(@ColorInt color: Int): List<Value> {
      val numberFormat =
        NumberFormat.getNumberInstance(Locale.US)
      val decimalFormat = numberFormat as DecimalFormat
      decimalFormat.applyPattern("#.###")
      val alpha =
        decimalFormat.format((color shr 24 and 0xFF).toFloat() / 255.0f.toDouble())
      return arrayListOf(
        Value("rgba"),
        Value((color shr 16 and 0xFF).toLong()),
        Value((color shr 8 and 0xFF).toLong()),
        Value((color and 0xFF).toLong()),
        Value(alpha.toDouble())
      )
    }

    fun colorToRgbaArray(@ColorInt color: Int): FloatArray {
      return floatArrayOf(
        (color shr 16 and 0xFF.toFloat().toInt()).toFloat(), // r (0-255)
        (color shr 8 and 0xFF.toFloat().toInt()).toFloat(), // g (0-255)
        (color and 0xFF.toFloat().toInt()).toFloat(), // b (0-255)
        (color shr 24 and 0xFF) / 255.0f // a (0-1)
      )
    }

    const val PULSING_DEFAULT_DURATION = 3_000L
    private val PULSING_DEFAULT_INTERPOLATOR = PathInterpolatorCompat.create(
      0.0f,
      0.0f,
      0.25f,
      1.0f
    )
  }
}