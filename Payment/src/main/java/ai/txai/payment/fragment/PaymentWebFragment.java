package ai.txai.payment.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.alibaba.android.arouter.facade.annotation.Route;

import ai.txai.common.base.BaseScrollFragment;
import ai.txai.common.base.helper.ScrollBindingHelper;
import ai.txai.common.log.LOG;
import ai.txai.common.router.ARouterConstants;
import ai.txai.common.utils.WebUtils;
import ai.txai.payment.databinding.PaymentWebFragmentBinding;
import ai.txai.payment.provider.PaymentProviderImpl;
import ai.txai.payment.viewmodel.PaymentViewModel;

/**
 * Time: 06/06/2022
 * Author Hay
 */
@Route(path = ARouterConstants.PATH_FRAGMENT_WEB)
public class PaymentWebFragment extends BaseScrollFragment<PaymentWebFragmentBinding, PaymentViewModel> {
    private final String TAG = "JsBridgeDemoFragment";

    public static final String EXTRA_URL = "extra.url";
    public static final String EXTRA_CONTENT = "extra.content";

    @Override
    public void initViewObservable() {
        super.initViewObservable();

        itemBinding.webView.setWebChromeClient(new WebChromeClient() {
        });

        Bundle arguments = getArguments();
        if (arguments != null) {
            String data = arguments.getString(EXTRA_CONTENT);
            String url = arguments.getString(EXTRA_URL);
            if (!TextUtils.isEmpty(url)) {
                itemBinding.webView.loadUrl(url);
            } else if (!TextUtils.isEmpty(data)) {
                itemBinding.webView.loadData(data, "text/html; charset=UTF-8", null);
            }
        }

        WebUtils.INSTANCE.configWebView(itemBinding.webView);

        itemBinding.webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Uri uri = Uri.parse(url);
                LOG.i("WebView", "shouldOverrideUrlLoading-d %s", uri.getPath());
                if (uri.getPath().equals("/payment/callback")) {
                    String orderId = uri.getQueryParameter("orderId");
                    PaymentProviderImpl.Companion.getMapPaying().put(orderId, true);
                    viewModel.finish();
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                LOG.i("WebView", "shouldOverrideUrlLoading %s", uri.getPath());
                if (uri.getPath().equals("/payment/callback")) {
                    String orderId = uri.getQueryParameter("orderId");
                    PaymentProviderImpl.Companion.getMapPaying().put(orderId, true);
                    viewModel.finish();
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, request);
            }
        });
    }

    @Override
    protected int getCustomTitle() {
        return 0;
    }

    @Override
    protected PaymentWebFragmentBinding initItemBinding(ViewGroup parent) {
        return PaymentWebFragmentBinding.inflate(getLayoutInflater(), parent, false);
    }

}
