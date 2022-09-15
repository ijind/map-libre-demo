package ai.txai.setting.api

import ai.txai.common.api.BaseApiServer
import ai.txai.common.response.CommonResponse
import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.http.*

interface SettingApiServer : BaseApiServer {

    @POST("/api/biz/user/logout")
    fun logout(@Body body: RequestBody): Observable<CommonResponse<Boolean>>
}