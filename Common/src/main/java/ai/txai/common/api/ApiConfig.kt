package ai.txai.common.api

import ai.txai.common.json.GsonManager
import ai.txai.common.log.LOG
import ai.txai.database.utils.CommonData
import android.text.TextUtils
import android.util.Log
import com.google.gson.Gson
import okhttp3.Request
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import kotlin.collections.ArrayList


object ApiConfig {
    private const val TAG = "ApiConfig"
    private const val KEY_BASE_URL = "BASE_URL"
    const val DEV_URL = "https://api-dev.kharita.ai"
    const val TEST_URL = "https://api-test.kharita.ai"

    var baseUrl = ""
        set(value) {
            if (TextUtils.isEmpty(value)) {
                field = ""
                return
            }
            field = value
            CommonData.getInstance().put(KEY_BASE_URL, value)
        }
        get() {
            if (TextUtils.isEmpty(field)) {
                val savedValue = CommonData.getInstance().get(KEY_BASE_URL)
                if (TextUtils.isEmpty(savedValue)) {
                    field = DEV_URL
                    CommonData.getInstance().put(KEY_BASE_URL, DEV_URL)
                    LOG.i(TAG, "baseUrl  2 %s", field)
                    return field
                }
                field = savedValue
                LOG.i(TAG, "baseUrl 1 %s", field)
                return field
            }
            LOG.i(TAG, "baseUrl 3 %s", field)
            return field
        }

    fun baseUrl(): String {
        return baseUrl
    }

    fun getHeaderMap(params: Map<String, Any>, method: String, uri: String)
            : HashMap<String, String> {
        val paramsMap = HashMap<String, Any>()
        paramsMap.putAll(params)
        val infoIds = ArrayList<Map.Entry<String, Any>>(paramsMap.entries)
        Collections.sort(infoIds,
            Comparator<Map.Entry<String, Any>> { o1, o2 ->
                return@Comparator o1.key.compareTo(o2.key)
            })
        val buffer = StringBuffer()
        val curTime = System.currentTimeMillis()
        buffer.append("$curTime:$method $uri?")
        infoIds.forEach {
            if (it.key != "") {
                if (it.value is List<*> || it.value is Map<*, *>) {
                    buffer.append(it.key).append("=").append(GsonManager.GsonString(it.value))
                        .append("&")
                } else {
                    buffer.append(it.key).append("=").append(it.value).append("&")
                }
            }
        }
        val headerMap = HashMap<String, String>()
        headerMap["logid"] = UUID.randomUUID().toString().replace("-", "")
        val curUser = CommonData.getInstance().currentUser()
        if (curUser != null && !curUser.pushToken.isNullOrEmpty()) {
            val appKey = curUser.pushToken.substring(0, 16)
            buffer.append("apiKey=$appKey")
            val md5String = md5(buffer.toString()).uppercase()
            Log.d(TAG, "md5 string $md5String")
            headerMap["API-SIGNATURE"] = md5String
            headerMap["API-TOKEN"] = curUser.apiToken
            headerMap["API-TIMESTAMP"] = curTime.toString()
        }

        return headerMap
    }

    fun md5(content: String): String {
        Log.d(TAG, "content $content")
        var result = ""
        val encrypt: ByteArray
        try {
            val tem: ByteArray = content.toByteArray()
            val md5 = MessageDigest.getInstance("md5")
            md5.reset()
            md5.update(tem)
            encrypt = md5.digest()
            val stringBuilder = StringBuilder()
            for (t in encrypt) {
                var s = Integer.toHexString(t.toInt() and 0xFF)
                if (s.length == 1) {
                    s = "0$s"
                }
                stringBuilder.append(s)
            }
            result = stringBuilder.toString()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        Log.d(TAG, "result $result")
        return result
    }

    fun filterUpload(request: Request) =
        (request.url.toString().contains("/api/biz/media/picture/upload")
                || request.url.toString().contains("/api/biz/media/file/upload"))
}