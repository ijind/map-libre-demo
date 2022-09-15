package ai.txai.commonview;

import android.content.Context;
import android.text.NoCopySpan;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.Locale;

import ai.txai.commonview.items.NoLineItem;

/**
 * Time: 2020-01-07
 * Author Hay
 */
public abstract class NoLineClickSpan extends ClickableSpan implements NoCopySpan {
    @Override
    public void updateDrawState(@NonNull TextPaint ds) {
        ds.setColor(ds.linkColor);
        ds.setUnderlineText(false);
    }

    public static void setClickableSpan(TextView tv, Context context, String message, NoLineItem... items) {
        final SpannableStringBuilder style = new SpannableStringBuilder();
        style.append(message);
        for (NoLineItem item : items) {
            int colorRes = item.colorRes;
            String focusMsg = item.focusMsg;
            int startIndex = message.toLowerCase().indexOf(focusMsg.toLowerCase());
            if (startIndex >= 0) {
                if (item.span != null) {
                    style.setSpan(item.span, startIndex, startIndex + focusMsg.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(context.getResources().getColor(colorRes));
                style.setSpan(foregroundColorSpan, startIndex, startIndex + focusMsg.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        tv.setText(style);
    }
}
