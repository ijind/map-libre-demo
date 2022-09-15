package ai.txai.common.utils

import ai.txai.common.api.ApiConfig
import ai.txai.common.log.LOG
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import java.util.*

object DeviceUtils {
    private const val TAG = "DeviceUtils"
    private var deviceId = ""

    fun initDeviceId(context: Context) {
        deviceId = Settings.System.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID)
    }

    fun getDeviceId(): String {
        LOG.d(TAG,"device id = $deviceId")
        return if (TextUtils.isEmpty(deviceId)) {
            ""
        } else {
            ApiConfig.md5(deviceId)
        }
    }

    fun getVersion(): String {
        var version = ""
        try {
            val context = AndroidUtils.getApplicationContext()
            val packageManager = context.packageManager
            val packageInfo = packageManager.getPackageInfo(context.packageName, 0)
            version = packageInfo.versionName
        } catch (e: Exception) {
            LOG.e(TAG,"Exception ", e)
        }
        LOG.d(TAG,"version $version")
        return version
    }

    fun getSystemLanguage(): String {
        return Locale.getDefault().language
    }

    fun getMobileModel(): String {
        return Build.BRAND
    }

    fun marshmallowDevices(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    }
}