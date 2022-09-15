package ai.txai.common.data

class ToastInfo {

    private var isShowDrawable = false

    private var content = ""

    fun setIsShowDrawable(isShowDrawable: Boolean) {
        this.isShowDrawable = isShowDrawable
    }

    fun setContent(content: String) {
        this.content = content
    }

    fun getContent() = content

    fun isShowDrawable() = isShowDrawable
}