package ai.txai.commonbiz.main;

import android.app.Activity;
import android.view.View;

import java.lang.reflect.Constructor;

/**
 * Time: 28/02/2022
 * Author Hay
 */
public class MainBottomViewHelper {

    private <T extends View> T realCreateView(Activity activity, Class<T> clazz) {
        if (clazz == null) {
            return null;
        }
        Constructor<?>[] constructors = clazz.getConstructors();
        Constructor constructor = null;
        for (Constructor<?> it : constructors) {
            Class<?>[] parameterTypes = it.getParameterTypes();
            if (parameterTypes.length == 1) {
                constructor = it;
                break;
            }
        }
        if (constructor != null) {
            try {
                return (T) constructor.newInstance(activity);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    public <T extends View> T getView(Activity activity, Class<T> clazz) {
        return (T) realCreateView(activity, clazz);
    }
}
