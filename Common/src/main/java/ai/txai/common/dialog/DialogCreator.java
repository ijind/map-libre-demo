package ai.txai.common.dialog;

import android.app.Activity;
import android.view.View;

/**
 * Time: 04/04/2022
 * Author Hay
 */
public class DialogCreator {
    public static ConfirmDialog showConfirmDialog(Activity activity, String text, View.OnClickListener listener) {
        ConfirmDialog dialog = new ConfirmDialog(activity, text, true);
        dialog.setOkListener(listener);
        dialog.showPop();
        return dialog;
    }

    public static TwoSelectDialog showTwoSelectDialog(Activity activity, String title, String cancel, String ok, TwoSelectDialog.OnClickListener listener) {
        TwoSelectDialog dialog = new TwoSelectDialog(activity, listener, title, cancel, ok);
        dialog.showPop();
        return dialog;
    }

    public static TwoSelectWithTitleDialog showTwoSelectWithTitleDialog(Activity activity, String title, String content, String cancel, String ok, TwoSelectWithTitleDialog.OnClickListener listener) {
        TwoSelectWithTitleDialog dialog = new TwoSelectWithTitleDialog(activity, listener, title, content, cancel, ok);
        dialog.showPop();
        return dialog;
    }


    public static ConfirmWithTitleAndIconDialog showConfirmDialog(Activity activity, int iconRes, String title, String text, View.OnClickListener listener) {
        ConfirmWithTitleAndIconDialog dialog = new ConfirmWithTitleAndIconDialog(activity,iconRes, title, text, true);
        dialog.setOkListener(listener);
        dialog.showPop();
        return dialog;
    }
}
