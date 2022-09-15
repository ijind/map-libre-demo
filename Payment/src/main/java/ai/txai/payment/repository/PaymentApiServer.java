package ai.txai.payment.repository;

import ai.txai.common.api.BaseApiServer;
import ai.txai.common.response.CommonResponse;
import ai.txai.payment.response.PayMethodEntry;
import ai.txai.payment.response.PayMethodList;
import ai.txai.payment.response.PayOrder;
import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Time: 06/06/2022
 * Author Hay
 */
public interface PaymentApiServer extends BaseApiServer {
    @POST("/api/payment/method/default")
    Observable<CommonResponse<PayMethodEntry>> defaultPayMethod(@Body RequestBody body);

    @POST("/api/payment/method/list")
    Observable<CommonResponse<PayMethodList>> payMethodList(@Body RequestBody body);

    @POST("/api/payment/savecard/remove")
    Observable<CommonResponse<String>> removePayMethod(@Body RequestBody body);

    @POST("/api/payment/order/supply")
    Observable<CommonResponse<PayOrder>> startPay(@Body RequestBody body);

    @POST("/api/payment/order/query")
    Observable<CommonResponse<PayOrder>> queryPayStatus(@Body RequestBody body);
}
