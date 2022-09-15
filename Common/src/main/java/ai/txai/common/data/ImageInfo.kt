package ai.txai.common.data

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class ImageInfo {

    @Expose
    @SerializedName("height")
    private var height = 0L

    @Expose
    @SerializedName("path")
    private var path = ""

    @Expose
    @SerializedName("size")
    private var size = 0L

    @Expose
    @SerializedName("url")
    private var url = ""

    @Expose
    @SerializedName("width")
    private var width = 0L

    fun getPath() = path

    fun getUrl() = url
}