package ai.txai.common.api

import ai.txai.common.api.extra.LogInterceptor
import ai.txai.common.utils.RetrofitUtils
import android.util.Log
import com.google.gson.GsonBuilder
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object ApiServerManager {
    private val mApiMap = mutableMapOf<Class<*>, Any>()
    private var retrofit: Retrofit? = null

    @Suppress("UNCHECKED_CAST")
    @Synchronized
    fun <T> getApiServer(service: Class<T>, url: String): T {
        val value = mApiMap[service]
        if (value != null) {
            return value as T
        }

        val gson = GsonBuilder()
            .setLenient()
            .create()
//        intercept.level = HttpLoggingInterceptor.Level.BODY
        if (retrofit == null) {
            retrofit = RetrofitUtils.createRetrofit(
                RxJava2CallAdapterFactory.create(),
                GsonConverterFactory.create(gson),
                url,
                intercept
            )
        }
        val result = RetrofitUtils.createService(retrofit!!, service)
        mApiMap[service] = result!!
        return result
    }

    private val intercept = LogInterceptor()

    fun clearServerCache() {
        mApiMap.clear()
        retrofit = null
    }

}