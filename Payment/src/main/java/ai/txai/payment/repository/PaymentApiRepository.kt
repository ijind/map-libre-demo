package ai.txai.payment.repository

import ai.txai.common.api.BaseApiRepository
import ai.txai.common.json.GsonManager
import ai.txai.common.response.CommonResponse
import ai.txai.database.utils.CommonData
import ai.txai.payment.request.PayRequestBean
import ai.txai.payment.response.PayMethodEntry
import ai.txai.payment.response.PayMethodList
import ai.txai.payment.response.PayOrder
import com.google.gson.JsonObject
import io.reactivex.Observable
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Time: 10/06/2022
 * Author Hay
 */
object PaymentApiRepository : BaseApiRepository<PaymentApiServer>() {

    fun defaultPayMethod(): Observable<CommonResponse<PayMethodEntry>> {
        val json = JsonObject()
        json.addProperty("user_id", CommonData.getInstance().currentUser().uid)
        val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
        return apiServer.defaultPayMethod(body)
    }

    fun payMethodList(): Observable<CommonResponse<PayMethodList>> {
        val json = JsonObject()
        json.addProperty("user_id", CommonData.getInstance().currentUser().uid)
        val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
        return apiServer.payMethodList(body)
    }

    fun removePayMethod(token: String): Observable<CommonResponse<String>> {
        val json = JsonObject()
        json.addProperty("user_id", CommonData.getInstance().currentUser().uid)
        json.addProperty("card_token", token)
        val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
        return apiServer.removePayMethod(body)

    }

    fun startPay(payBean: PayRequestBean): Observable<CommonResponse<PayOrder>> {
        return apiServer.startPay(
            GsonManager.GsonString(payBean).toRequestBody("application/json".toMediaTypeOrNull())
        )
    }

    fun queryPayStatus(orderId: String): Observable<CommonResponse<PayOrder>> {
        val json = JsonObject()
        json.addProperty("user_id", CommonData.getInstance().currentUser().uid)
        json.addProperty("order_id", orderId)
        val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
        return apiServer.queryPayStatus(body)
    }
}