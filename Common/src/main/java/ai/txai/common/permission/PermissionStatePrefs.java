package ai.txai.common.permission;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

class PermissionStatePrefs {
    private static final String PREF_NAME = "perms_request_state";
    private static final String KEY_PREFIX_FIRST_REQUEST = "first_request_";

    private static SharedPreferences getPrefs(@NonNull Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static boolean isFirstRequest(@NonNull Context context, @NonNull String permission) {
        return getPrefs(context).getBoolean(KEY_PREFIX_FIRST_REQUEST + permission, true);
    }

    public static void setRequested(@NonNull Context context, @NonNull String permission) {
        getPrefs(context).edit().putBoolean(KEY_PREFIX_FIRST_REQUEST + permission, false).apply();
    }
}
