package ai.txai.common.router;

import android.app.Activity;
import android.os.Bundle;

import com.alibaba.android.arouter.launcher.ARouter;

import ai.txai.common.R;
import ai.txai.common.base.BaseActivity;
import ai.txai.common.base.ParameterField;

/**
 * Time: 2020/8/2
 * Author Hay
 */
public class ARouterUtils {
    public static void navigation(Activity activity, String path) {
        navigation(activity, path, new Bundle());
    }

    public static void navigation(Activity activity, int flag, String path, Bundle args) {
        if (args == null) {
            args = new Bundle();
        }
        if (isActivity(path)) {
            ARouter.getInstance().build(path).with(args).withFlags(flag)
                    .withTransition(R.anim.commonview_page_right_enter,
                            R.anim.commonview_page_left_leave).
                    navigation(activity == null ?
                            BaseActivity.getLastActivity() : activity);
        } else {
            args.putString(ParameterField.AROUTER_PATH, path);
            ARouter.getInstance().build(ARouterConstants.PATH_ACTIVITY_CONTAINER)
                    .with(args).withTransition(R.anim.commonview_page_right_enter,
                    R.anim.commonview_page_left_leave).navigation(activity == null ?
                    BaseActivity.getLastActivity() : activity);
        }
    }

    public static void navigation(Activity activity, String path, Bundle args) {
        if (args == null) {
            args = new Bundle();
        }
        if (isActivity(path)) {
            ARouter.getInstance().build(path).with(args).withTransition(R.anim.commonview_page_right_enter, R.anim.commonview_page_left_leave).navigation(activity == null ?
                    BaseActivity.getLastActivity() : activity);
        } else {
            args.putString(ParameterField.AROUTER_PATH, path);
            ARouter.getInstance().build(ARouterConstants.PATH_ACTIVITY_CONTAINER).with(args).withTransition(R.anim.commonview_page_right_enter, R.anim.commonview_page_left_leave).navigation(activity == null ? BaseActivity.getLastActivity() : activity);
        }
    }

    private static boolean isActivity(String path) {
        return path.toLowerCase().endsWith("activity");
    }

    public static void navigationWithBundleForResult(Activity activity, String path, Bundle args, int requestCode) {
        if (args == null) {
            args = new Bundle();
        }
        if (isActivity(path)) {
            ARouter.getInstance().build(path).with(args).withTransition(R.anim.commonview_page_right_enter, R.anim.commonview_page_left_leave).navigation(activity, requestCode);
        } else {
            args.putString(ParameterField.AROUTER_PATH, path);
            ARouter.getInstance().build(ARouterConstants.PATH_ACTIVITY_CONTAINER).with(args).withTransition(R.anim.commonview_page_right_enter, R.anim.commonview_page_left_leave).navigation(activity, requestCode);
        }
    }
}
