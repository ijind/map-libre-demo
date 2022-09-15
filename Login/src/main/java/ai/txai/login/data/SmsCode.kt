package ai.txai.login.data

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.lang.StringBuilder

class SmsCode {

    @Expose
    @SerializedName("created_at")
    private var createdTime = ""

    @Expose
    @SerializedName("expired_at")
    private var expiredTime = ""

    @Expose
    @SerializedName("resend_at")
    private var resendTime = ""

    @Expose
    @SerializedName("vcode")
    private var smsCode = ""

    private var failedMessage = ""

    fun getSmsCode() = smsCode

    fun getCreateTime() = createdTime

    fun getExpiredTime() = expiredTime

    fun getResendTime() = resendTime

    fun getFailedMessage() = failedMessage

    fun setFailedMessage(message: String) {
        failedMessage = message
    }
}