package ai.txai.lostfound.repository

import ai.txai.common.api.BaseApiRepository
import ai.txai.common.response.CommonResponse
import ai.txai.lostfound.bean.ItemTypeEntry
import ai.txai.lostfound.bean.LostDetailEntry
import ai.txai.lostfound.bean.LostListEntry
import com.google.gson.JsonObject
import io.reactivex.Observable
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Time: 14/03/2022
 * Author Hay
 */
object LostFoundApiRepository :BaseApiRepository<LostFoundApiServer>() {
    fun itemTypes(): Observable<CommonResponse<List<ItemTypeEntry>>> {
        return apiServer.itemTypes()
    }

    fun lostAdd(
        callingCode: Int,
        email: String,
        itemType: ItemTypeEntry,
        orderId: String,
        phone: String,
        summary: String?
    ): Observable<CommonResponse<Int>> {
        val json = JsonObject()
        json.addProperty("calling_code", callingCode)
        json.addProperty("email", email)
        json.addProperty("item_type", itemType.value)
        json.addProperty("order_id", orderId)
        json.addProperty("phone", phone)
        json.addProperty("summary", summary ?: "")
        val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
        return apiServer.lostAdd(body)
    }

    fun lostDetail(id: String): Observable<CommonResponse<LostDetailEntry>> {
        return apiServer.lostDetail(id)
    }

    fun lostList(
        orderId: String,
        page: Int,
        size: Int,
    ): Observable<CommonResponse<LostListEntry>> {
        val json = JsonObject()
        json.addProperty("order_id", orderId)
        json.addProperty("page", page)
        json.addProperty("size", size)
        val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
        return apiServer.lostList(body)
    }
}