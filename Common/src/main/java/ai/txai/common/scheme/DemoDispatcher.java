package ai.txai.common.scheme;

import android.app.Activity;

import androidx.annotation.NonNull;

/**
 * Time: 27/05/2022
 * Author Hay
 */
public class DemoDispatcher extends ISchemeDispatcher {
    @Override
    public String uriString() {
        return "txai://demo";
    }

    @Override
    public boolean dispatch(Activity activity) {
        return false;
    }

    @NonNull
    @Override
    public String toString() {
        return mUri.toString();
    }
}
