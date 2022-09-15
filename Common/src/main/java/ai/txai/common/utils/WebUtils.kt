package ai.txai.common.utils

import ai.txai.common.base.ParameterField
import ai.txai.database.utils.CommonData
import android.os.Build
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView

/**
 * Time: 28/04/2022
 * Author Hay
 */
object WebUtils {
    const val USER_LEGAL_NAME = "user-service-agreement"
    const val PRIVACY_LEGAL_NAME = "privacy-policy"

    fun configWebView(webView: WebView) {

        val webSettings = webView.settings
        val agent = webSettings.userAgentString
        val version = DeviceUtils.getVersion()
        webSettings.userAgentString = "$agent App/Driver AppVersion/$version"
        webSettings.javaScriptEnabled = true
        webSettings.cacheMode = WebSettings.LOAD_DEFAULT

        webSettings.setSupportZoom(true)
        webSettings.builtInZoomControls = true
        webSettings.displayZoomControls = false

        webSettings.blockNetworkImage = false
        webSettings.loadsImagesAutomatically = true

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            webSettings.safeBrowsingEnabled = true
        }

        webSettings.javaScriptCanOpenWindowsAutomatically = true
        webSettings.domStorageEnabled = true
        webSettings.setSupportMultipleWindows(true)

        webSettings.useWideViewPort = true
        webSettings.loadWithOverviewMode = true
        webSettings.allowFileAccess = true
        webSettings.layoutAlgorithm = WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING
        webSettings.setGeolocationEnabled(true)

        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
    }

    fun cacheAgreementUrl(name: String, url: String) {
        when(name) {
            PRIVACY_LEGAL_NAME -> CommonData.getInstance().put(ParameterField.PRIVACY_URL, url)
            USER_LEGAL_NAME -> CommonData.getInstance().put(ParameterField.USER_AGREEMENT, url)
        }
    }
}