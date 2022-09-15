package ai.txai.common.widget.popupview

import ai.txai.common.R
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.View.OnClickListener
import android.widget.FrameLayout
import android.widget.TextView


class CommonLoadingStatusView: FrameLayout {

    //加载时显示文字
    private lateinit var loadingTextTv: TextView

    //加载错误视图
    private lateinit var loadErrorLl: TextView

    //加载错误点击事件处理
    private var loadingHandler: LoadingHandler? = null

    //加载view
    private lateinit var loadingView: View

    //加载失败view
    private lateinit var loadingErrorView: View

    //数据为空
    private lateinit var emptyView: View

    constructor(context: Context, attrs: AttributeSet?) :
            super(context, attrs, 0) {
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr) {

    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        loadingView = inflate(context, R.layout.commonview_loading_layout, null)
        loadingErrorView = inflate(context, R.layout.commonview_network_wrong_layout, null)
        emptyView = inflate(context, R.layout.commonview_no_trip_layout, null)
        this.addView(loadingView)
        this.addView(loadingErrorView)
        this.addView(emptyView)
        loadingErrorView.visibility = GONE
        emptyView.visibility = GONE
        setBackgroundResource(R.color.white)
        this.isClickable = true
        initView(this)
    }

    private fun initView(rootView: View) {
        loadingTextTv = rootView.findViewById<View>(R.id.loading_tv) as TextView
        loadErrorLl = rootView.findViewById<View>(R.id.refresh_tv) as TextView
        loadErrorLl.setOnClickListener(OnClickListener {
            loadingHandler?.let {
                load()
                it.doRequestData()
            }
        })
    }

    fun setLoadingHandler(loadingHandler: LoadingHandler) {
        this.loadingHandler = loadingHandler
    }

    fun setLoadingErrorView(loadingErrorView: View?) {
        removeViewAt(1)
        this.loadingErrorView = loadingErrorView!!
        this.loadingErrorView.setOnClickListener {
            loadingHandler?.doRequestData()
            load()
        }
        this.addView(loadingErrorView, 1)
    }

    fun setLoadingView(loadingView: View?) {
        removeViewAt(0)
        this.loadingView = loadingView!!
        this.addView(loadingView, 0)
    }

    fun setMessage(message: String) {
        loadingTextTv.text = message
    }

    fun load() {
        loadingView.visibility = VISIBLE
        loadingErrorView.visibility = GONE
        emptyView.visibility = GONE
    }

    fun load(message: String?) {
        loadingTextTv.text = message
        loadingView.visibility = VISIBLE
        loadingErrorView.visibility = GONE
        emptyView.visibility = GONE
    }

    fun loadSuccess() {
        this.loadSuccess(false)
    }

    fun loadSuccess(isEmpty: Boolean) {
        loadingView.visibility = GONE
        loadingErrorView.visibility = GONE
        if (isEmpty) {
            emptyView.visibility = VISIBLE
        } else {
            emptyView.visibility = GONE
        }
    }


    fun loadError() {
        loadingView.visibility = GONE
        emptyView.visibility = GONE
        loadingErrorView.visibility = VISIBLE
    }

    interface LoadingHandler {
        fun doRequestData()
    }
}