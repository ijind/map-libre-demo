package ai.txai.login.utils

import ai.txai.login.R
import android.content.res.Resources
import android.text.NoCopySpan
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import androidx.core.content.res.ResourcesCompat

object LoginModuleUtils {

    fun createClickSpan(res: Resources, clickOperation: () -> Unit): ClickableSpan {
        val clickableSpan = object: ClickableSpan(), NoCopySpan {
            override fun onClick(p0: View) {
                clickOperation.invoke()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
                ds.color = ResourcesCompat.getColor(res, R.color.login_link_color, null)
            }
        }

        return clickableSpan
    }

}