package ai.txai.login.data

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class LoginInfo<T, U> {

    @Expose
    @SerializedName("new_user")
    private var newUser = false

    @Expose
    @SerializedName("token")
    private var token: T? = null

    @Expose
    @SerializedName("user")
    private var user: U? = null

    private var errorMessage: String = ""

    fun getToken() = token

    fun getUser() = user

    fun setErrorMessage(message: String) {
        this.errorMessage = message
    }

    fun getErrorMessage() = errorMessage
}