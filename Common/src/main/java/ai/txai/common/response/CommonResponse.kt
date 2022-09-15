package ai.txai.common.response

import ai.txai.common.data.ResponseHeader
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.lang.StringBuilder

class CommonResponse<T> {

    @Expose
    @SerializedName("response")
    private var response: T? = null

    @Expose
    @SerializedName("response_header")
    private var responseHeader: ResponseHeader? = null

    fun getResult(): T? = response

    fun getResultHeader(): ResponseHeader? = responseHeader
}