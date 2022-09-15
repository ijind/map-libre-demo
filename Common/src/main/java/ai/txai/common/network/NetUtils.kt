package ai.txai.common.network

import ai.txai.common.utils.AndroidUtils
import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.blankj.utilcode.util.NetworkUtils

object NetUtils {

    private val networkTypeList = mapOf(
        NetworkUtils.NetworkType.NETWORK_2G to "2G",
        NetworkUtils.NetworkType.NETWORK_3G to "3G",
        NetworkUtils.NetworkType.NETWORK_4G to "4G",
        NetworkUtils.NetworkType.NETWORK_5G to "5G",
        NetworkUtils.NetworkType.NETWORK_WIFI to "wifi"
    )

    var isShowToastWhenNoInternet = true
    fun getNetworkType(): String {
        val type: NetworkUtils.NetworkType = NetworkUtils.getNetworkType()
        return networkTypeList[type] ?: "unKnow"
    }

    fun isInternetEnable(): Boolean {
        var result = false
        val connectManager = AndroidUtils.getApplicationContext()
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            connectManager?.run {
                this.getNetworkCapabilities(this.activeNetwork)?.run {
                    result = when {
                        hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                        hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                        hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                        else -> false
                    }
                }
            }
        } else {
            connectManager?.run {
                this.activeNetworkInfo?.run {
                    if (type == ConnectivityManager.TYPE_WIFI) {
                        result = true
                    } else if (type == ConnectivityManager.TYPE_MOBILE) {
                        result = true
                    }
                }
            }
        }
        return result
    }
}