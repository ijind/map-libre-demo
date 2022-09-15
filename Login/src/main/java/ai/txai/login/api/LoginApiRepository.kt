package ai.txai.login.api

import ai.txai.common.api.BaseApiRepository
import ai.txai.common.countrycode.Country
import ai.txai.common.response.CommonResponse
import ai.txai.common.utils.DeviceUtils
import ai.txai.login.data.*
import io.reactivex.Observable
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

object LoginApiRepository : BaseApiRepository<LoginApiServer>() {
    private const val TAG = "BaseApiRepository"

    fun sendSms(mobile: String, type: String): Observable<CommonResponse<SmsCode>> {
        val json = JSONObject()
        json.put("client_id", DeviceUtils.getDeviceId())
        json.put("phone", mobile)
        json.put("type", type)
        val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
        return apiServer.sendSms(body)
    }

    fun userLogin(code: String, mobile: String):
            Observable<CommonResponse<LoginInfo<LoginToken, LoginUser>>> {
        val json = JSONObject()
        json.put("client_id", DeviceUtils.getDeviceId())
        json.put("phone", mobile)
        json.put("vcode", code)
        val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
        return apiServer.userLogin(body)
    }

    fun getCountryList(): Observable<CommonResponse<MutableList<Country>>> {
        return apiServer.loadCountryList(null)
    }
}