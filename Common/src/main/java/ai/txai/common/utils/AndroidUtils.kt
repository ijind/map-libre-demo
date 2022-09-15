package ai.txai.common.utils

import ai.txai.common.log.LOG
import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("StaticFieldLeak")
object AndroidUtils : LifecycleObserver {
    private const val TAG = "AndroidUtil"

    private lateinit var context: Context

    private var isAppForeground = false

    var curCountryIsoCode = "AE"

    // Application初始化时调用
    fun init(context: Context) {
        this.context = context
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    /**
     * 提供获取application context
     * 应用内应尽量使用调用方的context
     */
    fun getApplicationContext() = context

    fun isAppForeground() = isAppForeground

    fun delayOperation(time: Long, operation: () -> Unit) {
        Looper.getMainLooper()?.let {
            Handler(it).postDelayed({
                operation.invoke()
            }, time)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForeground() {
        isAppForeground = true
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackground() {
        isAppForeground = false
    }

    /**
     * 提供（相对）精确的除法运算。当发生除不尽的情况时，由scale参数指
     * 定精度，以后的数字四舍五入。
     * @param v1 被除数
     * @param v2 除数
     * @param scale 表示表示需要精确到小数点以后几位。
     * @return 两个参数的商
     */
    fun div(v1: Double, v2: Double, scale: Int): Double {
        require(scale >= 0) { "The scale must be a positive integer or zero" }
        val b1 = BigDecimal(v1.toString())
        val b2 = BigDecimal(v2.toString())
        val result = b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP).toDouble()
        LogUtils.d(TAG, "div $b1 y= $b2  result $result")
        return result
    }

    fun buildDate(dateTime: Long): String {
        val pattern = "hh:mm a, dd MMM, yyyy"
        val sDateFormat = newSimpleDateFormat(pattern)
        val data = sDateFormat.format(Date(dateTime + 0))
        LOG.d(TAG, "data = $data")
        return data
    }

    fun buildDateHour(dateTime: Long): String {
        val pattern = "hh:mm a"
        val sDateFormat = newSimpleDateFormat(pattern)
        val data = sDateFormat.format(Date(dateTime + 0))
        LOG.d(TAG, "data = $data")
        return data
    }

    fun buildNowDate(): String {
        val sDateFormat = newSimpleDateFormat("yyyyMMdd_HHmmss")
        return sDateFormat.format(Date());
    }

    private fun newSimpleDateFormat(pattern: String): SimpleDateFormat {
        val sDateFormat = SimpleDateFormat(pattern, Locale.ENGLISH)
//        sDateFormat.timeZone = TimeZone.getTimeZone("GMT+4:00")
        return sDateFormat
    }

    fun buildAmount(amount: Double?): String {
        if (amount == null || amount <= 0.0) {
            return "0.00"
        }
        val toInt = (amount * 100).toInt()
        if (toInt == 0) {
            return "0.00"
        }
        val strB = StringBuilder((toInt / 100).toString()).append(".")
        when {
            toInt % 100 <= 9 -> {
                strB.append(0).append(toInt % 100)
            }
            else -> {
                strB.append(toInt % 100)
            }
        }
        return strB.toString();
    }

    fun dip2px(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    fun px2dip(context: Context, pxValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }

    fun isExistClass(className: String): Boolean {
        try {
            Class.forName(className)
        } catch (e: ClassNotFoundException) {
            return false
        }

        return true
    }

    fun isShouldHideKeyboard(v: View?, event: MotionEvent): Boolean {
        if (v != null && v is EditText) {
            val l = intArrayOf(0, 0)
            v.getLocationOnScreen(l)
            val left = l[0]
            val top = l[1]
            val bottom = top + v.getHeight()
            val right = left + v.getWidth()
            return !(event.rawX > left && event.rawX < right && event.rawY > top && event.rawY < bottom)
        }

        return false
    }
}