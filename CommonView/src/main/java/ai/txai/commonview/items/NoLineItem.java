package ai.txai.commonview.items;

import ai.txai.commonview.NoLineClickSpan;

/**
 * Time: 2020/8/17
 * Author Hay
 */
public class NoLineItem {
    public int colorRes;
    public String focusMsg;
    public NoLineClickSpan span;

    public NoLineItem(int colorRes, String focusMsg, NoLineClickSpan span) {
        this.colorRes = colorRes;
        this.focusMsg = focusMsg;
        this.span = span;
    }
}
