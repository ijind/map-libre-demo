package ai.txai.common.data

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class VersionRange {

    @Expose
    @SerializedName("from")
    private var startVersion = ""

    @Expose
    @SerializedName("to")
    private var endVersion = ""

    fun getStartVersion() = startVersion

    fun getEndVersion() = endVersion
}