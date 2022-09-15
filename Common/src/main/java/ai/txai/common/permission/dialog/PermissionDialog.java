package ai.txai.common.permission.dialog;

import android.app.Activity;
import android.content.res.Resources;

import com.blankj.utilcode.util.ThreadUtils;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashSet;

import ai.txai.common.R;
import ai.txai.common.dialog.DialogCreator;
import ai.txai.common.dialog.TwoSelectWithTitleDialog;
import ai.txai.common.log.LOG;
import ai.txai.common.permission.PermissionSettings;
import ai.txai.common.permission.runtime.Permission;

/**
 * only used to request permission
 */
public class PermissionDialog {

    public static WeakReference<TwoSelectWithTitleDialog> dialogWeakReference;

    public static void show(final Activity context, final PermissionSettings permissionSettings, final String title, final String message, final String negative, final String positive) {
        if (context == null) {
            return;
        }
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                DialogCreator.showTwoSelectWithTitleDialog(context, title, message, negative, positive, new TwoSelectWithTitleDialog.OnClickListener() {
                    @Override
                    public void onPositive() {
                        permissionSettings.goToAppSettings(context);
                    }

                    @Override
                    public void onNegative() {

                    }
                });
            }
        });
    }

    public static void show(final Activity context, final PermissionSettings permissionSettings, int titleStringId, int messageStringId) {
        if (context == null) {
            return;
        }
        Resources resources = context.getResources();
        if (resources == null) {
            return;
        }
        show(context, permissionSettings, resources.getString(titleStringId), resources.getString(messageStringId));
    }

    public static void show(final Activity context, final PermissionSettings permissionSettings, String title, String message) {
        if (context == null) {
            return;
        }
        Resources resources = context.getResources();
        if (resources == null) {
            return;
        }
        show(context, permissionSettings, title, message, resources.getString(R.string.commonview_cancel), resources.getString(R.string.commonview_settings));
    }

    public synchronized static void show(final Activity context, final PermissionSettings permissionSettings) {
        if (dialogWeakReference != null) {
            try {
                TwoSelectWithTitleDialog dialog = dialogWeakReference.get();
                if (dialog != null) {
                    dialog.dismiss();
                }
            } catch (Throwable e) {
                LOG.e("Permission", " permission dialog dismiss failed:" + e.getMessage());
            }
            dialogWeakReference = null;
        }
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                _show(context, permissionSettings);
            }
        });
    }

    private static int[] getPermissionTitleAndDescription(String permission, boolean isAlwaysDeny) {
        int result[] = new int[2];
        switch (permission) {
            case Permission.CAMERA:
                result[0] = isAlwaysDeny ? R.string.commonview_enable_camera_title : R.string.commonview_enable_camera_title;
                result[1] = isAlwaysDeny ? R.string.commonview_settings_camera_explain : R.string.commonview_settings_camera_explain;
                break;
            case Permission.ACCESS_FINE_LOCATION:
                result[0] = isAlwaysDeny ? R.string.commonview_enable_location_title : R.string.commonview_enable_location_title;
                result[1] = isAlwaysDeny ? R.string.commonview_settings_location_explain : R.string.commonview_settings_location_explain;
                break;
        }
        return result;
    }

    private static void _show(final Activity context, final PermissionSettings permissionSettings) {
        if (context == null) {
            return;
        }
        if (permissionSettings == null) {
            return;
        }
        HashSet<String> permissions = new HashSet<>(Arrays.asList(permissionSettings.getPermission()));
        if (permissions.size() <= 0) {
            return;
        }
        final boolean isAlwaysDeny = permissionSettings.isAlwaysDeny();
        int descriptionNormalResId = permissionSettings.getDescriptionNormal();
        int descriptionAlwaysDenyResId = permissionSettings.getDescriptionAlwaysDeny();
        boolean hasCustomDescription = isAlwaysDeny ? (descriptionAlwaysDenyResId != 0) : (descriptionNormalResId != 0);

        HashSet<Integer> message = new HashSet<>();
        if (hasCustomDescription) {
            message.add(isAlwaysDeny ? descriptionAlwaysDenyResId : descriptionNormalResId);
        }
        int negative = R.string.commonview_cancel;
        int positive = isAlwaysDeny ? R.string.commonview_settings : R.string.commonview_settings;

        String title = "";
        for (String permissionTmp : permissions) {
            int[] result = getPermissionTitleAndDescription(permissionTmp, isAlwaysDeny);
            if (!hasCustomDescription) {
                message.add(result[1]);
            }
            title = context.getString(result[0]);
        }

        if (message.isEmpty()) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (Integer integer : message) {
            sb.append(context.getString(integer));
        }

        TwoSelectWithTitleDialog dialog = DialogCreator.showTwoSelectWithTitleDialog(context, title, sb.toString(),
                context.getString(negative),
                context.getString(positive),
                new TwoSelectWithTitleDialog.OnClickListener() {
                    @Override
                    public void onPositive() {
                        permissionSettings.performClickPositive(isAlwaysDeny);
                        if (isAlwaysDeny) {
                            permissionSettings.goToAppSettings(context);
                        } else {
                            permissionSettings.whenContinue();
                        }
                    }

                    @Override
                    public void onNegative() {
                        permissionSettings.whenDeny();
                        permissionSettings.performClickNegative();
                    }
                });
        dialogWeakReference = new WeakReference<>(dialog);
    }
}
