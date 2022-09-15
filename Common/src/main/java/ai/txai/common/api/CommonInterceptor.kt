package ai.txai.common.api

import ai.txai.common.api.ApiConfig.getHeaderMap
import ai.txai.common.json.GsonManager
import ai.txai.common.log.LOG
import ai.txai.common.utils.AndroidUtils
import ai.txai.common.utils.DeviceUtils
import ai.txai.common.network.NetUtils
import ai.txai.database.utils.CommonData
import com.blankj.utilcode.util.NetworkUtils
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okio.Buffer
import java.io.IOException
import java.util.*
import kotlin.collections.HashMap

class CommonInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val oldRequest: Request = chain.request()
        return try {
            val commonParameter: Map<String, Any> = getCommonParams()
            val builder: Request.Builder = oldRequest.newBuilder()
            var parameterMaps: MutableMap<String, Any> = HashMap()
            val method = oldRequest.method
            val body = oldRequest.body
            if (filterUpload(oldRequest)) {
                //上传图片参数不需要转成json，此处直接执行
                return chain.proceed(oldRequest)
            }

            val urlString = oldRequest.url.toString()
            val startIndex = ApiConfig.baseUrl.length
            val uri = urlString.substring(startIndex, urlString.length)
            injectParamsIntoUrl(oldRequest.url.newBuilder(), builder, commonParameter)
            if ("GET" == method) {
                val url = oldRequest.url
                val paraSize = url.querySize
                for (i in 0 until paraSize) {
                    val name = url.queryParameterName(i)
                    val value = url.queryParameterValue(i)
                    parameterMaps[name] = value!!
                }
            } else if ("POST" == method) {
                val s = bodyToString(body)
                parameterMaps = GsonManager.GsonToMaps(s)
                if (parameterMaps == null) {
                    parameterMaps = HashMap()
                }
                val requestBody: RequestBody? = GsonManager.GsonString(parameterMaps)
                    ?.toRequestBody("application/json".toMediaTypeOrNull())
                if (requestBody != null) {
                    builder.post(requestBody)
                }
            }
            parameterMaps.putAll(commonParameter)

            LOG.i(TAG, "parameterMaps %s", GsonManager.GsonString(parameterMaps))
            val headerMap: HashMap<String, String> = getHeaderMap(parameterMaps, method, uri)
            for ((key, value) in headerMap) {
                builder.addHeader(key, value)
            }
            val newRequest = builder.build()
            chain.proceed(newRequest)
        } catch (e: IOException) {
            chain.proceed(oldRequest)
        }
    }

    private fun filterUpload(request: Request) = ApiConfig.filterUpload(request)

    private fun injectParamsIntoUrl(
        httpUrlBuilder: HttpUrl.Builder,
        requestBuilder: Request.Builder,
        paramsMap: Map<String, Any>
    ): Request.Builder {
        if (paramsMap.isNotEmpty()) {
            val iterator: Iterator<*> = paramsMap.entries.iterator()
            while (iterator.hasNext()) {
                val (key, value) = iterator.next() as Map.Entry<*, *>
                httpUrlBuilder.addQueryParameter(key as String, value as String)
            }
            requestBuilder.url(httpUrlBuilder.build())
        }
        return requestBuilder
    }

    private fun getCommonParams(): HashMap<String, String> {
        NetworkUtils.getNetworkType()
        val map = HashMap<String, String>()
        map["app_version"] = DeviceUtils.getVersion()
        map["os_version"] = "Android"
        map["device_type"] = DeviceUtils.getMobileModel()
        map["device_locale"] = DeviceUtils.getSystemLanguage()
        map["network_type"] = NetUtils.getNetworkType()
        map["country_code"] = CommonData.getInstance().currentUser()?.isoCode ?:
        AndroidUtils.curCountryIsoCode
        return map
    }

    companion object {
        const val TAG = "CommonInterceptor"
        fun bodyToString(request: RequestBody?): String {
            return try {
                val buffer = Buffer()
                if (request != null) request.writeTo(buffer) else return ""
                buffer.readUtf8()
            } catch (e: IOException) {
                ""
            }
        }
    }
}