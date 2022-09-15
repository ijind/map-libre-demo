package ai.txai.commonbiz.provider;

import android.content.Context;
import android.os.CountDownTimer;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import ai.txai.common.log.LOG;
import ai.txai.common.observer.CommonObserver;
import ai.txai.common.router.ARouterConstants;
import ai.txai.common.router.provider.BizProvider;
import ai.txai.commonbiz.bean.OrderDetailResponse;
import ai.txai.commonbiz.data.BizData;
import ai.txai.commonbiz.repository.BizApiRepository;
import ai.txai.commonbiz.utils.ViewHelper;
import ai.txai.database.order.Order;
import ai.txai.database.site.Site;
import ai.txai.database.vehicle.VehicleModel;

/**
 * Time: 22/04/2022
 * Author Hay
 */
@Route(path = ARouterConstants.PATH_SERVICE_BIZ)
public class BizProviderImpl implements BizProvider {
    private static final String TAG = "MainProviderImpl";

    @Override
    public Order getOrder(String orderId) {
        Order order = BizData.getInstance().getOrder(orderId);
        if (order != null) {
            return order;
        }
        final Order[] orders = new Order[1];
        CountDownLatch latch = new CountDownLatch(1);
        BizApiRepository.INSTANCE.detailOrder(orderId)
                .subscribe(new CommonObserver<OrderDetailResponse>() {
                    @Override
                    public void onSuccess(@Nullable OrderDetailResponse detailResponse) {
                        super.onSuccess(detailResponse);
                        orders[0] = detailResponse.toOrder();
                        latch.countDown();
                    }

                    @Override
                    public void onFailed(@Nullable String msg) {
                        super.onFailed(msg);
                        latch.countDown();
                    }
                });

        try {
            latch.await(1, TimeUnit.MINUTES);
        } catch (Exception e) {
        }
        return orders[0];
    }

    @Override
    public void updateSiteName(TextView textView, String siteId) {
        ViewHelper.updateSiteLabel(textView, siteId);
    }


    @Override
    public void updateVehicleModel(TextView textView, String modelId) {
        ViewHelper.updateVehicleModelLabel(textView, modelId);
    }

    @Override
    public void init(Context context) {

    }
}
