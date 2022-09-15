package ai.txai.feedback.data

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class IssueInfo {

    @Expose
    @SerializedName("name")
    var name = ""

    @Expose
    @SerializedName("value")
    var value = 0L

    fun getTitle() = name
}