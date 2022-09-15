package ai.txai.common.utils

import ai.txai.common.api.CommonInterceptor
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit


object RetrofitUtils {
    private const val DEFAULT_READ = 100L
    private const val DEFAULT_CONNECT = 60L
    private const val DEFAULT_WRITE = 60L

    fun createRetrofit(
        adapter: CallAdapter.Factory,
        converter: Converter.Factory,
        baseUrl: String,
        interceptor: Interceptor?): Retrofit {
        return Retrofit.Builder()
            .client(initOkHttp(interceptor))
            .addCallAdapterFactory(adapter)
            .addConverterFactory(converter)
            .baseUrl(baseUrl)
            .build()
    }

    fun <T> createService(retrofit: Retrofit, clazz: Class<T>): T = retrofit.create(clazz)

    private fun initOkHttp(interceptor: Interceptor?): OkHttpClient {
        return OkHttpClient().newBuilder()
            .readTimeout(DEFAULT_READ, TimeUnit.SECONDS)
            .connectTimeout(DEFAULT_CONNECT, TimeUnit.SECONDS)
            .writeTimeout(DEFAULT_WRITE, TimeUnit.SECONDS)
            .addInterceptor(CommonInterceptor())
            .retryOnConnectionFailure(true)
            .apply { interceptor?.let { this.addNetworkInterceptor(it) } }
            .build()
    }
}