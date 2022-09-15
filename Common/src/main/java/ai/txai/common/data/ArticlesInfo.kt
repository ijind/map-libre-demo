package ai.txai.common.data

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class ArticlesInfo {

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
    @SerializedName("ename")
    private var articleName = ""

    @Expose
    @SerializedName("link")
    private var articleLink = ""

    @Expose
    @SerializedName("title")
    private var articleTitle = ""

    fun getArticleName() = articleName

    fun getArticleLink() = articleLink

    fun getArticleTitle() = articleTitle

    fun getContent() = content

    override
    fun toString(): String {
        return "name $articleName, title $articleTitle, content $content"
    }
}