package ai.txai.login.data

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class LoginToken {

    @Expose
    @SerializedName("token")
    private var apiToken = ""

    @Expose
    @SerializedName("created_at")
    private var createdTime = ""

    @Expose
    @SerializedName("expired_at")
    private var expiredTime = ""

    @Expose
    @SerializedName("key")
    private var pushToken = ""

    @Expose
    @SerializedName("update_token")
    private var updateToken = ""

    fun getApiToken() = apiToken

    fun getCreatedTime() = createdTime

    fun getExpiredTime() = expiredTime

    fun getPushToken() = pushToken

    fun getUpdateToken() = updateToken
}