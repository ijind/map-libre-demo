package ai.txai.common.permission;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import ai.txai.common.permission.checker.DoubleChecker;
import ai.txai.common.permission.checker.PermissionChecker;
import ai.txai.common.permission.option.Option;
import ai.txai.common.permission.source.ActivitySource;
import ai.txai.common.permission.source.ContextSource;
import ai.txai.common.permission.source.Source;
import ai.txai.common.permission.source.XFragmentSource;

import java.io.File;
import java.util.List;

public class AndPermission {

    private static final String ACTION_BRIDGE_SUFFIX = ".andpermission.bridge";

    public static String bridgeAction(Context context) {
        return context.getPackageName() + ACTION_BRIDGE_SUFFIX;
    }

    /**
     * With context.
     *
     * @param context {@link Context}.
     * @return {@link Option}.
     */
    public static Option with(Context context) {
        return new Boot(getContextSource(context));
    }

    /**
     * With {@link Fragment}.
     *
     * @param fragment {@link Fragment}.
     * @return {@link Option}.
     */
    public static Option with(Fragment fragment) {
        return new Boot(new XFragmentSource(fragment));
    }

//    /**
//     * With {@link android.support.v4.app.Fragment}.
//     *
//     * @param fragment {@link android.support.v4.app.Fragment}.
//     * @return {@link Option}.
//     */
//    public static Option with(android.support.v4.app.Fragment fragment) {
//        return new Boot(new XFragmentSource(fragment));
//    }

    /**
     * With activity.
     *
     * @param activity {@link AppCompatActivity}.
     * @return {@link Option}.
     */
    public static Option with(AppCompatActivity activity) {
        return new Boot(new ActivitySource(activity));
    }

    /**
     * Some privileges permanently disabled, may need to set up in the execute.
     *
     * @param context           {@link Context}.
     * @param deniedPermissions one or more permissions.
     * @return true, other wise is false.
     */
    public static boolean hasAlwaysDeniedPermission(Context context, List<String> deniedPermissions) {
        return hasAlwaysDeniedPermission(getContextSource(context), deniedPermissions);
    }

    /**
     * Some privileges permanently disabled, may need to set up in the execute.
     *
     * @param fragment          {@link Fragment}.
     * @param deniedPermissions one or more permissions.
     * @return true, other wise is false.
     */
    public static boolean hasAlwaysDeniedPermission(Fragment fragment, List<String> deniedPermissions) {
        return hasAlwaysDeniedPermission(new XFragmentSource(fragment), deniedPermissions);
    }

//    /**
//     * Some privileges permanently disabled, may need to set up in the execute.
//     *
//     * @param fragment          {@link android.support.v4.app.Fragment}.
//     * @param deniedPermissions one or more permissions.
//     * @return true, other wise is false.
//     */
//    public static boolean hasAlwaysDeniedPermission(android.support.v4.app.Fragment fragment, List<String> deniedPermissions) {
//        return hasAlwaysDeniedPermission(new XFragmentSource(fragment), deniedPermissions);
//    }

    /**
     * Some privileges permanently disabled, may need to set up in the execute.
     *
     * @param activity          {@link AppCompatActivity}.
     * @param deniedPermissions one or more permissions.
     * @return true, other wise is false.
     */
    public static boolean hasAlwaysDeniedPermission(AppCompatActivity activity, List<String> deniedPermissions) {
        return hasAlwaysDeniedPermission(new ActivitySource(activity), deniedPermissions);
    }

    /**
     * Has always been denied permission.
     */
    private static boolean hasAlwaysDeniedPermission(Source source, List<String> deniedPermissions) {

        return hasAlwaysDeniedPermission(source, deniedPermissions.toArray(new String[0]));
    }

    /**
     * Some privileges permanently disabled, may need to set up in the execute.
     *
     * @param context           {@link Context}.
     * @param deniedPermissions one or more permissions.
     * @return true, other wise is false.
     */
    public static boolean hasAlwaysDeniedPermission(Context context, String... deniedPermissions) {
        return hasAlwaysDeniedPermission(getContextSource(context), deniedPermissions);
    }

    /**
     * Some privileges permanently disabled, may need to set up in the execute.
     *
     * @param fragment          {@link Fragment}.
     * @param deniedPermissions one or more permissions.
     * @return true, other wise is false.
     */
    public static boolean hasAlwaysDeniedPermission(Fragment fragment, String... deniedPermissions) {
        return hasAlwaysDeniedPermission(new XFragmentSource(fragment), deniedPermissions);
    }

//    /**
//     * Some privileges permanently disabled, may need to set up in the execute.
//     *
//     * @param fragment          {@link android.support.v4.app.Fragment}.
//     * @param deniedPermissions one or more permissions.
//     * @return true, other wise is false.
//     */
//    public static boolean hasAlwaysDeniedPermission(android.support.v4.app.Fragment fragment, String... deniedPermissions) {
//        return hasAlwaysDeniedPermission(new XFragmentSource(fragment), deniedPermissions);
//    }

    /**
     * Some privileges permanently disabled, may need to set up in the execute.
     *
     * @param activity          {@link AppCompatActivity}.
     * @param deniedPermissions one or more permissions.
     * @return true, other wise is false.
     */
    public static boolean hasAlwaysDeniedPermission(AppCompatActivity activity, String... deniedPermissions) {
        return hasAlwaysDeniedPermission(new ActivitySource(activity), deniedPermissions);
    }

    /**
     * Has always been denied permission.
     */
    private static boolean hasAlwaysDeniedPermission(Source source, String... deniedPermissions) {
        for (String permission : deniedPermissions) {
            if (!source.isShowRationalePermission(permission)) {
                return true;
            }
        }
        return false;
    }

    public static boolean alwaysDeniedPermissionBefore(Context context, String deniedPermissions) {
        int mode = Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS;
        SharedPreferences pref = context.getSharedPreferences(ACTION_BRIDGE_SUFFIX, mode);
        return pref.getBoolean(deniedPermissions, false);
    }

    public static void setAlwaysDeniedPermissionBefore(Context context, String deniedPermissions, boolean alwaysDeniedPermissionBefore) {
        int mode = Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS;
        SharedPreferences pref = context.getSharedPreferences(ACTION_BRIDGE_SUFFIX, mode);
        pref.edit().putBoolean(deniedPermissions, alwaysDeniedPermissionBefore).apply();
    }

    /**
     * Classic permission checker.
     */
    private static final PermissionChecker PERMISSION_CHECKER = new DoubleChecker();

    /**
     * Judgment already has the target permission.
     *
     * @param context     {@link Context}.
     * @param permissions one or more permissions.
     * @return true, other wise is false.
     */
    public static boolean hasPermissions(Context context, String... permissions) {
        return PERMISSION_CHECKER.hasPermission(context, permissions);
    }

    /**
     * Judgment already has the target permission.
     *
     * @param fragment    {@link Fragment}.
     * @param permissions one or more permissions.
     * @return true, other wise is false.
     */
    public static boolean hasPermissions(Fragment fragment, String... permissions) {
        return hasPermissions(fragment.getActivity(), permissions);
    }

//    /**
//     * Judgment already has the target permission.
//     *
//     * @param fragment    {@link android.support.v4.app.Fragment}.
//     * @param permissions one or more permissions.
//     * @return true, other wise is false.
//     */
//    public static boolean hasPermissions(android.support.v4.app.Fragment fragment, String... permissions) {
//        return hasPermissions(fragment.getActivity(), permissions);
//    }

    /**
     * Judgment already has the target permission.
     *
     * @param activity    {@link AppCompatActivity}.
     * @param permissions one or more permissions.
     * @return true, other wise is false.
     */
    public static boolean hasPermissions(AppCompatActivity activity, String... permissions) {
        return PERMISSION_CHECKER.hasPermission(activity, permissions);
    }

    /**
     * Judgment already has the target permission.
     *
     * @param context     {@link Context}.
     * @param permissions one or more permission groups.
     * @return true, other wise is false.
     */
    public static boolean hasPermissions(Context context, String[]... permissions) {
        for (String[] permission : permissions) {
            boolean hasPermission = PERMISSION_CHECKER.hasPermission(context, permission);
            if (!hasPermission) return false;
        }
        return true;
    }

    /**
     * Judgment already has the target permission.
     *
     * @param fragment    {@link Fragment}.
     * @param permissions one or more permission groups.
     * @return true, other wise is false.
     */
    public static boolean hasPermissions(Fragment fragment, String[]... permissions) {
        return hasPermissions(fragment.getActivity(), permissions);
    }

//    /**
//     * Judgment already has the target permission.
//     *
//     * @param fragment    {@link android.support.v4.app.Fragment}.
//     * @param permissions one or more permission groups.
//     * @return true, other wise is false.
//     */
//    public static boolean hasPermissions(android.support.v4.app.Fragment fragment, String[]... permissions) {
//        return hasPermissions(fragment.getActivity(), permissions);
//    }

    /**
     * Judgment already has the target permission.
     *
     * @param activity    {@link AppCompatActivity}.
     * @param permissions one or more permission groups.
     * @return true, other wise is false.
     */
    public static boolean hasPermissions(AppCompatActivity activity, String[]... permissions) {
        for (String[] permission : permissions) {
            boolean hasPermission = PERMISSION_CHECKER.hasPermission(activity, permission);
            if (!hasPermission) return false;
        }
        return true;
    }

    /**
     * Get compatible Android 7.0 and lower versions of Uri.
     *
     * @param context {@link Context}.
     * @param file    apk file.
     * @return uri.
     */
    public static Uri getFileUri(Context context, File file) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
        }
        return Uri.fromFile(file);
    }

//    /**
//     * Get compatible Android 7.0 and lower versions of Uri.
//     *
//     * @param fragment {@link Fragment}.
//     * @param file     apk file.
//     * @return uri.
//     */
//    public static Uri getFileUri(Fragment fragment, File file) {
//        return getFileUri(fragment.getContext(), file);
//    }

//    /**
//     * Get compatible Android 7.0 and lower versions of Uri.
//     *
//     * @param fragment {@link android.support.v4.app.Fragment}.
//     * @param file     apk file.
//     * @return uri.
//     */
//    public static Uri getFileUri(android.support.v4.app.Fragment fragment, File file) {
//        return getFileUri(fragment.getActivity(), file);
//    }

    private static Source getContextSource(Context context) {
        if (context instanceof AppCompatActivity) {

            return new ActivitySource((AppCompatActivity) context);
        } else if (context instanceof ContextWrapper) {

            return getContextSource(((ContextWrapper) context).getBaseContext());
        }

        return new ContextSource(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static boolean neverAskAgainSelected(final Activity activity, final String permission) {
        final boolean prevShouldShowStatus = getRatinaleDisplayStatus(activity, permission);
        final boolean currShouldShowStatus = activity.shouldShowRequestPermissionRationale(permission);
        return prevShouldShowStatus != currShouldShowStatus;
    }

    public static void setShouldShowStatus(final Context context, final String permission) {
        SharedPreferences genPrefs = context.getSharedPreferences("GENERIC_PREFERENCES", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = genPrefs.edit();
        editor.putBoolean(permission, true);
        editor.apply();
    }

    public static boolean getRatinaleDisplayStatus(final Context context, final String permission) {
        SharedPreferences genPrefs = context.getSharedPreferences("GENERIC_PREFERENCES", Context.MODE_PRIVATE);
        return genPrefs.getBoolean(permission, false);
    }
}