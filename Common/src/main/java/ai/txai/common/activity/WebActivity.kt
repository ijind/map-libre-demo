package ai.txai.common.activity

import ai.txai.common.base.BaseScrollActivity
import ai.txai.common.base.ParameterField
import ai.txai.common.databinding.CommonWebLayoutBinding
import ai.txai.common.mvvm.BaseViewModel
import ai.txai.common.utils.AndroidUtils
import ai.txai.common.utils.WebUtils
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient


class WebActivity : BaseScrollActivity<CommonWebLayoutBinding, BaseViewModel>() {

    override fun initViewObservable() {
        super.initViewObservable()
        val url = intent.extras?.getString(ParameterField.WEB_URL) ?: ""
        if (url.isEmpty()) return
        WebUtils.configWebView(itemBinding.webLayout)
        itemBinding.webLayout.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                return false
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                AndroidUtils.delayOperation(300) {
                    //promptPopupView.dismissPopup()
                }
            }
        }
        itemBinding.webLayout.loadUrl(url)
    }

    override fun onDestroy() {
        super.onDestroy()
        itemBinding.webLayout.clearCache(true)
    }

    override fun getCustomTitle(): Int {
        return intent.extras?.getInt(ParameterField.WEB_TITLE) ?: 0
    }

    override fun initItemBinding(parent: ViewGroup?): CommonWebLayoutBinding {
        return CommonWebLayoutBinding.inflate(layoutInflater, parent, false)
    }
}