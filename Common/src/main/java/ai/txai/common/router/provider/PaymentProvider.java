package ai.txai.common.router.provider;

import android.app.Activity;

import com.alibaba.android.arouter.facade.template.IProvider;

import ai.txai.common.router.bean.PayMethod;
import ai.txai.common.router.bean.PayStatus;

/**
 * Time: 10/06/2022
 * Author Hay
 */
public interface PaymentProvider extends IProvider {
    void preloadPaymentInfo();

    void defaultPaymentMethod(String orderId, DefaultModelListener listener);

    void payNow(Activity activity, double fare, String orderId);

    void selectCard(Activity activity, double actualPay, String id);

    Boolean isPaying(String orderId);

    void clearPaying(String orderId);

    void paymentStatus(String orderId, PayStatusListener listener);

    interface DefaultModelListener {
        void result(PayMethod method);
    }

    interface PayStatusListener {
        void result(PayStatus method);
    }
}
