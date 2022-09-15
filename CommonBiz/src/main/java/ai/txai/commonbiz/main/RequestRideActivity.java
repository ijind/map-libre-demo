package ai.txai.commonbiz.main;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.lifecycle.Observer;

import com.alibaba.android.arouter.facade.annotation.Route;

import java.util.ArrayList;
import java.util.List;

import ai.txai.common.activity.DispatcherActivity;
import ai.txai.common.base.BaseActivity;
import ai.txai.common.dialog.DialogCreator;
import ai.txai.common.json.GsonManager;
import ai.txai.common.log.LOG;
import ai.txai.common.router.ARouterConstants;
import ai.txai.common.router.ARouterUtils;
import ai.txai.commonbiz.R;
import ai.txai.commonbiz.bean.OrderResponse;
import ai.txai.commonbiz.bean.TripPlanResponse;
import ai.txai.commonbiz.data.BizData;
import ai.txai.commonbiz.entries.RequestRideItem;
import ai.txai.commonbiz.fragment.EstimatedFareFragment;
import ai.txai.commonbiz.onetrip.TripModel;
import ai.txai.commonbiz.search.SearchActivity;
import ai.txai.commonbiz.view.RequestRideView;
import ai.txai.commonbiz.viewmodel.RequestRideViewModel;
import ai.txai.database.location.Point;
import ai.txai.database.router.Router;
import ai.txai.database.site.Site;
import ai.txai.mapsdk.utils.MapBoxUtils;

/**
 * Time: 30/03/2022
 * Author Hay
 */

@Route(path = ARouterConstants.PATH_ACTIVITY_REQUEST_RIDE)
public class RequestRideActivity extends TripDetailsActivity<RequestRideView, RequestRideViewModel> {

    @Override
    public void initObservable() {
        enableLocation();
        viewModel.getOrderResponse().observe(this, new Observer<OrderResponse>() {
            @Override
            public void onChanged(OrderResponse response) {
                Bundle args = new Bundle();
                ARouterUtils.navigation(RequestRideActivity.this, ARouterConstants.PATH_ACTIVITY_PENDING, args);
            }
        });

        viewModel.getVModels().observe(this, vehicleModelResponses -> {
            LOG.d(TAG, "vehicleModelResponses:%s", GsonManager.GsonString(vehicleModelResponses));
            bottomView.updateModel(vehicleModelResponses);
            viewModel.requestTripPlan();
        });


        viewModel.getTriPlaResponses().observe(this, tripPlans -> {
            if (tripPlans == null || tripPlans.isEmpty()) {
                Site pickUpSite = viewModel.getPickUpSite();
                Site dropOffSite = viewModel.getDropOffSite();
                List<Point> points = new ArrayList<>();
                points.add(pickUpSite.getPoint());
                points.add(dropOffSite.getPoint());
                centerPoints(points);
                mapBoxService.drawPickUpSmall(pickUpSite.getPoint());
                mapBoxService.drawDropOffSmall(dropOffSite.getPoint());
                mapBoxService.drawPickUpNameV2(viewModel.getPickUpSite(), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        gotoSelectPickupActivity();
                    }
                });
                mapBoxService.drawDropOffNameV2(viewModel.getDropOffSite(), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        gotoSelectDropOffActivity();
                    }
                });

                DialogCreator.showConfirmDialog(
                        RequestRideActivity.this,
                        getString(R.string.biz_no_trip_available_vehicle), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ARouterUtils.navigation(
                                        BaseActivity.getLastActivity(),
                                        ARouterConstants.PATH_ACTIVITY_V2_MAIN
                                );
                            }
                        });
                return;
            }
            bottomView.updateTrips(tripPlans);

            bottomView.post(new Runnable() {
                @Override
                public void run() {
                    mapBoxService.logoBottom(bottomHeight());
                }
            });
        });

        intiRequestView();
    }

    private void intiRequestView() {

        bottomView.setOnItemClickListener(new RequestRideView.OnItemClickListener() {
            @Override
            public void onItemSelected(View v, RequestRideItem item) {
                if (item == null || item.getTripPlan() == null || item.getVehicleModel() == null) {
                    showToast("selected vehicle unavailable", false);
                    return;
                }
                LOG.d(TAG, "router selected :%s", GsonManager.GsonString(item.getTripPlan()));
                Point pickUpPoint = viewModel.getPickUpSite().getPoint();
                Point dropOffPoint = viewModel.getDropOffSite().getPoint();
                if (item.getTripPlan() != null) {
                    Router router = TripPlanResponse.toRouter(item.getTripPlan());
                    if (router != null && router.getPath() != null) {
                        Point centerPoint = MapBoxUtils.centerPoint(router.getPath());
                        mapBoxService.drawRouter(router.getPath(), Color.parseColor("#6592FF"));
                        mapBoxService.drawEstRouteNotice(centerPoint, item.getTripPlan().estimateTripInfo.eta, item.getTripPlan().estimateTripInfo.emt);
                        centerPoints(router.getPath());
                    }
                } else {
                    List<Point> points = new ArrayList<>();
                    points.add(pickUpPoint);
                    points.add(dropOffPoint);
                    centerPoints(points);
                }

                mapBoxService.drawPickUpSmall(pickUpPoint);
                mapBoxService.drawDropOffSmall(dropOffPoint);
                mapBoxService.drawPickUpNameV2(viewModel.getPickUpSite(), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        gotoSelectPickupActivity();
                    }
                });
                mapBoxService.drawDropOffNameV2(viewModel.getDropOffSite(), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        gotoSelectDropOffActivity();
                    }
                });

                viewModel.setVModel(item.getVehicleModel());
            }

            @Override
            public void onItemInfoClick(View v, RequestRideItem item) {
                if (item.getTripPlan() == null || item.getTripPlan().fareInfo == null || item.getTripPlan().estimateTripInfo == null) {
                    showToast("No available vehicle", false);
                    return;
                }

                Bundle bundle = new Bundle();
                bundle.putDouble(EstimatedFareFragment.EXTRA_EST_AMOUNT, item.getTripPlan().fareInfo.totalFare);
                bundle.putDouble(EstimatedFareFragment.EXTRA_BASE_AMOUNT, item.getTripPlan().fareInfo.baseFare);
                bundle.putDouble(EstimatedFareFragment.EXTRA_BASE_DISTANCE, item.getTripPlan().fareInfo.baseMileage);
                bundle.putDouble(EstimatedFareFragment.EXTRA_PER_KIM_PRICE, item.getTripPlan().fareInfo.perMileageSurcharge);
                bundle.putDouble(EstimatedFareFragment.EXTRA_DISMOUNT, item.getTripPlan().fareInfo.discountFare);
                bundle.putDouble(EstimatedFareFragment.EXTRA_EST_DISTANCE, item.getTripPlan().estimateTripInfo.emt);
                bundle.putString(EstimatedFareFragment.EXTRA_VEHICLE_MODEL, item.getTripPlan().vehicleTypeId);
                viewModel.router(ARouterConstants.PATH_FRAGMENT_ESTIMATED_FARE, bundle);
            }
        });
        bottomView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.confirmOrder();
            }
        });
    }

    private void gotoSelectPickupActivity() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(SearchActivity.EXTRA_IS_PICK_UP, true);
        com.mapbox.geojson.Point point = TripModel.getInstance().getCurrentPoint();
        if (point != null) {
            bundle.putString(SearchActivity.EXTRA_CURRENT_POINT, GsonManager.GsonString(new Point(point.longitude(), point.latitude())));
        }
        Site pickUpSite = viewModel.getPickUpSite();
        if (pickUpSite != null) {
            bundle.putString(SearchActivity.EXTRA_PICK_UP_SITE, pickUpSite.getId());
        }
        Site value = viewModel.getDropOffSite();
        if (value != null) {
            bundle.putString(SearchActivity.EXTRA_DROP_OFF_SITE, value.getId());
        }
        bundle.putBoolean(TripDetailsActivity.EXTRA_BACK_ACTION, true);
        DispatcherActivity.startActivity(ARouterConstants.PATH_ACTIVITY_SEARCH_IN_MAP, bundle, this, new DispatcherActivity.ICallback() {
            @Override
            public void onSuccess(String content) {
                BizData.getInstance().requestSite(site -> {
                    viewModel.setPickUpSite(site[0]);
                    viewModel.requestTripPlan();
                }, content);
            }
            @Override
            public void onFailed() {
            }
        });
    }


    private void gotoSelectDropOffActivity() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(SearchActivity.EXTRA_IS_PICK_UP, false);
        com.mapbox.geojson.Point point = TripModel.getInstance().getCurrentPoint();
        if (point != null) {
            bundle.putString(SearchActivity.EXTRA_CURRENT_POINT, GsonManager.GsonString(new Point(point.longitude(), point.latitude())));
        }
        Site pickUpSite = viewModel.getPickUpSite();
        if (pickUpSite != null) {
            bundle.putString(SearchActivity.EXTRA_PICK_UP_SITE, pickUpSite.getId());
        }

        Site value = viewModel.getDropOffSite();
        if (value != null) {
            bundle.putString(SearchActivity.EXTRA_DROP_OFF_SITE, value.getId());
        }
        DispatcherActivity.startActivity(ARouterConstants.PATH_ACTIVITY_SEARCH, bundle, this, new DispatcherActivity.ICallback() {
            @Override
            public void onSuccess(String content) {
                BizData.getInstance().requestSite(site -> {
                    viewModel.setDropOffSite(site[0]);
                    viewModel.requestTripPlan();
                }, content);
            }

            @Override
            public void onFailed() {

            }
        });
    }

    @Override
    protected void clickedCurrentLocation() {
        bottomView.reSelectItem();
    }

}
