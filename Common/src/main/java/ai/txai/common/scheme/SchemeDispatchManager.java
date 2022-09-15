package ai.txai.common.scheme;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.Nullable;

import java.util.HashMap;

import ai.txai.common.log.LOG;

/**
 * Time: 2020-02-12
 * Author Hay
 */
public class SchemeDispatchManager {
    public static final String TAG = "SchemeDispatch";
    private ISchemeDispatcher dispatcher;
    private final HashMap<String, ISchemeDispatcher> mSchemeMap = new HashMap<>();

    private SchemeDispatchManager() {
    }

    private static class DispatcherHolder {
        private static final SchemeDispatchManager instance = new SchemeDispatchManager();
    }

    public static SchemeDispatchManager getInstance() {
        return DispatcherHolder.instance;
    }

    private String formatModelName(Uri uri) {
        return String.format("%s://%s.txai.ai", uri.getScheme(), uri.getHost());
    }

    private ISchemeDispatcher matchDispatcher(Uri uri) {
        if (uri == null
                || uri.getScheme() == null
                || uri.getHost() == null) {
            return null;
        }
        return mSchemeMap.get(formatModelName(uri));
    }


    public void setUri(Uri uri, @Nullable Intent intent) {
        LOG.i("Scheme", "setUri %s", uri);
        if (uri == null) {
            dispatcher = null;
            return;
        }
        dispatcher = matchDispatcher(uri);
        if (dispatcher != null) {
            dispatcher.setUri(uri, intent);
        }
    }

    public void setUri(Uri uri) {
        if (uri == null) {
            dispatcher = null;
            return;
        }
        dispatcher = matchDispatcher(uri);
        if (dispatcher != null) {
            dispatcher.setUri(uri, null);
        }
    }


    public boolean isSupportUri(Uri uri) {
        return matchDispatcher(uri) != null;
    }

    public boolean dispatch(Activity activity) {
        if (dispatcher != null) {
            boolean result = dispatcher.dispatch(activity);
            setUri(null, null);
            return result;
        }
        return false;
    }

    /**
     * 所有支持的scheme注册入口.
     */
    public void register(ISchemeDispatcher intercept) {
        String formatHost = formatModelName(Uri.parse(intercept.uriString()));
        LOG.i(TAG, "register name %s", formatHost);
        if (!mSchemeMap.containsKey(formatHost)) {
            mSchemeMap.put(formatHost, intercept);
        }
    }

    public void init(Application application) {
        register(new DemoDispatcher());
    }
}
