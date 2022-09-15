package ai.txai.commonbiz.main;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.lifecycle.Observer;

import com.alibaba.android.arouter.facade.annotation.Route;

import java.util.ArrayList;
import java.util.List;

import ai.txai.common.router.ARouterConstants;
import ai.txai.common.router.provider.LostFoundProvider;
import ai.txai.common.router.ProviderManager;
import ai.txai.commonbiz.R;
import ai.txai.commonbiz.data.BizData;
import ai.txai.commonbiz.fragment.ExpendDetailsFragment;
import ai.txai.commonbiz.onetrip.TripStateMachine;
import ai.txai.commonbiz.view.TripCompleteView;
import ai.txai.commonbiz.viewmodel.CompleteViewModel;
import ai.txai.database.location.Point;
import ai.txai.database.site.Site;
import ai.txai.database.vehicle.Vehicle;
import ai.txai.database.vehicle.VehicleModel;

/**
 * Time: 30/03/2022
 * Author Hay
 */

@Route(path = ARouterConstants.PATH_ACTIVITY_COMPLETE)
public class CompleteActivity extends TripDetailsActivity<TripCompleteView, CompleteViewModel> {
    private boolean showSuccessDialog = true;
    @Override
    public void initObservable() {
        viewModel.getVehicleDetail().observe(this, new Observer<Vehicle>() {
            @Override
            public void onChanged(Vehicle vehicleDetailResponse) {
                bottomView.updateVehicle(vehicleDetailResponse);
            }
        });

        viewModel.getOrderDetail().observe(this, order -> {
            BizData.getInstance().requestSite(new BizData.SingleSiteChangeListener() {
                @Override
                public void onLoaded(Site... site) {
                    Site pickUpSite = site[0];
                    Site dropOffSite = site[1];

                    if (pickUpSite != null) {
                        bottomView.updateFrom(pickUpSite);
                        mapBoxService.drawPickUpName(pickUpSite);
                        mapBoxService.drawPickUpSmall(pickUpSite.getPoint());
                    }
                    if (dropOffSite != null) {
                        bottomView.updateTo(dropOffSite);
                        mapBoxService.drawDropOffName(dropOffSite);
                        mapBoxService.drawDropOffSmall(dropOffSite.getPoint());
                    }

                    List<Point> path = order.getEstimateRouter();
                    if (path != null && !path.isEmpty()) {
                        mapBoxService.drawRouter(path, Color.parseColor("#B9BEC3"));
                        centerPoints(path);
                    } else {
                        List<Point> points = new ArrayList<>();
                        if (pickUpSite != null) {
                            points.add(pickUpSite.getPoint());
                        }
                        if (dropOffSite != null) {
                            points.add(dropOffSite.getPoint());
                        }
                        centerPoints(points);
                    }
                }
            }, order.getPickUpId(), order.getDropOffId());


            bottomView.updateDispatchStatus(order);
            bottomView.setExpandClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putString(ExpendDetailsFragment.EXTRA_ORDER_ID, order.getId());
                viewModel.router(ARouterConstants.PATH_FRAGMENT_EXPEND_DETAILS, bundle);
            });
            bottomView.updateVehicleModelName(order.getVehicleModelId());
            bottomView.setLostAndFoundClickListener(v -> {
                LostFoundProvider provide = ProviderManager.provide(ARouterConstants.PATH_SERVICE_LOST_FOUND);
                if (provide != null) {
                    provide.triggerLostFound(CompleteActivity.this, order.getId());
                }
            });

            if (showSuccessDialog) {
                if (TextUtils.equals(order.getPayStatus(), TripStateMachine.Paid) || TextUtils.equals(order.getPayStatus(), TripStateMachine.Paid_Success)) {
                    Bundle extras = getIntent().getExtras();
                    if (!extras.getBoolean(TripDetailsActivity.EXTRA_BACK_ACTION)) {
                        showToast(getString(R.string.biz_order_payment_success), true);
                    }
                }
                showSuccessDialog = false;
            }
        });
    }


    @Override
    protected void clickedCurrentLocation() {
        viewModel.getOrderDetail().postValue(viewModel.getOrder());
    }
}
