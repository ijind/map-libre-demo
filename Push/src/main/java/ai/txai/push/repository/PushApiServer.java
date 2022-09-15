package ai.txai.push.repository;

import java.util.List;

import ai.txai.common.api.BaseApiServer;
import ai.txai.common.response.CommonResponse;
import io.reactivex.Observable;
import retrofit2.http.POST;

/**
 * Time: 08/03/2022
 * Author Hay
 */
public interface PushApiServer extends BaseApiServer {
    @POST("/api/biz/push/list")
    Observable<CommonResponse<List<String>>> pushList();
}
