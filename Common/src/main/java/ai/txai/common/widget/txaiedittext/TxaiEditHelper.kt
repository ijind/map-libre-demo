package ai.txai.common.widget.txaiedittext

import ai.txai.common.R
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.viewbinding.ViewBinding

object TxaiEditHelper {

    inline fun <reified T> translateBinding(binding: ViewBinding): T? {
        if (binding !is T) {
            return null
        }

        return binding
    }

    fun TextView.textCountBuilder(count: Int) {
        val builder = SpannableStringBuilder()
        builder.append(count.toString()).append("/1000")
        val color = if (count > 0) {
            ResourcesCompat.getColor(resources, R.color.commonview_grey_99, null)
        } else {
            ResourcesCompat.getColor(resources, R.color.commonview_grey_c3, null)
        }
        val colorSpan = ForegroundColorSpan(color)
        builder.setSpan(colorSpan, 0, count.toString().length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        this.text = builder
    }
}