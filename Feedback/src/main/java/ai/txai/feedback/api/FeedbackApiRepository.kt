package ai.txai.feedback.api

import ai.txai.common.api.BaseApiRepository
import ai.txai.common.response.CommonResponse
import ai.txai.common.utils.DeviceUtils
import ai.txai.feedback.data.IssueInfo
import com.google.gson.Gson
import io.reactivex.Observable
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

object FeedbackApiRepository: BaseApiRepository<FeedbackApiServer>() {
    private const val TAG = "FeedbackApiRepository"

    fun loadIssueType(): Observable<CommonResponse<ArrayList<IssueInfo>>> {
        return apiServer.loadArticlesByType("issue-type")
    }

    fun uploadFeedback(contactStyle: String, describe: String,
                       imageList: ArrayList<String>): Observable<CommonResponse<Long>>{
        val map = HashMap<String, Any>()
        if (contactStyle.isNotEmpty()) {
            map["contact_info"] = contactStyle
        }
        map["content"] = describe.trim()
        if (imageList.size > 0) {
            map["pictures"] = imageList
        }
        val gson = Gson().toJson(map)
        val body = gson.toString().toRequestBody("application/json".toMediaTypeOrNull())
        return apiServer.uploadFeedback(body)
    }

    fun uploadIssue(contactStyle: String, describe: String, issueType: Long, path: String,
                       imageList: ArrayList<String>): Observable<CommonResponse<Long>>{
        val map = HashMap<String, Any>()
        if (contactStyle.isNotEmpty()) {
            map["contact_info"] = contactStyle
        }
        map["content"] = describe.trim()
        map["issue_type"] = issueType
        if (path.isNotEmpty()) {
            map["log_path"] = path
        }
        if (imageList.size > 0) {
            map["pictures"] = imageList
        }
        val gson = Gson().toJson(map)

        val body = gson.toString().toRequestBody("application/json".toMediaTypeOrNull())
        return apiServer.uploadIssue(body)
    }
}