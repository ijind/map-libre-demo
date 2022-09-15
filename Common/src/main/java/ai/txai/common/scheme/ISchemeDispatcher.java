package ai.txai.common.scheme;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import java.util.HashMap;
import java.util.Map;

/**
 * Time: 2020-02-12
 * Author Hay
 */
public abstract class ISchemeDispatcher {
    protected Uri mUri;
    protected Map<String, Object> mMap = new HashMap<>();

    private void setUri(Uri uri) {
        mUri = uri;
    }

    public void setUri(Uri uri, Intent intent) {
        setUri(uri);
    }

    public void putExtra(String key, Object value) {
        mMap.put(key, value);
    }

    public abstract String uriString();

    public abstract boolean dispatch(Activity activity);
}