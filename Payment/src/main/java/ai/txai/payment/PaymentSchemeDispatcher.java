package ai.txai.payment;

import android.app.Activity;

import ai.txai.common.api.ApiConfig;
import ai.txai.common.log.LOG;
import ai.txai.common.scheme.ISchemeDispatcher;
import ai.txai.payment.provider.PaymentProviderImpl;

/**
 * Time: 14/06/2022
 * Author Hay
 */
public class PaymentSchemeDispatcher extends ISchemeDispatcher {
    @Override
    public String uriString() {
        return "txai://payment.ai";
    }

    @Override
    public boolean dispatch(Activity activity) {
        LOG.i("Payment", "scheme url %s", mUri.toString());
        String orderId = mUri.getQueryParameter("orderId");
        PaymentProviderImpl.Companion.getMapPaying().put(orderId, true);
        return true;
    }
}
