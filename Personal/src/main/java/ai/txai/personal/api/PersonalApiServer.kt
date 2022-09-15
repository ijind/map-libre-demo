package ai.txai.personal.api

import ai.txai.common.api.BaseApiServer
import ai.txai.common.response.CommonResponse
import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.http.*

interface PersonalApiServer : BaseApiServer {

    @POST("/api/biz/user/update")
    fun updatePersonalInfo(@Body body: RequestBody): Observable<CommonResponse<Boolean>>
}