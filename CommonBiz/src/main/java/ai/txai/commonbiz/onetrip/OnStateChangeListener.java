package ai.txai.commonbiz.onetrip;

import ai.txai.database.enums.TripState;
import ai.txai.database.order.Order;
import ai.txai.database.router.Router;
import ai.txai.database.vehicle.VehicleIndicator;
import ai.txai.push.payload.notify.DispatchStatusNotify;
import ai.txai.push.payload.notify.DispatchTripNotify;
import ai.txai.push.payload.notify.DispatchWaitingNotify;
import ai.txai.push.payload.notify.OrderStatusNotify;
import ai.txai.push.payload.notify.VehicleStatusNotify;

/**
 * Time: 15/03/2022
 * Author Hay
 */
public interface OnStateChangeListener {

    void onQueueChange(DispatchWaitingNotify notify);
    void onDispatchStatusChange(TripState state, DispatchStatusNotify notify, Order order);

    void onRouteChange(Order router);
    void onVehicleChange(VehicleIndicator notify, Router router);

    void onStateChange(TripState tripState);

    void onOrderStatusChange(OrderStatusNotify orderStatusNotify);
}
