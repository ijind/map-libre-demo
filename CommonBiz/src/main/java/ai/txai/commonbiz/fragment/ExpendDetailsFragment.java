package ai.txai.commonbiz.fragment;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.lifecycle.Observer;

import com.alibaba.android.arouter.facade.annotation.Route;

import ai.txai.common.base.BaseScrollFragment;
import ai.txai.common.router.ARouterConstants;
import ai.txai.common.utils.AndroidUtils;
import ai.txai.common.utils.DoubleOperate;
import ai.txai.common.utils.FormatUtils;
import ai.txai.commonbiz.R;
import ai.txai.commonbiz.databinding.BizFragmentExpendDetailsBinding;
import ai.txai.commonbiz.utils.DataUtils;
import ai.txai.commonbiz.viewmodel.ExpendDetailsViewModel;
import ai.txai.database.order.Order;
import ai.txai.database.order.bean.FareInfoBean;
import ai.txai.database.order.bean.PayOrderInfoBean;

/**
 * Time: 09/03/2022
 * Author Hay
 */
@Route(path = ARouterConstants.PATH_FRAGMENT_EXPEND_DETAILS)
public class ExpendDetailsFragment extends BaseScrollFragment<BizFragmentExpendDetailsBinding, ExpendDetailsViewModel> {
    public static final String EXTRA_ORDER_ID = "extra.order.id";

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        backgroundColor(R.color.commonview_grey_f6);
        viewModel.orderDetail.observe(this, new Observer<Order>() {
            @Override
            public void onChanged(Order order) {
                FareInfoBean fareInfo = order.getFareInfo();
                itemBinding.tvAmount.setText(AndroidUtils.INSTANCE.buildAmount(order.dueFare));
                if (fareInfo != null) {
                    itemBinding.tvBaseFareDescription.setText(getContext().getString(R.string.biz_order_base_mileage,
                            FormatUtils.INSTANCE.buildDistanceWithKm(fareInfo.baseMileage)));
                    itemBinding.tvBaseFareAmount.setText(getString(R.string.biz_aed_with_amount,
                            AndroidUtils.INSTANCE.buildAmount(fareInfo.baseFare)));

                    double mileageDistance = DoubleOperate.subtract(fareInfo.totalMileage, fareInfo.baseMileage);
                    if (mileageDistance <= 0.0) {
                        disableMileage();
                    } else {
                        enableMileage();
                        itemBinding.tvMileageFareDescription.setText(getContext().getString(R.string.biz_order_mileage_description, FormatUtils.INSTANCE.buildDistanceWithKm(mileageDistance)));
                        itemBinding.tvMileageFareAmount.setText(getString(R.string.biz_aed_with_amount, AndroidUtils.INSTANCE.buildAmount(fareInfo.mileageSurcharge)));
                    }

                    itemBinding.tvFareBreakdown.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Bundle args = new Bundle();
                            args.putDouble(FareBreakdownFragment.EXTRA_BASE_AMOUNT, fareInfo.baseFare);
                            args.putDouble(FareBreakdownFragment.EXTRA_BASE_DISTANCE, fareInfo.baseMileage);
                            args.putDouble(FareBreakdownFragment.EXTRA_PER_KIM_PRICE, fareInfo.perMileageSurcharge);
                            args.putString(FareBreakdownFragment.EXTRA_VEHICLE_MODEL, order.getVehicleModelId());
                            viewModel.router(ARouterConstants.PATH_FRAGMENT_FARE_BREAKDOWN, args);
                        }
                    });
                }
                PayOrderInfoBean payOrderInfo = order.getPayOrderInfo();
                if (payOrderInfo != null) {
                    itemBinding.tvPaymentMethodDescription.setText(payOrderInfo.payMethod);
                    itemBinding.tvPaymentTimeDescription.setText(AndroidUtils.INSTANCE.buildDate(payOrderInfo.paidAt));
                } else {
                    itemBinding.tvPaymentMethodTitle.setVisibility(View.GONE);
                    itemBinding.tvPaymentMethodDescription.setVisibility(View.GONE);
                    itemBinding.tvPaymentTimeTitle.setVisibility(View.GONE);
                    itemBinding.tvPaymentTimeDescription.setVisibility(View.GONE);
                }
                itemBinding.tvOrderNumberDescription.setText(order.getId());
                if (DataUtils.isFreeOrder(order)) {
                    visibleDiscount(View.VISIBLE);
                } else {
                    visibleDiscount(View.GONE);
                }


            }
        });
    }

    @Override
    protected int getCustomTitle() {
        return R.string.biz_txai_receipt_details;
    }

    @Override
    protected BizFragmentExpendDetailsBinding initItemBinding(ViewGroup parent) {
        return BizFragmentExpendDetailsBinding.inflate(getLayoutInflater(), parent, false);
    }

    private void visibleDiscount(int visible) {
        itemBinding.tvDiscountRate.setVisibility(visible);
        itemBinding.tvDiscountDescription.setVisibility(visible);
        itemBinding.line2.setVisibility(visible);
    }


    public void disableMileage() {
        itemBinding.tvMileageFareTitle.setVisibility(View.GONE);
        itemBinding.tvMileageFareDescription.setVisibility(View.GONE);
        itemBinding.tvMileageFareAmount.setVisibility(View.GONE);
    }

    public void enableMileage() {
        itemBinding.tvMileageFareTitle.setVisibility(View.VISIBLE);
        itemBinding.tvMileageFareDescription.setVisibility(View.VISIBLE);
        itemBinding.tvMileageFareAmount.setVisibility(View.VISIBLE);
    }
}
