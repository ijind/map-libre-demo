package ai.txai.common.router;

import android.text.TextUtils;

import com.alibaba.android.arouter.facade.template.IProvider;
import com.alibaba.android.arouter.launcher.ARouter;

/**
 * Time: 22/04/2022
 * Author Hay
 */
public class ProviderManager {
    private static final String TAG = "ProviderManager";

    public static <T extends IProvider> T provide(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        try {
            return (T) ARouter.getInstance().build(path).navigation();
        } catch (Exception e) {
            return null;
        }
    }

    public static <T extends IProvider> T provide(Class<T> clz) {
        return ARouter.getInstance().navigation(clz);
    }
}
