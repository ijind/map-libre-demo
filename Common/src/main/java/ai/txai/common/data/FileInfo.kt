package ai.txai.common.data

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class FileInfo {

    @Expose
    @SerializedName("path")
    private var filePath = ""

    @Expose
    @SerializedName("size")
    private var fileSize = 0L

    private var fileTag = 0

    fun setFilePath(path: String) {
        filePath = path
    }

    fun getFilePath() = filePath

    fun setFileTag(tag: Int) {
        fileTag = tag
    }

    fun getFileTag() = fileTag


}