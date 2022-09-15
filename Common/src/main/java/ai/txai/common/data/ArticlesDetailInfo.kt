package ai.txai.common.data

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class ArticlesDetailInfo {

    @Expose
    @SerializedName("article_id")
    private var articleId = 0L

    @Expose
    @SerializedName("cate_id")
    private var cateId = 0L

    @Expose
    @SerializedName("cate_name")
    private var cateName = ""

    @Expose
    @SerializedName("content")
    private var content = ""

    @Expose
    @SerializedName("created_at")
    private var createdTime = 0L

    @Expose
    @SerializedName("created_by")
    private var createdUser = 0L

    @Expose
    @SerializedName("ename")
    private var articleName = ""

    @Expose
    @SerializedName("link")
    private var articleLink = ""

    @Expose
    @SerializedName("order_index")
    private var orderIndex = 0L

    @Expose
    @SerializedName("title")
    private var title = ""

    @Expose
    @SerializedName("updated_at")
    private var updatedTime = 0L

    @Expose
    @SerializedName("updated_by")
    private var updatedUser = 0L

    fun getTitle(): String = title

    fun getContent(): String = content
}