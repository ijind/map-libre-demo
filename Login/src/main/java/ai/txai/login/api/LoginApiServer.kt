package ai.txai.login.api

import ai.txai.common.api.BaseApiServer
import ai.txai.common.countrycode.Country
import ai.txai.common.response.CommonResponse
import ai.txai.login.data.*
import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface LoginApiServer : BaseApiServer {

    @POST("/api/biz/vcode/send")
    fun sendSms(@Body body: RequestBody):
            Observable<CommonResponse<SmsCode>>

    @POST("/api/biz/user/login")
    fun userLogin(@Body body: RequestBody):
            Observable<CommonResponse<LoginInfo<LoginToken, LoginUser>>>

    @GET("/api/biz/country/list")
    fun loadCountryList(@Query("purpose") purpose: Int?):
            Observable<CommonResponse<MutableList<Country>>>
}