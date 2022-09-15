package ai.txai.common.api

import ai.txai.common.data.*
import ai.txai.common.log.LOG
import ai.txai.common.response.CommonResponse
import ai.txai.common.utils.DeviceUtils
import io.reactivex.Observable
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File

object CommonApiRepository: BaseApiRepository<CommonApiServer>() {
    private const val TAG = "BaseApiRepository"
    fun checkAppUpdate(): Observable<CommonResponse<UpdateVersionInfo>> {
        val json = JSONObject()
        // 1: Android 2: IOS
        json.put("terminal_type", 1)
        json.put("version", DeviceUtils.getVersion())
        val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
        return apiServer.checkAppUpdate(body)
    }

    fun loadArticlesByType(typeName: String, isLoadContent: Boolean):
            Observable<CommonResponse<ArrayList<ArticlesInfo>>> {
        return apiServer.loadArticlesByType(typeName, isLoadContent)
    }

    fun loadArticlesDetail(typeName: String): Observable<CommonResponse<ArticlesDetailInfo>> {
        return apiServer.loadArticlesDetail(typeName)
    }

    fun uploadImageFile(channel: String, type: String, fileList: ArrayList<File>):
            Observable<CommonResponse<ArrayList<ImageInfo>>>? {
        LOG.d(TAG,"upload file")
        if (fileList.size <= 0) return null

        val boundary = "--WebKitFormBoundary7MA4YWxkTrZu0gW"
        val builder = MultipartBody.Builder(boundary)
            .setType(MultipartBody.FORM)
            .addFormDataPart("channel", channel)
            .addFormDataPart("classifier", type)
        fileList.forEach { file ->
            val body = file.asRequestBody("application/json".toMediaType())
            builder.addFormDataPart("files", file.name, body)
        }
        val requestBody = builder.build()
        val headerMap = ApiConfig.getHeaderMap(HashMap<String, Any>().apply {
            put("channel", channel)
            put("classifier", type)
        }, "POST", "/api/biz/media/picture/upload")
        return apiServer.uploadImageFiles(headerMap, requestBody)
    }

    fun uploadFile(channel: String, type: String, file: File):
            Observable<CommonResponse<FileInfo>> {
        LOG.d(TAG,"upload file")
        val boundary = "--WebKitFormBoundary7MA4YWxkTrZu0gW"
        val builder = MultipartBody.Builder(boundary)
            .setType(MultipartBody.FORM)
            .addFormDataPart("channel", channel)
            .addFormDataPart("classifier", type)
            val body = file.asRequestBody("application/json".toMediaType())
            builder.addFormDataPart("file", file.name, body)
        val requestBody = builder.build()
        val headerMap = ApiConfig.getHeaderMap(HashMap<String, Any>().apply {
            put("channel", channel)
            put("classifier", type)
        }, "POST", "/api/biz/media/file/upload")
        return apiServer.uploadFiles(headerMap, requestBody)
    }
}