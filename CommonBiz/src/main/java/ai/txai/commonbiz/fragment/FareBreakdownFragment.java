package ai.txai.commonbiz.fragment;

import android.os.Bundle;
import android.view.ViewGroup;

import com.alibaba.android.arouter.facade.annotation.Route;

import ai.txai.common.base.BaseScrollFragment;
import ai.txai.common.mvvm.BaseViewModel;
import ai.txai.common.router.ARouterConstants;
import ai.txai.common.utils.AndroidUtils;
import ai.txai.common.utils.FormatUtils;
import ai.txai.commonbiz.R;
import ai.txai.commonbiz.databinding.BizFragmentFareBreakdownBinding;
import ai.txai.commonbiz.utils.ViewHelper;

/**
 * Time: 09/03/2022
 * Author Hay
 */
@Route(path = ARouterConstants.PATH_FRAGMENT_FARE_BREAKDOWN)
public class FareBreakdownFragment extends BaseScrollFragment<BizFragmentFareBreakdownBinding, BaseViewModel> {
    public static final String EXTRA_BASE_AMOUNT = "extra.base.amount";
    public static final String EXTRA_BASE_DISTANCE = "extra.base.distance";
    public static final String EXTRA_PER_KIM_PRICE = "extra.per.km.price";
    public static final String EXTRA_VEHICLE_MODEL = "extra.vehicle.model";


    @Override
    public void initViewObservable() {
        super.initViewObservable();

        Bundle arguments = getArguments();
        double baseAmount = arguments.getDouble(EXTRA_BASE_AMOUNT);
        double baseDistance = arguments.getDouble(EXTRA_BASE_DISTANCE);
        double perKmPrice = arguments.getDouble(EXTRA_PER_KIM_PRICE);
        String vehicleModel = arguments.getString(EXTRA_VEHICLE_MODEL);

        ViewHelper.updateVehicleModelLabel(itemBinding.tvVehicleModel, vehicleModel);

        itemBinding.tvBaseFareAmount.setText(getString(R.string.biz_aed_with_amount, AndroidUtils.INSTANCE.buildAmount(baseAmount)));
        itemBinding.tvBaseFareDistance.setText(FormatUtils.INSTANCE.buildDistance(baseDistance));
        itemBinding.tvPerKmPrice.setText(getString(R.string.biz_per_mil_aed_with_amount, AndroidUtils.INSTANCE.buildAmount(perKmPrice)));
    }

    @Override
    protected int getCustomTitle() {
        return R.string.biz_order_fare_breakdown_title;
    }

    @Override
    protected BizFragmentFareBreakdownBinding initItemBinding(ViewGroup parent) {
        return BizFragmentFareBreakdownBinding.inflate(getLayoutInflater(), parent, false);
    }
}
