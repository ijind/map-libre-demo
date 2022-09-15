package ai.txai.push.repository

import ai.txai.common.api.BaseApiRepository
import ai.txai.common.response.CommonResponse
import io.reactivex.Observable

/**
 * Time: 14/03/2022
 * Author Hay
 */
object PushApiRepository : BaseApiRepository<PushApiServer>() {
    private const val TAG = "BaseApiRepository"

    fun pushList(): Observable<CommonResponse<List<String>>> {
        return apiServer.pushList()
    }

}