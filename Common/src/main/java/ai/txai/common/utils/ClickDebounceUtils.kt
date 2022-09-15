package ai.txai.common.utils

import ai.txai.common.R
import ai.txai.common.log.LOG
import android.view.View
import kotlin.math.abs

fun View.setDebounceClickListener(listener: View.OnClickListener?) {
    LOG.d("Debounce", " tag ")
    this.setOnClickListener { view ->
        this.getTag(R.id.common_click_view)?.let {
            val duration = (it as Long) - System.currentTimeMillis()
            LOG.d("Debounce", "duration = $duration")
            if (abs(duration) > 500) {
                this.setTag(R.id.common_click_view, System.currentTimeMillis())
                listener?.onClick(view)
            }
        } ?: let {
            this.setTag(R.id.common_click_view, System.currentTimeMillis())
            listener?.onClick(view)
        }
    }
}