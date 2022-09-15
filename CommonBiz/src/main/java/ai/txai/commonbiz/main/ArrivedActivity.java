package ai.txai.commonbiz.main;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.mapbox.geojson.Point;
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener;
import com.mapbox.turf.TurfMeasurement;

import java.util.ArrayList;
import java.util.List;

import ai.txai.common.log.LOG;
import ai.txai.common.router.ARouterConstants;
import ai.txai.commonbiz.data.BizData;
import ai.txai.commonbiz.onetrip.TripStateMachine;
import ai.txai.commonbiz.view.ArrivedView;
import ai.txai.commonbiz.viewmodel.ArrivedViewModel;
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

@Route(path = ARouterConstants.PATH_ACTIVITY_ARRIVED)
public class ArrivedActivity extends TripDetailsActivity<ArrivedView, ArrivedViewModel> implements OnIndicatorPositionChangedListener {
    private Point personPoint;
    private long lastRefresh = 0;

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
                final VehicleIndicator first = pair.getFirst();
                if (first != null) {
                    List<VehicleIndicator> vList = new ArrayList<>();
                    vList.add(first.deepCopy());
                    mapBoxService.drawVehicles(vList);
                }
            }
        });


        TripStateMachine tripStateMachine = viewModel.getTripModel().getTripStateMachine();
        final Order order = tripStateMachine.getOrder();
        if (order == null) {
            LOG.e(TAG, "trip state error!!!, should never run this");
            return;
        }

        BizData.getInstance().requestSite(new BizData.SingleSiteChangeListener() {
            @Override
            public void onLoaded(Site... site) {
                final Site pickUpSite = site[0];
                if (pickUpSite != null) {
                    bottomView.updateTo(pickUpSite);
                    reCenter(personPoint, pickUpSite.getPoint());
                    mapBoxService.ringPoint(pickUpSite.getPoint());

                }
            }
        }, order.getPickUpId());


        long updateTime = order.getUpdateTime();
        long gap = updateTime + 15 * 60 * 1000 - System.currentTimeMillis();
        if (gap < 0 || gap > 15 * 60 * 1000) {
            gap = 15 * 60 * 1000;
        }
        mapBoxService.drawWaitingWhenArrived((int) (gap / 1000), tripStateMachine.getPickUpSite());
        final String vehicleNo = order.getVehicleNo();
        final String vehicleModelId = order.getVehicleModelId();
        viewModel.requestVehicleDetails(vehicleNo);
        bottomView.updateVehicleModelName(vehicleModelId);

        bottomView.setCancelOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vehicle vehicle = BizData.getInstance().getVehicle(tripStateMachine.getOrder().getVehicleNo());
                toMakeSureCancel(TripState.Arrived, vehicle);
            }
        });
    }

    @Override
    protected void initData(Bundle args) {
        super.initData(args);
        initDragTripView();
    }

    @Override
    protected void clickedCurrentLocation() {
        BizData.getInstance().requestSite(new BizData.SingleSiteChangeListener() {
            @Override
            public void onLoaded(Site... site) {
                final Site pickUpSite = site[0];
                if (pickUpSite != null) {
                    bottomView.updateTo(pickUpSite);
                    reCenter(personPoint, pickUpSite.getPoint());
                }
            }
        }, viewModel.getOrder().getPickUpId());
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
                if (pickUpSite == null) {
                    return;
                }
                double distance = TurfMeasurement.distance(point, Point.fromLngLat(pickUpSite.getPoint().getLongitude(), pickUpSite.getPoint().getLatitude()));
                if (distance > 10) {
                    return;
                }
                personPoint = point;
                mapBoxService.drawPersonRoute(point, Point.fromLngLat(pickUpSite.getPoint().getLongitude(), pickUpSite.getPoint().getLatitude()));
                reCenter(personPoint, pickUpSite.getPoint());
            }
        }, pickUpId);
    }

    private void reCenter(Point personPoint, ai.txai.database.location.Point point) {
        List<ai.txai.database.location.Point> points = new ArrayList<>();
        points.add(point);

        if (personPoint != null
                && MapBoxUtils.distance(new ai.txai.database.location.Point(personPoint.longitude(), personPoint.latitude()), point) < 10) {
            points.add(new ai.txai.database.location.Point(personPoint.longitude(), personPoint.latitude()));
        }
        centerPoints(points);
    }
}
