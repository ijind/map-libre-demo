package ai.txai.lostfound.repository;

import java.util.List;

import ai.txai.common.api.BaseApiServer;
import ai.txai.common.response.CommonResponse;
import ai.txai.lostfound.bean.ItemTypeEntry;
import ai.txai.lostfound.bean.LostDetailEntry;
import ai.txai.lostfound.bean.LostListEntry;
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
public interface LostFoundApiServer extends BaseApiServer {

    @GET("/api/biz/lost/item_types")
    Observable<CommonResponse<List<ItemTypeEntry>>> itemTypes();

    @POST("/api/biz/lost/add")
    Observable<CommonResponse<Integer>> lostAdd(@Body RequestBody body);

    @GET("/api/biz/lost/detail/{id}")
    Observable<CommonResponse<LostDetailEntry>> lostDetail(@Path("id") String id);

    @POST("/api/biz/lost/list")
    Observable<CommonResponse<LostListEntry>> lostList(@Body RequestBody body);
}
