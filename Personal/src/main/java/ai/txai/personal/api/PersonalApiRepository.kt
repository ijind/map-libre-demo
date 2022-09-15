package ai.txai.personal.api

import ai.txai.common.api.BaseApiRepository
import ai.txai.common.log.LOG
import ai.txai.common.response.CommonResponse
import io.reactivex.Observable
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

object PersonalApiRepository : BaseApiRepository<PersonalApiServer>() {
    private const val TAG = "PersonalApiRepository"

    fun updatePersonalInfo(avatar: String, nickname: String, email:String)
    : Observable<CommonResponse<Boolean>> {
        LOG.d(TAG,"update personal info")
        val json = JSONObject()
        if (avatar.isNotEmpty()) {
            json.put("avatar", avatar)
        }
        if (nickname.isNotEmpty()) {
            json.put("nickname", nickname)
        }
        json.put("email", email)
        val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
        return apiServer.updatePersonalInfo(body)
    }
}