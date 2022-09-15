package ai.txai.common.router.provider;

import android.app.Activity;

import com.alibaba.android.arouter.facade.template.IProvider;

/**
 * Time: 09/05/2022
 * Author Hay
 */
public interface LostFoundProvider extends IProvider {
    void triggerLostFound(Activity activity, String orderId);

    boolean enable();
}
