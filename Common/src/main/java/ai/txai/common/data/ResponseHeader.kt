package ai.txai.common.data

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class ResponseHeader {
    @Expose
    @SerializedName("msg")
    private var msg = ""

    @Expose
    @SerializedName("status")
    private var status = 0

    @Expose
    @SerializedName("version")
    private var version = ""

    fun getMsg() = msg

    fun getStatus() = status

    fun getVersion() = version

    override fun toString(): String {
        return "msg $msg, status $status, version $version"
    }
}