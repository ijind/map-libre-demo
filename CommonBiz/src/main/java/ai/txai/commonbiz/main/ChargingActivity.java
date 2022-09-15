package ai.txai.commonbiz.main;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.lifecycle.Observer;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.blankj.utilcode.util.SizeUtils;

import java.util.ArrayList;
import java.util.List;

import ai.txai.common.log.LOG;
import ai.txai.common.router.ARouterConstants;
import ai.txai.commonbiz.data.BizData;
import ai.txai.commonbiz.fragment.EstimatedFareFragment;
import ai.txai.commonbiz.mapbox.service.MapService;
import ai.txai.commonbiz.view.ChargingView;
import ai.txai.commonbiz.viewmodel.ChargingViewModel;
import ai.txai.database.enums.TripState;
import ai.txai.database.order.Order;
import ai.txai.database.router.Router;
import ai.txai.database.site.Site;
import ai.txai.database.vehicle.Vehicle;
import ai.txai.database.vehicle.VehicleIndicator;
import kotlin.Pair;

/**
 * Time: 30/03/2022
 * Author Hay
 */

@Route(path = ARouterConstants.PATH_ACTIVITY_CHARGING)
public class ChargingActivity extends TripDetailsActivity<ChargingView, ChargingViewModel> {

    @Override
    public void initObservable() {

        final Order order = viewModel.getOrder();
        if (order == null) {
            LOG.e(TAG, "trip state error!!!, should never run this");
            return;
        }

        BizData.getInstance().requestSite(new BizData.SingleSiteChangeListener() {
            @Override
            public void onLoaded(Site... site) {
                final Site dropOffSite = site[0];
                if (dropOffSite != null) {
                    bottomView.updateTo(dropOffSite);
                    mapBoxService.drawDropOffSmall(dropOffSite.getPoint());
                }

                Router router = viewModel.getTripModel().getTripStateMachine().getEstRouter();
                if (router != null && router.getPath() != null) {
                    mapBoxService.drawRouter(router.getPath(), Color.parseColor("#6592FF"));
                    VehicleIndicator vehicle = viewModel.getTripModel().getTripStateMachine().getVehicle();
                    if (vehicle != null) {
                        List<VehicleIndicator> list = new ArrayList<>();
                        list.add(vehicle);
                        mapBoxService.drawVehicles(list);
                    }
                    centerPoints(router.getPath());
                } else if (dropOffSite != null) {
                    centerPoints(dropOffSite.getPoint());
                }
            }
        }, order.getDropOffId());


        final String vehicleNo = order.getVehicleNo();
        final String vehicleModelId = order.getVehicleModelId();

        viewModel.getVehicleDetail().observe(this, new Observer<Vehicle>() {
            @Override
            public void onChanged(Vehicle vehicleDetailResponse) {
                bottomView.updateVehicle(vehicleDetailResponse);
            }
        });

        viewModel.getVehicleNotify().observe(this, new Observer<Pair<VehicleIndicator, Router>>() {
            @Override
            public void onChanged(Pair<VehicleIndicator, Router> pair) {
                if (pair == null) {
                    return;
                }
                final VehicleIndicator first = pair.getFirst();
                if (first != null) {
                    Router router = pair.getSecond();
                    if (router != null && router.getPath() != null) {
                        VehicleIndicator currentVehicle = first.deepCopy();
                        mapBoxService.drawRouter(router.getPath(),
                                currentVehicle,
                                5000,
                                Color.parseColor("#6592FF"),
                                MapService.CHARGING_NOTICE,
                                mapTop(), mapLeftAndRight(), bottomHeight() + SizeUtils.dp2px(45), mapLeftAndRight(),
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Bundle bundle = new Bundle();
                                        bundle.putDouble(EstimatedFareFragment.EXTRA_EST_AMOUNT, first.totalFare);
                                        bundle.putDouble(EstimatedFareFragment.EXTRA_BASE_AMOUNT, first.startPrice);
                                        bundle.putDouble(EstimatedFareFragment.EXTRA_BASE_DISTANCE, first.startDistance);
                                        bundle.putDouble(EstimatedFareFragment.EXTRA_PER_KIM_PRICE, first.perMileagePrice);
                                        bundle.putDouble(EstimatedFareFragment.EXTRA_DISMOUNT, first.discountFare);
                                        bundle.putDouble(EstimatedFareFragment.EXTRA_EST_DISTANCE, first.totalDistance);
                                        bundle.putString(EstimatedFareFragment.EXTRA_VEHICLE_MODEL, vehicleModelId);
                                        viewModel.router(ARouterConstants.PATH_FRAGMENT_ESTIMATED_FARE, bundle);
                                    }
                                });
                    }
                }
            }
        });

        viewModel.requestVehicleDetails(vehicleNo);
        bottomView.updateVehicleModelName(vehicleModelId);

        viewModel.getTripModel().getTripStateMachine().notifyVehicleChange(TripState.Charging);
    }

    @Override
    protected void clickedCurrentLocation() {
        viewModel.getVehicleNotify().postValue(viewModel.getVehicleNotify().getValue());
    }

    @Override
    protected void initData(Bundle args) {
        super.initData(args);
        initDragTripView();
    }
}
