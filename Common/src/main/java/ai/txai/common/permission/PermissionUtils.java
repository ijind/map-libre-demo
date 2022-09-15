package ai.txai.common.permission;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.PowerManager;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import ai.txai.common.permission.runtime.Permission;
import ai.txai.common.permission.runtime.PermissionDef;
import ai.txai.common.utils.DeviceUtils;

/**
 * created by zhaoyuntao
 * on 2019-11-06
 * description:
 */
public class PermissionUtils {
    public static boolean isFirstRequest(@NonNull Context context, @NonNull String... permissions) {
        boolean firstRequest = true;
        for (String perm : permissions) {
            if (!PermissionStatePrefs.isFirstRequest(context, perm)) {
                firstRequest = false;
                break;
            }
        }
        return firstRequest;
    }

    public static void setRequested(@NonNull Context context, @NonNull String... permissions) {
        for (String perm : permissions) {
            PermissionStatePrefs.setRequested(context, perm);
        }
    }

    public static boolean isAlwaysDenied(@NonNull Context context, @NonNull String... permissions) {
        if (AndPermission.hasPermissions(context, permissions)) {
            return false;
        }
        if (!AndPermission.hasAlwaysDeniedPermission(context, permissions)) {
            return false;
        }
        return !isFirstRequest(context, permissions);
    }

    public static String[] getCalendarPermissions() {
        return new String[]{Permission.READ_CALENDAR, Permission.WRITE_CALENDAR};
    }

    /**
     * request the specified permission if app don't has it
     *
     * @param context
     * @param requestResult
     * @param permissions
     */
    public static void requestPermission(final Context context, final PermissionSettings permissionSettings, @NonNull final RequestResult requestResult, final @NonNull @PermissionDef String... permissions) {
        if (context == null) {
            requestResult.onDenied(Arrays.asList(permissions));
            return;
        }
        if (hasPermission(context, permissions)) {
            for (String permission : permissions) {
                AndPermission.setAlwaysDeniedPermissionBefore(context, permission, false);
            }
            requestResult.onGranted(Arrays.asList(permissions));
            return;
        }


        AndPermission.with(context)
                .runtime()
                .permission(permissions)
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> permissions) {
                        for (String permission : permissions) {
                            if (hasPermission(context, permission)) {
                                AndPermission.setAlwaysDeniedPermissionBefore(context, permission, false);
                            } else {
                                AndPermission.setAlwaysDeniedPermissionBefore(context, permission, true);
                            }
                        }
                        requestResult.onGranted(permissions);
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> permissionsList) {
                        PermissionSettings finalPermissionSettings = permissionSettings;
                        if (finalPermissionSettings == null) {
                            finalPermissionSettings = new PermissionSettings() {
                                @Override
                                public void whenDeny() {

                                }

                                @Override
                                public void whenContinue() {

                                }
                            };
                        }
                        finalPermissionSettings.setPermission(permissions);
                        if (AndPermission.hasAlwaysDeniedPermission(context, permissionsList)) {
                            finalPermissionSettings.setPermission(permissions);
                            finalPermissionSettings.setAlwaysDeny(true);
                            for (String permission : permissionsList) {
                                if (hasPermission(context, permission)) {
                                    AndPermission.setAlwaysDeniedPermissionBefore(context, permission, false);
                                } else {
                                    AndPermission.setAlwaysDeniedPermissionBefore(context, permission, true);
                                }
                            }
                            requestResult.onDeniedNotAsk(finalPermissionSettings);
                        } else {
                            finalPermissionSettings.setAlwaysDeny(false);
                            requestResult.onDenied(permissionsList);
                        }
                    }
                }).start();

        for (String perm : permissions) {
            PermissionStatePrefs.setRequested(context, perm);
        }
    }

    /**
     * request the apk install permission if app don't has it
     *
     * @param context
     * @param requestResult
     */
    public static void requestInstallPermission(final Context context, final File file, @NonNull final InstallRequestResult requestResult) {
        if (context == null) {
            requestResult.onDenied();
            return;
        }

        AndPermission.with(context).install().file(file).rationale(new Rationale<File>() {
            @Override
            public void showRationale(Context context, File data, RequestExecutor executor) {
                requestResult.showRationale(context, data, executor);
            }
        }).onGranted(new Action<File>() {
            @Override
            public void onAction(File data) {
                requestResult.onGranted();
            }
        }).onDenied(new Action<File>() {
            @Override
            public void onAction(File data) {
                requestResult.onDenied();
            }
        }).start();
    }

    public static void requestPermission(final Context context, @NonNull final RequestResult requestResult, final @NonNull @PermissionDef String... permissions) {
        requestPermission(context, null, requestResult, permissions);
    }

    /**
     * whether the app has the specified permission
     *
     * @param context
     * @param permissions
     * @return
     */
    public static boolean hasPermission(final Context context, @NonNull @PermissionDef String... permissions) {
        if (context == null) {
            return false;
        }
        return AndPermission.hasPermissions(context, permissions);
    }

    /**
     * request a specified permission with a notice for why you want this permission
     *
     * @param context
     * @param requestResultWithNotice
     * @param permissions
     */
    public static void requestPermissionWithNotice(final Context context, @NonNull final RequestResultWithNotice requestResultWithNotice, @NonNull @PermissionDef final String... permissions) {
        if (hasPermission(context, permissions)) {
            for (String permission : permissions) {
                AndPermission.setAlwaysDeniedPermissionBefore(context, permission, false);
            }
            requestResultWithNotice.onGranted(Arrays.asList(permissions));
        } else {
            PermissionSettings permissionSettings = new PermissionSettings() {
                @Override
                public void whenDeny() {

                }

                @Override
                public void whenContinue() {
                    requestPermission(context, requestResultWithNotice, permissions);
                }
            };
            permissionSettings.setPermission(permissions);
            permissionSettings.setAlwaysDeny(false);
            for (String permission : permissions) {
                if (!hasPermission(context, permission)) {
                    if (AndPermission.hasAlwaysDeniedPermission(context, permissions) && AndPermission.alwaysDeniedPermissionBefore(context, permission)) {
                        permissionSettings.setAlwaysDeny(true);
                    }
                }
            }
            requestResultWithNotice.onShowNotice(permissionSettings);
        }
    }

    /**
     * request camera permission
     *
     * @param context
     * @param requestResult
     */
    public static void requestCameraPermission(final Context context, final RequestResult requestResult) {
        requestPermission(context, requestResult, Permission.CAMERA);
    }

    /**
     * request camera permission with a notice
     *
     * @param context
     * @param requestResultWithNotice
     */
    public static void requestCameraPermissionWithNotice(final Context context, @NonNull final RequestResultWithNotice requestResultWithNotice) {
        requestPermissionWithNotice(context, requestResultWithNotice, Permission.CAMERA);
    }

    public static void requestAudioWithExternalPermissionWithNotice(Context context, RequestResultWithNotice requestResultWithNotice) {
        requestPermissionWithNotice(context, requestResultWithNotice, Permission.RECORD_AUDIO, Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE);
    }

    /**
     * request record audio permission
     *
     * @param context
     * @param requestResult
     */
    public static void requestAudioPermission(final Context context, final RequestResult requestResult) {
        requestPermission(context, requestResult, Permission.RECORD_AUDIO);
    }

    /**
     * request record audio permission with notice
     *
     * @param context
     * @param requestResultWithNotice
     */
    public static void requestAudioPermissionWithNotice(Context context, RequestResultWithNotice requestResultWithNotice) {
        requestPermissionWithNotice(context, requestResultWithNotice, Permission.RECORD_AUDIO);
    }

    /**
     * if has permission of RECORD_AUDIO
     *
     * @param context
     * @return
     */
    public static boolean hasAudioPermission(Context context) {
        return hasPermission(context, Permission.RECORD_AUDIO);
    }

    /**
     * if has permission of READ_SMS
     *
     * @param context
     * @return
     */
    public static boolean hasReadSMSPermission(Context context) {
        return hasPermission(context, Permission.READ_SMS);
    }

    public static void requestReadSMSPermissionWithNotice(Context context, RequestResultWithNotice requestResult) {
        requestPermissionWithNotice(context, requestResult, Permission.READ_SMS);
    }

    public static void requestReadSMSPermission(Context context, RequestResult requestResult) {
        requestPermission(context, requestResult, Permission.READ_SMS);
    }

    /**
     * request write and read file permissions
     *
     * @param context
     * @param requestResult
     */
    public static void requestExternalPermission(final Context context, final RequestResult requestResult) {
        requestPermission(context, requestResult, Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE);
    }

    public static void requestExternalPermissionWithNotice(final Context context, final RequestResultWithNotice requestResult) {
        requestPermissionWithNotice(context, requestResult, Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE);
    }

    public static void requestCalendarPermission(final Context context, final RequestResult requestResult) {
        requestPermission(context, requestResult, getCalendarPermissions());
    }

    public static boolean hasContactsPermission(Context context) {
        return hasPermission(context, Permission.READ_CONTACTS);
    }

    public static void requestContactsPermissionWithNotice(Context context, RequestResultWithNotice requestResultWithNotice) {
        requestPermissionWithNotice(context, requestResultWithNotice, Permission.READ_CONTACTS, Permission.WRITE_CONTACTS);
    }

    public static void requestAVRPermission(Context context, RequestResultWithNotice requestResultWithNotice) {
        requestPermissionWithNotice(context, requestResultWithNotice,
                Permission.WRITE_EXTERNAL_STORAGE,
                Permission.READ_EXTERNAL_STORAGE,
                Permission.READ_PHONE_STATE);
    }

    public static boolean hasFineLocationPermission(Context context) {
        return hasPermission(context, Permission.ACCESS_FINE_LOCATION);
    }

    public static void requestFineLocationPermission(Context context, RequestResult requestResult) {
        requestPermission(context, requestResult, Permission.ACCESS_FINE_LOCATION);
    }

    public static void requestFineLocationPermissionWithNotice(Context context, RequestResultWithNotice requestResultWithNotice) {
        requestPermissionWithNotice(context, requestResultWithNotice, Permission.ACCESS_FINE_LOCATION);
    }

    public static boolean hasCameraPermission(Context context) {
        return hasPermission(context, Permission.CAMERA);
    }

    public static boolean hasExternalPermission(Context context) {
        return hasPermission(context, Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE);
    }

    public static void requestCameraAndAudioPermissionWithNotice(Context context, RequestResultWithNotice requestResultWithNotice) {
        requestPermissionWithNotice(context, requestResultWithNotice, Permission.CAMERA, Permission.RECORD_AUDIO);
    }

    public static void requestCameraAndAudioPermission(Context context, RequestResult requestResult) {
        requestPermission(context, requestResult, Permission.CAMERA, Permission.RECORD_AUDIO);
    }

    public static void requestCameraAndAudioAndWriteExternalPermissionWithNotice(Context context, RequestResultWithNotice requestResultWithNotice) {
        requestPermissionWithNotice(context, requestResultWithNotice, Permission.CAMERA, Permission.RECORD_AUDIO, Permission.WRITE_EXTERNAL_STORAGE, Permission.READ_EXTERNAL_STORAGE);
    }

    public static void requestCameraAndWriteExternalPermissionWithNotice(Context context, RequestResultWithNotice requestResultWithNotice) {
        requestPermissionWithNotice(context, requestResultWithNotice, Permission.CAMERA, Permission.WRITE_EXTERNAL_STORAGE, Permission.READ_EXTERNAL_STORAGE);
    }

    public static void requestVideoRecordPermission(Context context, RequestResult requestResult) {
        requestPermission(context, requestResult, Permission.CAMERA, Permission.RECORD_AUDIO, Permission.READ_PHONE_STATE);
    }

    public static void requestAudioRecordPermission(Context context, RequestResult requestResult) {
        requestPermission(context, requestResult, Permission.RECORD_AUDIO, Permission.READ_PHONE_STATE);
    }

    public static boolean requestAudioRecordPermission(Activity activity) {
        if (DeviceUtils.INSTANCE.marshmallowDevices()) {
            if (activity == null || activity.isFinishing()) {
                return false;
            }
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(activity,
                        Manifest.permission.RECORD_AUDIO)) {
                    return false;
                }
            } else {
                return true;
            }
        }

        AudioRecord audioRec = null;
        try {
            int minBufSize = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);
            if (minBufSize == AudioRecord.ERROR_BAD_VALUE || minBufSize == AudioRecord.ERROR) {
                // Get min buffer size error!
                return false;
            }
            audioRec = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, 8000, AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, 2 * minBufSize);
            int sleepCount = 0;
            while (audioRec.getState() != AudioRecord.STATE_INITIALIZED) {
                if (sleepCount >= 2) {
                    break;
                }
                SystemClock.sleep(100);
                sleepCount++;
            }
            audioRec.startRecording();
            byte[] buffer = new byte[minBufSize];
            long curSysTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - curSysTime < 500l) { // Recording 500 Millisecond.
                audioRec.read(buffer, 0, minBufSize);
                final int bufSize = buffer.length;
                for (int i = 0; i < bufSize; i++) {
                    if (buffer[i] != 0) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            // e.printStackTrace();
        } finally {
            if (audioRec != null) {
                audioRec.release();
            }
        }
        return false;
    }

    public static void requestBatteryWhitePermission(Context context, @NonNull BatteryRequestResult requestResult) {
        if (context == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            boolean isInWhiteList = pm.isIgnoringBatteryOptimizations(context.getPackageName());

            if (isInWhiteList) {
                requestResult.isInWhiteList();
            } else {
                requestResult.isNotInWhiteList();
            }
        }
    }

    public interface BatteryRequestResult {
        void isInWhiteList();

        void isNotInWhiteList();
    }

    public interface RequestResult {
        void onGranted(List<String> permissions);

        void onDenied(List<String> permissions);

        void onDeniedNotAsk(PermissionSettings permissionSettings);
    }

    public interface InstallRequestResult {
        void onGranted();

        void onDenied();

        void showRationale(Context context, File data, RequestExecutor executor);
    }

    public interface RequestResultWithNotice extends RequestResult {

        void onShowNotice(PermissionSettings permissionSettings);
    }

}
