package ai.txai.commonbiz.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.android.arouter.facade.annotation.Route;

import ai.txai.common.base.BaseListFragment;
import ai.txai.common.router.ARouterConstants;
import ai.txai.common.router.ARouterUtils;
import ai.txai.common.utils.AndroidUtils;
import ai.txai.common.utils.DoubleOperate;
import ai.txai.commonbiz.R;
import ai.txai.commonbiz.data.BizData;
import ai.txai.commonbiz.databinding.BizItemTripIntroBinding;
import ai.txai.commonbiz.main.TripDetailsActivity;
import ai.txai.commonbiz.onetrip.TripModel;
import ai.txai.commonbiz.utils.ViewHelper;
import ai.txai.commonbiz.viewmodel.TripListViewModel;
import ai.txai.database.enums.TripState;
import ai.txai.database.order.Order;
import ai.txai.database.site.Site;
import ai.txai.database.vehicle.VehicleModel;
import ai.txai.push.payload.eunms.OrderState;
import ai.txai.push.payload.eunms.PayState;

/**
 * Time: 15/03/2022
 * Author Hay
 */
@Route(path = ARouterConstants.PATH_FRAGMENT_TRIP_LIST)
public class TripListFragment extends BaseListFragment<Order, Long, BizItemTripIntroBinding, TripListViewModel> {

    @Override
    protected int getCustomTitle() {
        return R.string.biz_my_trip;
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
        switch (tripState) {
            case Completed:
                itemBinding.tvOrderState.setText(R.string.biz_ui_order_status_completed);
                itemBinding.tvAmount.setText(getString(R.string.biz_aed_with_amount, AndroidUtils.INSTANCE.buildAmount(DoubleOperate.subtract(t.getOrderFare(), t.getDiscountFare()))));
                itemBinding.tvOrderState.setTextColor(getContext().getResources().getColor(R.color.commonview_grey_99));
                itemBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Bundle bundle = new Bundle();
                        bundle.putString("extra.order.id", t.getId());
                        bundle.putBoolean(TripDetailsActivity.EXTRA_BACK_ACTION, true);
                        viewModel.router(ARouterConstants.PATH_ACTIVITY_COMPLETE, bundle);
                    }
                });
                break;

            case Cancelled:
                itemBinding.tvOrderState.setText(R.string.biz_ui_order_status_cancelled);
                itemBinding.tvOrderState.setTextColor(getContext().getResources().getColor(R.color.commonview_grey_99));

                itemBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Bundle args = new Bundle();
                        args.putBoolean(TripDetailsActivity.EXTRA_BACK_ACTION, true);
                        args.putString(TripDetailsActivity.EXTRA_ORDER_ID, t.getId());
                        ARouterUtils.navigation(getActivity(), ARouterConstants.PATH_ACTIVITY_CANCEL, args);
                    }
                });

                break;
            case In_Progress:
                TripState dispatchState = TripModel.getInstance().getTripStateMachine().getCurrentTripState();
                PayState payState = null;
                try {
                    payState = PayState.valueOf(t.getPayStatus());
                } catch (Exception e) {
                    payState = PayState.Unpaid;
                }
                if (dispatchState == TripState.Finished && payState == PayState.Unpaid) {
                    itemBinding.tvAmount.setText(getString(R.string.biz_aed_with_amount, AndroidUtils.INSTANCE.buildAmount(DoubleOperate.subtract(t.getOrderFare(), t.getDiscountFare()))));
                    itemBinding.tvOrderState.setText(R.string.biz_ui_order_status_unpaid);
                    itemBinding.tvOrderState.setTextColor(getContext().getResources().getColor(R.color.commonview_color_FF4D4C));
                    itemBinding.line.setVisibility(View.VISIBLE);
                    itemBinding.tvPayNow.setVisibility(View.VISIBLE);
                    itemBinding.getRoot().setOnClickListener(v -> {
                        Bundle bundle = new Bundle();
                        bundle.putString("extra.order.id", t.getId());
                        bundle.putBoolean(TripDetailsActivity.EXTRA_BACK_ACTION, true);
                        ARouterUtils.navigation(getActivity(), ARouterConstants.PATH_ACTIVITY_FINISH, bundle);
                    });
                    break;
                }
                itemBinding.tvOrderState.setText(R.string.biz_ui_order_status_in_progress);
                itemBinding.tvOrderState.setTextColor(getContext().getResources().getColor(R.color.commonview_color_17A800));
                itemBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        TripState dispatchState = TripModel.getInstance().getTripStateMachine().getCurrentTripState();
                        Bundle bundle = new Bundle();
                        bundle.putString("extra.order.id", t.getId());
                        bundle.putBoolean(TripDetailsActivity.EXTRA_BACK_ACTION, true);
                        switch (dispatchState) {
                            case Pending:
                                ARouterUtils.navigation(getActivity(), ARouterConstants.PATH_ACTIVITY_PENDING, bundle);
                                break;
                            case Dispatched:
                            case Arriving:
                                ARouterUtils.navigation(getActivity(), ARouterConstants.PATH_ACTIVITY_ARRIVING, bundle);
                                break;
                            case Arrived:
                                ARouterUtils.navigation(getActivity(), ARouterConstants.PATH_ACTIVITY_ARRIVED, bundle);
                                break;
                            case Charging:
                                ARouterUtils.navigation(getActivity(), ARouterConstants.PATH_ACTIVITY_CHARGING, bundle);
                                break;
                            case Finished:
                                ARouterUtils.navigation(getActivity(), ARouterConstants.PATH_ACTIVITY_FINISH, bundle);
                                break;
                            case Completed:
                                ARouterUtils.navigation(getActivity(), ARouterConstants.PATH_ACTIVITY_COMPLETE, bundle);
                                break;
                        }
                    }
                });
                break;
        }

    }

    @Override
    protected BizItemTripIntroBinding initItemBinding(ViewGroup parent) {
        return BizItemTripIntroBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
    }
}
