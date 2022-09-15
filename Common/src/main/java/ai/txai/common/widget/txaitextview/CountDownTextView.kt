package ai.txai.common.widget.txaitextview

import ai.txai.common.R
import ai.txai.common.utils.LogUtils
import ai.txai.common.utils.setDebounceClickListener
import android.content.Context
import android.graphics.Color
import android.os.CountDownTimer
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import kotlin.math.abs

class CountDownTextView : AppCompatTextView {
    companion object {
        private const val TAG = "CountDownText"
        private const val DEFAULT_DURATION = 0
        private const val WEEKS = 7 * 24 * 60 * 60 * 1000
        private const val DAYS = 24 * 60 * 60 * 1000
        private const val HOURS = 60 * 60 * 1000
        private const val MINUTES = 60 * 1000
        private const val SECONDS = 1000

        const val INTERVAL_SECOND = "second"
        const val INTERVAL_MINUTE = "minute"
        const val INTERVAL_HOUR = "hour"

        const val TIME_UP = 1 // overtime
        const val TIME_DOWN = 2 // Not time out
    }

    private var duration: Long = 60 //Default 60s

    //interval support: second(s), minute(m), hour(h)
    private var interval = INTERVAL_SECOND
    private var finishString = "null"
    private var prefixString = "null"
    private var suffixString = "null"
    private var suffixColor = 0
    private var colorPrefix: Int = 0
    private var finishColor: Int = 0
    private var timeColor: Int = 0
    private var isFinish: Boolean = false

    private lateinit var countDownFunc: (millis: Long) -> SpannableStringBuilder
    private var timeCount: TimeCount? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        init(context, attributeSet)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CountDownTextView)
        duration =
            typedArray.getInteger(R.styleable.CountDownTextView_duration, DEFAULT_DURATION).toLong()
        finishColor = ResourcesCompat.getColor(context.resources, R.color.commonview_orange_00, null)
        val strInterval = typedArray.getString(R.styleable.CountDownTextView_interval).toString()
        if (strInterval.isNotEmpty()) interval = strInterval

        val strFinish = typedArray.getString(R.styleable.CountDownTextView_finishString).toString()
        if (strFinish.isNotEmpty()) finishString = strFinish

        prefixString = typedArray.getString(R.styleable.CountDownTextView_prefixText).toString()
        colorPrefix =
            typedArray.getColor(R.styleable.CountDownTextView_prefixColor, Color.BLACK)

        suffixString = typedArray.getString(R.styleable.CountDownTextView_suffixText).toString()
        suffixColor = typedArray.getColor(R.styleable.CountDownTextView_suffixColor, Color.BLACK)

        timeColor = typedArray.getColor(R.styleable.CountDownTextView_countDownTimeColor, Color.RED)
        buildTimeCount(duration, interval)
        typedArray.recycle()
    }

    private fun buildTargetString(timeString: String): SpannableStringBuilder {
        val builder = SpannableStringBuilder()
        if (timeString.isEmpty()) {
            return builder
        }

        val colorPrefixSpan = ForegroundColorSpan(colorPrefix)
        val colorTimeSpan = ForegroundColorSpan(timeColor)
        val colorSuffixSpan = ForegroundColorSpan(suffixColor)
        val prefix = if (prefixString == "null") "" else prefixString
        val suffix = if (suffixString == "null") "" else suffixString
        builder.append(prefix).append(timeString).append(" ").append(suffix)
        builder.setSpan(colorPrefixSpan, 0, prefix.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        builder.setSpan(
            colorTimeSpan,
            prefix.length,
            timeString.length + prefix.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        builder.setSpan(
            colorSuffixSpan,
            prefix.length + timeString.length,
            builder.toString().length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return builder
    }

    private fun buildTimeCount(duration: Long, interval: String) {
        val product: Int
        when (interval) {
            INTERVAL_HOUR -> {
                product = 1000 * 60 * 60
                countDownFunc = { buildTargetString(" ${it / HOURS}h") }
            }
            INTERVAL_MINUTE -> {
                product = 1000 * 60
                countDownFunc = { buildTargetString(" ${it / MINUTES}mins") }
            }
            else -> {
                product = 1000
                countDownFunc = { buildTargetString(" ${it / SECONDS}s") }
            }
        }
        timeCount = TimeCount(duration * product, product.toLong())
        timeCount!!.start()
        LogUtils.d(TAG, "init start")
    }

    private var orderTimeCount: TimeCount? = null
    private fun buildTimeCount(time: Long) {
        clear()
        val timeDiff = System.currentTimeMillis() - time
        val type = if (timeDiff > 0) TIME_UP else TIME_DOWN
        LogUtils.d(TAG, "timeDiff = $timeDiff")
        val duration = if (type == TIME_UP) WEEKS.toLong() else abs(timeDiff)
        if (orderTimeCount != null) {
            orderTimeCount!!.onFinish()
            orderTimeCount!!.cancel()
            orderTimeCount = null
        }
        orderTimeCount = TimeCount(duration, MINUTES.toLong(), type, time)
        orderTimeCount!!.begin()
    }

    fun start() {
        timeCount?.let {
            cancel()
            it.start()
        }
        isEnabled = false
        isFinish = false
    }

    fun cancel() {
        timeCount?.let {
            it.onFinish()
            it.cancel()
        }
    }

    fun clear() {
        if (timeCount != null) {
            cancel()
            timeCount = null
        }
    }

    fun setClickListener(operation: () -> Unit) {
        setDebounceClickListener {
            if (isFinish)
            operation.invoke() }
    }

    fun isFinish() = isFinish

    fun setPrefixText(prefix: String) {
        this.prefixString = prefix
    }

    fun setSuffixText(suffix: String) {
        this.suffixString = suffix
    }

    private inner class TimeCount(
        millisInFuture: Long,
        countDownInterval: Long,
        private val type: Int = 0,
        private val time: Long = 0L
    ) : CountDownTimer(millisInFuture, countDownInterval) {
        private var restartOvertime = false

        override fun onTick(millis: Long) {
            if (type == 0) {
                text = countDownFunc.invoke(millis)
                return
            }
        }

        override fun onFinish() {
            LogUtils.d(TAG, "onFinish")
            isEnabled = true
            isFinish = true
            if (restartOvertime) {
                buildTimeCount(time)
                restartOvertime = false
            }
            if (finishString != "null") {
                text = finishString
            }
            if (finishColor != 0) {
                setTextColor(finishColor)
            }
        }

        fun begin() {
            start()
            isEnabled = false
            isFinish = false
        }

    }
}