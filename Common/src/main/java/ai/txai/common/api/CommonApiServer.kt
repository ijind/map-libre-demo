package ai.txai.common.api

import ai.txai.common.data.*
import ai.txai.common.response.CommonResponse
import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.http.*

interface CommonApiServer : BaseApiServer {

    @POST("/api/biz/app/check_update")
    fun checkAppUpdate(@Body body: RequestBody): Observable<CommonResponse<UpdateVersionInfo>>

    @GET("/api/biz/article/list/{ename}")
    fun loadArticlesByType(
        @Path("ename") name: String,
        @Query("get_detail") isLoadDetails: Boolean
    ):
            Observable<CommonResponse<ArrayList<ArticlesInfo>>>

    @GET("/api/biz/article/detail/{ename}")
    fun loadArticlesDetail(@Path("ename") name: String):
            Observable<CommonResponse<ArticlesDetailInfo>>

    @POST("/api/biz/media/picture/upload")
    fun uploadImageFiles(@HeaderMap headerMap: HashMap<String, String>,
                    @Body body: RequestBody): Observable<CommonResponse<ArrayList<ImageInfo>>>

    @POST("/api/biz/media/file/upload")
    fun uploadFiles(@HeaderMap headerMap: HashMap<String, String>,
                    @Body body: RequestBody): Observable<CommonResponse<FileInfo>>
}