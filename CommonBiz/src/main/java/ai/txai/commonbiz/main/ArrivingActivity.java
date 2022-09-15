package ai.txai.commonbiz.main;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.blankj.utilcode.util.SizeUtils;
import com.mapbox.geojson.Point;
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener;
import com.mapbox.turf.TurfMeasurement;

import java.util.ArrayList;
import java.util.List;

import ai.txai.common.log.LOG;
import ai.txai.common.router.ARouterConstants;
import ai.txai.commonbiz.data.BizData;
import ai.txai.commonbiz.mapbox.service.MapService;
import ai.txai.commonbiz.onetrip.TripStateMachine;
import ai.txai.commonbiz.view.ArrivingView;
import ai.txai.commonbiz.view.PrecautionsView;
import ai.txai.commonbiz.viewmodel.ArrivingViewModel;
import ai.txai.database.enums.TripState;
import ai.txai.database.order.Order;
import ai.txai.database.router.Router;
import ai.txai.database.site.Site;
import ai.txai.database.vehicle.Vehicle;
import ai.txai.database.vehicle.VehicleIndicator;
import ai.txai.mapsdk.utils.MapBoxUtils;
import kotlin.Pair;

/**
 * Time: 30/03/2022
 * Author Hay
 */

@Route(path = ARouterConstants.PATH_ACTIVITY_ARRIVING)
public class ArrivingActivity extends TripDetailsActivity<ArrivingView, ArrivingViewModel> implements OnIndicatorPositionChangedListener {
    private Point personPoint;
    private long lastRefresh = 0;
    private PrecautionsView precautionsView;
    private TripStateMachine tripStateMachine;

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapBoxService.addOnIndicatorPositionChangeListener(null);
    }

    @Override
    public void initObservable() {
        mapBoxService.addOnIndicatorPositionChangeListener(this);
        enableLocation();
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
                    LOG.i("Trip", "Car Arriving %s", (router != null && router.getPath() != null));
                    if (router != null && router.getPath() != null) {
                        VehicleIndicator currentVehicle = first.deepCopy();
                        mapBoxService.drawArrivingRouter(router.getPath(),
                                currentVehicle,
                                5000,
                                Color.parseColor("#6592FF"),
                                MapService.RUNNING_NOTICE,
                                mapTop(), mapLeftAndRight(), bottomHeight() + SizeUtils.dp2px(45), mapLeftAndRight(),
                                null,
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                    }
                                });
                    } else {
                        List<VehicleIndicator> vList = new ArrayList<>();
                        vList.add(first.deepCopy());
                        mapBoxService.drawVehicles(vList);
                        if (first.emt > 0.0) {
                            mapBoxService.drawRunningNotice(first);
                        }
                        reCenter(personPoint, tripStateMachine.getPickUpSite().getPoint());
                    }
                }
            }
        });

        tripStateMachine = viewModel.getTripModel().getTripStateMachine();
        final Order order = tripStateMachine.getOrder();
        if (order == null) {
            LOG.e(TAG, "trip state error!!!, should never run this");
            return;
        }
        final String vehicleNo = order.getVehicleNo();
        final String vehicleModelId = order.getVehicleModelId();
        viewModel.requestVehicleDetails(vehicleNo);
        bottomView.updateVehicleModelName(vehicleModelId);
        final Site currentPickUpSite = tripStateMachine.getPickUpSite();
        bottomView.updateTo(currentPickUpSite);
        bottomView.setCancelOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vehicle vehicle = BizData.getInstance().getVehicle(vehicleNo);
                toMakeSureCancel(TripState.Dispatched, vehicle);
            }
        });
        reCenter(personPoint, currentPickUpSite.getPoint());
        mapBoxService.drawPickUpSmall(currentPickUpSite.getPoint());
        mapBoxService.drawPickUpNameV2(currentPickUpSite, null);
        showPrecautionsView();
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

    private void showPrecautionsView() {
        if (precautionsView == null) {
            precautionsView = new PrecautionsView(this);
        }
        precautionsView.showPop();
    }

    @Override
    public void onIndicatorPositionChanged(@NonNull Point point) {
        personPoint = point;
        Order order = viewModel.getOrder();
        if (order == null) {
            return;
        }
        String pickUpId = viewModel.getOrder().getPickUpId();
        if (TextUtils.isEmpty(pickUpId)) {
            return;
        }

        long currentTimeMillis = System.currentTimeMillis();
        if (currentTimeMillis - lastRefresh < 20000) {
            return;
        }
        lastRefresh = currentTimeMillis;
        BizData.getInstance().requestSite(new BizData.SingleSiteChangeListener() {
            @Override
            public void onLoaded(Site... site) {
                final Site pickUpSite = site[0];
                if(pickUpSite == null) {
                    return;
                }
                double distance = TurfMeasurement.distance(point, Point.fromLngLat(pickUpSite.getPoint().getLongitude(), pickUpSite.getPoint().getLatitude()));
                if (distance > 10) {
                    return;
                }
                mapBoxService.drawPersonRoute(point, Point.fromLngLat(pickUpSite.getPoint().getLongitude(), pickUpSite.getPoint().getLatitude()));
                viewModel.getVehicleNotify().postValue(viewModel.getVehicleNotify().getValue());
            }
        }, pickUpId);
    }

    private void reCenter(Point personPoint, ai.txai.database.location.Point point) {
        Router router = tripStateMachine.getPickupRouter();
        LOG.i("Trip", "reCenter Car Arriving %s %s-%s", personPoint, point , (router != null && router.getPath() != null));
        if (router != null && router.getPath() != null) {
            mapBoxService.drawRouter(router.getPath(), Color.parseColor("#6592FF"));
            ArrayList<ai.txai.database.location.Point> points = new ArrayList<>(router.getPath());
            centerPoints(points);
        } else {
            List<ai.txai.database.location.Point> points = new ArrayList<>();
            points.add(point);
            centerPoints(points);
        }
    }
}
