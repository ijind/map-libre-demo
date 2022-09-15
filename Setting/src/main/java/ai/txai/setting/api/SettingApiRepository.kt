package ai.txai.setting.api

import ai.txai.common.api.BaseApiRepository
import ai.txai.common.response.CommonResponse
import io.reactivex.Observable
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

object SettingApiRepository :BaseApiRepository<SettingApiServer>() {
    fun logout(): Observable<CommonResponse<Boolean>> {
        val json = JSONObject()
        val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
        return apiServer.logout(body)
    }
}