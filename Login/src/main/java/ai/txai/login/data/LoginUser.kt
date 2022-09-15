package ai.txai.login.data

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class LoginUser {

    @Expose
    @SerializedName("avatar")
    private var avatar = ""

    @Expose
    @SerializedName("user_id")
    private var userId: Long = 0L

    @Expose
    @SerializedName("nickname")
    private var nickName = ""

    @Expose
    @SerializedName("phone")
    private var phone = ""

    @Expose
    @SerializedName("type")
    private var type = 0

    @Expose
    @SerializedName("email")
    private var email = ""

    fun getAvatar() = avatar

    fun getId() = userId

    fun getNickname() = nickName

    fun getPhone() = phone

    fun getType() = type

    fun getEmail() = email
}