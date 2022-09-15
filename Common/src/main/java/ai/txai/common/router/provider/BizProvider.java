package ai.txai.common.router.provider;

import android.widget.TextView;

import com.alibaba.android.arouter.facade.template.IProvider;

import ai.txai.common.router.bean.PayMethod;
import ai.txai.database.order.Order;
import ai.txai.database.site.Site;
import ai.txai.database.vehicle.VehicleModel;

/**
 * Time: 22/04/2022
 * Author Hay
 */
public interface BizProvider extends IProvider {
    Order getOrder(String orderId);

    void updateSiteName(TextView textView, String siteId);

    void updateVehicleModel(TextView textView, String modelId);
}
