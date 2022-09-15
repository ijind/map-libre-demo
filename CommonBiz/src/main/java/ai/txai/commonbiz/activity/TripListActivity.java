package ai.txai.commonbiz.activity;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.android.arouter.facade.annotation.Route;

import java.util.List;

import ai.txai.common.activity.DispatcherActivity;
import ai.txai.common.base.BaseListActivity;
import ai.txai.common.router.ARouterConstants;
import ai.txai.common.utils.AndroidUtils;
import ai.txai.common.utils.DoubleOperate;
import ai.txai.commonbiz.R;
import ai.txai.commonbiz.data.BizData;
import ai.txai.commonbiz.databinding.BizItemTripIntroBinding;
import ai.txai.commonbiz.utils.ViewHelper;
import ai.txai.commonbiz.viewmodel.SelectTripListViewModel;
import ai.txai.database.order.Order;
import ai.txai.database.site.Site;
import ai.txai.database.vehicle.VehicleModel;
import ai.txai.push.payload.eunms.OrderState;

/**
 * Time: 17/05/2022
 * Author Hay
 */
@Route(path = ARouterConstants.PATH_ACTIVITY_TRIP_LIST)
public class TripListActivity extends BaseListActivity<Order, Long, BizItemTripIntroBinding, SelectTripListViewModel> {

    @Override
    protected int getCustomTitle() {
        return R.string.biz_title_choose_trip;
    }

    @Override
    protected void updateItem(BizItemTripIntroBinding itemBinding, Order t, int pos) {
        itemBinding.tvTime.setText(AndroidUtils.INSTANCE.buildDate(t.getCreateTime()));
        OrderState tripState = OrderState.Completed;
        if (!TextUtils.isEmpty(t.getOrderStatus())) {
            tripState = OrderState.valueOf(t.getOrderStatus());
        }
        ViewHelper.updateSiteLabel(itemBinding.tvPickUpName, t.getPickUpId());
        ViewHelper.updateSiteLabel(itemBinding.tvDropOffName, t.getDropOffId());
        ViewHelper.updateVehicleModelLabel(itemBinding.tvTaxiName, t.getVehicleModelId());

        itemBinding.tvAmount.setText("");
        itemBinding.line.setVisibility(View.GONE);
        itemBinding.tvPayNow.setVisibility(View.GONE);
        itemBinding.getRoot().setOnClickListener(null);
        if (tripState == OrderState.Completed) {
            itemBinding.tvOrderState.setText(R.string.biz_ui_order_status_completed);
            itemBinding.tvAmount.setText(getString(R.string.biz_aed_with_amount, AndroidUtils.INSTANCE.buildAmount(DoubleOperate.subtract(t.getOrderFare(), t.getDiscountFare()))));
            itemBinding.tvOrderState.setTextColor(this.getResources().getColor(R.color.commonview_grey_99));
            itemBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectedTrip(t.getId());
                }
            });
        }
    }

    private void selectedTrip(String orderId) {
        Intent intent = new Intent();
        intent.putExtra(DispatcherActivity.EXTRA_CONTENT, orderId);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    protected BizItemTripIntroBinding initItemBinding(ViewGroup parent) {
        return BizItemTripIntroBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
    }
}
