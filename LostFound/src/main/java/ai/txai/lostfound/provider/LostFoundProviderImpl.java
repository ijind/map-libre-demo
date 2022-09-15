package ai.txai.lostfound.provider;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.alibaba.android.arouter.facade.annotation.Route;

import ai.txai.common.router.ARouterConstants;
import ai.txai.common.router.ARouterUtils;
import ai.txai.common.router.provider.LostFoundProvider;
import ai.txai.lostfound.activity.LostFoundMainActivity;

/**
 * Time: 22/04/2022
 * Author Hay
 */
@Route(path = ARouterConstants.PATH_SERVICE_LOST_FOUND)
public class LostFoundProviderImpl implements LostFoundProvider {
    private static final String TAG = "LostFoundProviderImpl";

    @Override
    public void triggerLostFound(Activity activity, String orderId) {
        Bundle args = new Bundle();
        args.putString(LostFoundMainActivity.EXTRA_ORDER_ID, orderId);
        ARouterUtils.navigation(activity, ARouterConstants.PATH_ACTIVITY_LF, args);
    }

    @Override
    public boolean enable() {
        return true;
    }

    @Override
    public void init(Context context) {

    }
}
