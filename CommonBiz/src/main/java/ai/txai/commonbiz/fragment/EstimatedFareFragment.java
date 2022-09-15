package ai.txai.commonbiz.fragment;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.android.arouter.facade.annotation.Route;

import ai.txai.common.base.BaseScrollFragment;
import ai.txai.common.mvvm.BaseViewModel;
import ai.txai.common.router.ARouterConstants;
import ai.txai.common.router.ARouterUtils;
import ai.txai.common.utils.AndroidUtils;
import ai.txai.common.utils.DoubleOperate;
import ai.txai.common.utils.FormatUtils;
import ai.txai.commonbiz.R;
import ai.txai.commonbiz.databinding.BizFragmentEstimatedFareBinding;

/**
 * Time: 09/03/2022
 * Author Hay
 */
@Route(path = ARouterConstants.PATH_FRAGMENT_ESTIMATED_FARE)
public class EstimatedFareFragment extends BaseScrollFragment<BizFragmentEstimatedFareBinding, BaseViewModel> {
    public static final String EXTRA_BASE_AMOUNT = "extra.base.amount";
    public static final String EXTRA_BASE_DISTANCE = "extra.base.distance";
    public static final String EXTRA_PER_KIM_PRICE = "extra.per.km.price";
    public static final String EXTRA_EST_DISTANCE = "extra.est.distance";
    public static final String EXTRA_EST_AMOUNT = "extra.est.price";
    public static final String EXTRA_DISMOUNT = "extra.est.dismount";
    public static final String EXTRA_VEHICLE_MODEL = "extra.vehicle.model";

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        backgroundColor(R.color.commonview_grey_f6);
        Bundle arguments = getArguments();

        double baseAmount = arguments.getDouble(EXTRA_BASE_AMOUNT);
        double baseDistance = arguments.getDouble(EXTRA_BASE_DISTANCE);
        double estAmount = arguments.getDouble(EXTRA_EST_AMOUNT);
        double dismount = arguments.getDouble(EXTRA_DISMOUNT);
        double estDistance = arguments.getDouble(EXTRA_EST_DISTANCE);
        double perKmPrice = arguments.getDouble(EXTRA_PER_KIM_PRICE);
        String vehicleModel = arguments.getString(EXTRA_VEHICLE_MODEL);


        itemBinding.tvBaseFareDescription.setText(getContext().getString(R.string.biz_order_base_mileage, FormatUtils.INSTANCE.buildDistanceWithKm(baseDistance)));
        itemBinding.tvBaseFareAmount.setText(getString(R.string.biz_aed_with_amount, AndroidUtils.INSTANCE.buildAmount(baseAmount)));

        double amount = DoubleOperate.subtract(estAmount, dismount);
        itemBinding.tvAmount.setText(AndroidUtils.INSTANCE.buildAmount(amount));

        double mileageDistance = DoubleOperate.subtract(estDistance, baseDistance);
        if (mileageDistance <= 0.0) {
            disableMileage();
        } else {
            enableMileage();
            itemBinding.tvMileageFareDescription.setText(getContext().getString(R.string.biz_order_mileage_description, FormatUtils.INSTANCE.buildDistanceWithKm(mileageDistance)));
            itemBinding.tvMileageFareAmount.setText(getString(R.string.biz_aed_with_amount, AndroidUtils.INSTANCE.buildAmount(DoubleOperate.subtract(estAmount, baseAmount))));
        }

        if (estAmount - dismount == 0) {
            visibleDiscount(View.VISIBLE);
        } else {
            visibleDiscount(View.GONE);
        }

        itemBinding.tvFareBreakdown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putDouble(FareBreakdownFragment.EXTRA_BASE_AMOUNT, baseAmount);
                args.putDouble(FareBreakdownFragment.EXTRA_BASE_DISTANCE, baseDistance);
                args.putDouble(FareBreakdownFragment.EXTRA_PER_KIM_PRICE, perKmPrice);
                args.putString(FareBreakdownFragment.EXTRA_VEHICLE_MODEL, vehicleModel);
                ARouterUtils.navigation(getActivity(), ARouterConstants.PATH_FRAGMENT_FARE_BREAKDOWN, args);
            }
        });
    }

    @Override
    protected int getCustomTitle() {
        return R.string.biz_txai_estimated_fare;
    }

    @Override
    protected BizFragmentEstimatedFareBinding initItemBinding(ViewGroup parent) {
        return BizFragmentEstimatedFareBinding.inflate(getLayoutInflater(), parent, false);
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

    private void visibleDiscount(int visible) {
        itemBinding.tvDiscountRate.setVisibility(visible);
        itemBinding.tvDiscountDescription.setVisibility(visible);
        itemBinding.line2.setVisibility(visible);
    }
}
