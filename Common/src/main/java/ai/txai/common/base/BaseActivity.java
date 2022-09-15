package ai.txai.common.base;

import android.app.Activity;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

import ai.txai.common.R;
import ai.txai.common.manager.LifeCycleManager;
import ai.txai.common.network.NetUtils;
import ai.txai.common.network.NetworkObserver;
import ai.txai.common.utils.ReflectionUtils;
import ai.txai.common.utils.ToastUtils;

/**
 * Time: 2/17/22
 * Author Hay
 */
public class BaseActivity extends RxAppCompatActivity implements NetworkObserver {
    public static Activity getLastActivity() {
        if (!ThreadUtils.isMainThread()) {
            throw new RuntimeException("must call this method in main thread");
        }
        return ActivityUtils.getTopActivity();
    }

    public static void finishAllActivities() {
        ActivityUtils.finishAllActivities();
    }

    public static boolean containActivity(Class claszz) {
        return ActivityUtils.isActivityExistsInStack(claszz);
    }

    @Override
    public void onRefresh() {

    }

    @Override
    public void onConnected(@Nullable NetworkUtils.NetworkType networkType) {

    }

    @Override
    public void onDisconnected() {
        final Activity curActivity = LifeCycleManager.INSTANCE.getCurActivity();
        if (curActivity != null &&
                ReflectionUtils.hasMethod(curActivity.getClass(), "onDisconnected")) {
            ((NetworkObserver) curActivity).onDisconnected();
            return;
        }

        if (curActivity instanceof BaseScrollActivity) {
            ((BaseScrollActivity<?, ?>) curActivity).onDisconnected();
            return;
        }

        if (NetUtils.INSTANCE.isShowToastWhenNoInternet()) {
            final String message = getResources().getString(R.string.commonview_no_internet_prompt);
            ToastUtils.INSTANCE.show(message, false);
        }
    }
}
