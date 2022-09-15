package ai.txai.commonbiz.repository;

import java.util.List;

import ai.txai.common.api.BaseApiServer;
import ai.txai.common.response.CommonResponse;
import ai.txai.commonbiz.bean.AreaResponse;
import ai.txai.commonbiz.bean.CancelOrderResponse;
import ai.txai.commonbiz.bean.OrderDetailResponse;
import ai.txai.commonbiz.bean.OrderIntroResponse;
import ai.txai.commonbiz.bean.OrderResponse;
import ai.txai.commonbiz.bean.POIResponse;
import ai.txai.commonbiz.bean.PayResponse;
import ai.txai.commonbiz.bean.TripPlanResponse;
import ai.txai.commonbiz.bean.VehicleDetailResponse;
import ai.txai.commonbiz.bean.VehicleModelResponse;
import ai.txai.commonbiz.bean.VehicleResponse;
import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Time: 08/03/2022
 * Author Hay
 */
public interface BizApiServer extends BaseApiServer {

    @GET("/api/biz/poi/default")
    Observable<CommonResponse<POIResponse>> poiDefault();

    @GET("/api/biz/poi/list")
    Observable<CommonResponse<List<POIResponse>>> poiList();

    @POST("/api/biz/push/list")
    Observable<CommonResponse<List<String>>> pushList();

    @POST("/api/fc/trip/plan")
    Observable<CommonResponse<TripPlanResponse>> tripPlan(@Body RequestBody body);

    @POST("/api/fc/available_vehicle/pull")
    Observable<CommonResponse<List<VehicleResponse>>> availableVehicle(@Body RequestBody body);

    @POST("/api/order/cancel")
    Observable<CommonResponse<CancelOrderResponse>> cancelOrder(@Body RequestBody body);

    @POST("/api/order/create")
    Observable<CommonResponse<OrderResponse>> createOrder(@Body RequestBody body);

    @POST("/api/order/pay")
    Observable<CommonResponse<PayResponse>> payOrder(@Body RequestBody body);

    @POST("/api/order/detail")
    Observable<CommonResponse<OrderDetailResponse>> detailOrder(@Body RequestBody body);

    @POST("/api/order/list")
    Observable<CommonResponse<OrderIntroResponse>> listOrder(@Body RequestBody body);

    @GET("/api/biz/vmodel/list")
    Observable<CommonResponse<List<VehicleModelResponse>>> vehicleModels();

    @GET("/api/biz/vehicle/{id}")
    Observable<CommonResponse<VehicleDetailResponse>> vehicleDetails(@Path("id") String id);

    @GET("/api/biz/area/list")
    Observable<CommonResponse<List<AreaResponse>>> areaList();
}
