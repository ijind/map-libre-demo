package ai.txai.common.data

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class UpdateVersionInfo {

    @Expose
    @SerializedName("app_type")
    private var appType = ""

    @Expose
    @SerializedName("app_version_id")
    private var appVersionId = ""

    @Expose
    @SerializedName("app_version_ranges")
    private var appVersionRanges: ArrayList<VersionRange>? = null

    @Expose
    @SerializedName("download_url")
    private var downLoadUrl = ""

    @Expose
    @SerializedName("terminal_type")
    private var terminalType = 1 // 1: Android 2: IOS

    @Expose
    @SerializedName("update_brief")
    private var updateBrief = ""

    @Expose
    @SerializedName("update_type")
    private var updateType = 1 // 1: 提示更新， 2: 强制更新

    @Expose
    @SerializedName("version")
    private var newVersion = ""

    fun getNewVersion() = newVersion

    fun getDownLoadUrl() = downLoadUrl

    fun getUpdateBrief() = updateBrief
}