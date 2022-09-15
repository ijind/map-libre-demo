package ai.txai.feedback.api

import ai.txai.common.api.BaseApiServer
import ai.txai.common.response.CommonResponse
import ai.txai.feedback.data.IssueInfo
import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.http.*

interface FeedbackApiServer: BaseApiServer {

    @GET("/api/biz/common/option/list/{ename}")
    fun loadArticlesByType(@Path("ename") name: String):
            Observable<CommonResponse<ArrayList<IssueInfo>>>

    @POST("/api/biz/feedback/add")
    fun uploadFeedback(@Body body: RequestBody): Observable<CommonResponse<Long>>

    @POST("/api/biz/feedback/add_issue")
    fun uploadIssue(@Body body: RequestBody): Observable<CommonResponse<Long>>

}