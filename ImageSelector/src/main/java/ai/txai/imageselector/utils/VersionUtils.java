package ai.txai.imageselector.utils;

import android.os.Build;

import androidx.annotation.ChecksSdkIntAtLeast;

public class VersionUtils {

    /**
     * 判断是否是Android L版本
     *
     * @return
     */
    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.M)
    public static boolean isAndroidM() {
        return Build.VERSION.SDK_INT >=Build.VERSION_CODES.M;
    }

    /**
     * 判断是否是Android N版本
     *
     * @return
     */
    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.N)
    public static boolean isAndroidN() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
    }

    /**
     * 判断是否是Android P版本
     *
     * @return
     */
    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.P)
    public static boolean isAndroidP() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P;
    }

    /**
     * 判断是否是Android Q版本
     *
     * @return
     */
    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.Q)
    public static boolean isAndroidQ() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
    }
}
