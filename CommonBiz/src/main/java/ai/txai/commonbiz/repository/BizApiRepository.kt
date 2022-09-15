package ai.txai.commonbiz.repository

import ai.txai.common.api.BaseApiRepository
import ai.txai.common.json.GsonManager
import ai.txai.common.log.LOG
import ai.txai.common.response.CommonResponse
import ai.txai.commonbiz.bean.*
import ai.txai.database.order.Order
import ai.txai.database.site.Site
import ai.txai.database.utils.CommonData
import com.google.gson.JsonObject
import io.reactivex.Observable
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Time: 14/03/2022
 * Author Hay
 */
object BizApiRepository : BaseApiRepository<BizApiServer>() {
    private const val TAG = "BaseApiRepository"

    fun poiList(): Observable<CommonResponse<List<POIResponse>>> {
        return apiServer.poiList()
    }

    fun poiDefault():Observable<CommonResponse<POIResponse>> {
        return apiServer.poiDefault();
    }

    fun vehicleModels(): Observable<CommonResponse<List<VehicleModelResponse>>> {
        return apiServer.vehicleModels()
    }

    fun vehicleDetails(id: String): Observable<CommonResponse<VehicleDetailResponse>> {
        return apiServer.vehicleDetails(id)
    }

    fun pushList(): Observable<CommonResponse<List<String>>> {
        return apiServer.pushList()
    }

    fun areaList(): Observable<CommonResponse<List<AreaResponse>>> {
        return apiServer.areaList()
    }



    fun tripPlan(
        pickupSite: Site,
        dropOff: Site,
        vModelId: String
    ): Observable<CommonResponse<TripPlanResponse>> {
        val json = JsonObject()
        json.addProperty("dropoff_poi_id", dropOff.id)
        json.addProperty("pickup_poi_id", pickupSite.id)
        json.addProperty("user_id", CommonData.getInstance().currentUser().uid)
        json.addProperty("vehicle_type_id", vModelId)
        val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
        return apiServer.tripPlan(body)
    }

    fun availableVehicle(): Observable<CommonResponse<List<VehicleResponse>>> {
        val json = JsonObject()
        var currentUser = CommonData.getInstance().currentUser()
        json.addProperty("user_id", currentUser.uid)
        val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
        return apiServer.availableVehicle(body)
    }

    fun createOrder(
        pickupSite: Site,
        dropOff: Site,
        vModelId: String
    ): Observable<CommonResponse<OrderResponse>> {
        val json = JsonObject()
        json.addProperty("dropoff_poi_id", dropOff.id)
        json.addProperty("pickup_poi_id", pickupSite.id)
        json.addProperty("user_id", CommonData.getInstance().currentUser().uid)
        json.addProperty("vehicle_type_id", vModelId)
        val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
        return apiServer.createOrder(body)
    }

    fun cancelOrder(id: String, memo: String): Observable<CommonResponse<CancelOrderResponse>> {
        val json = JsonObject()
        json.addProperty("order_id", id)
        json.addProperty("memo", memo)
        json.addProperty("user_id", CommonData.getInstance().currentUser().uid)
        val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
        return apiServer.cancelOrder(body)
    }

    fun pay(order: Order, memo: String): Observable<CommonResponse<PayResponse>> {
        val json = JsonObject()
        json.addProperty("order_id", order.id)
        json.addProperty("memo", memo)
        json.addProperty("user_id", CommonData.getInstance().currentUser().uid)
        val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
        return apiServer.payOrder(body)
    }

    fun detailOrder(id: String): Observable<CommonResponse<OrderDetailResponse>> {
        val json = JsonObject()
        json.addProperty("order_id", id)
        json.addProperty("user_id", CommonData.getInstance().currentUser().uid)
        val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
        return apiServer.detailOrder(body)
    }

    fun recentOrder(): Observable<CommonResponse<OrderDetailResponse>> {
        val json = JsonObject()
        json.addProperty("user_id", CommonData.getInstance().currentUser().uid)
        json.addProperty("order_status", "In_Progress")
        val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
        return apiServer.detailOrder(body)
    }

    fun listOrder(endTime: Long, count: Int): Observable<CommonResponse<OrderIntroResponse>> {
        val json = JsonObject()
        json.addProperty("end_time", endTime)
        json.addProperty("user_id", CommonData.getInstance().currentUser().uid)
        json.addProperty("query_cnt", count)
        LOG.i("listOrder", GsonManager.GsonString(json))
        val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
        return apiServer.listOrder(body)
    }

    fun listOrder(endTime: Long, count: Int, orderStatus:String): Observable<CommonResponse<OrderIntroResponse>> {
        val json = JsonObject()
        json.addProperty("end_time", endTime)
        json.addProperty("user_id", CommonData.getInstance().currentUser().uid)
        json.addProperty("query_cnt", count)
        json.addProperty("order_status", orderStatus)
        val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
        return apiServer.listOrder(body)
    }
}