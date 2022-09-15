package ai.txai.commonbiz.utils;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import ai.txai.database.location.Point;
import ai.txai.database.order.Order;
import ai.txai.database.router.Router;
import ai.txai.database.vehicle.VehicleIndicator;
import ai.txai.push.payload.notify.DispatchStatusNotify;
import ai.txai.push.payload.notify.DispatchTripNotify;
import ai.txai.push.payload.notify.SegmentsBean;
import ai.txai.push.payload.notify.VehicleStatusNotify;

/**
 * Time: 17/03/2022
 * Author Hay
 */
public class DataUtils {

    public static VehicleIndicator toVehicle(VehicleStatusNotify notify) {
        return new VehicleIndicator(notify.vehicleNo, notify.vehicleSegment.longitude, notify.vehicleSegment.latitude);
    }

    public static List<Point> toEstPoints(DispatchStatusNotify notify) {
        if (notify.estimateTripInfo == null || notify.estimateTripInfo.segments == null) {
            return new ArrayList<>();
        }
        return toPoints(notify.estimateTripInfo.segments);
    }

    public static List<Point> toPickupPoints(DispatchStatusNotify notify) {
        if (notify.pickupEstimateTripInfo == null || notify.pickupEstimateTripInfo.segments == null) {
            return new ArrayList<>();
        }
        return toPoints(notify.pickupEstimateTripInfo.segments);
    }

    public static List<Point> toChangedPoints(DispatchTripNotify notify) {
        if (notify.remainingTripInfo == null || notify.remainingTripInfo.segments == null) {
            return new ArrayList<>();
        }
        return toPoints(notify.remainingTripInfo.segments);
    }

    @NonNull
    private static List<Point> toPoints(List<SegmentsBean> segments) {
        List<Point> points = new ArrayList<>();
        for (int i = 0; i < segments.size(); i++) {
            SegmentsBean segmentsBean = segments.get(i);
            points.add(new Point(segmentsBean.longitude, segmentsBean.latitude));
        }
        return points;
    }

    public static Router toEstRouter(DispatchStatusNotify notify) {
        if (notify == null || notify.estimateTripInfo == null || notify.estimateTripInfo.segments == null) {
            return null;
        }
        Router router = new Router();
        router.setPath(toPoints(notify.estimateTripInfo.segments));
        return router;
    }

    public static Router toPickUpRouter(DispatchStatusNotify notify) {
        if (notify == null || notify.pickupEstimateTripInfo == null || notify.pickupEstimateTripInfo.segments == null) {
            return null;
        }
        Router router = new Router();
        router.setPath(toPoints(notify.pickupEstimateTripInfo.segments));
        return router;
    }

    public static Router toRouter(DispatchTripNotify notify) {
        if (notify == null || notify.remainingTripInfo == null || notify.remainingTripInfo.segments == null) {
            return null;
        }
        Router router = new Router();
        List<Point> points = new ArrayList<>();
        for (int i = 0; i < notify.remainingTripInfo.segments.size(); i++) {
            SegmentsBean segmentsBean = notify.remainingTripInfo.segments.get(i);
            points.add(new Point(segmentsBean.longitude, segmentsBean.latitude));
        }
        router.setPath(toPoints(notify.remainingTripInfo.segments));
        return router;
    }

    public static boolean isFreeOrder(Order order) {
        if (order == null || order.fareInfo == null) {
            return false;
        }
        return order.fareInfo.freeOrder;
    }
}
