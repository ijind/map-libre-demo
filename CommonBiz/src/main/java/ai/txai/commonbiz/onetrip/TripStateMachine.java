package ai.txai.commonbiz.onetrip;

import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.txai.common.log.LOG;
import ai.txai.common.router.bean.PayStatus;
import ai.txai.common.statemachine.State;
import ai.txai.common.statemachine.StateMachine;
import ai.txai.commonbiz.BuildConfig;
import ai.txai.commonbiz.bean.PendingCountDown;
import ai.txai.commonbiz.bean.WaitingQueueBean;
import ai.txai.commonbiz.data.BizData;
import ai.txai.commonbiz.utils.DataUtils;
import ai.txai.database.enums.TripState;
import ai.txai.database.location.Point;
import ai.txai.database.order.Order;
import ai.txai.database.router.Router;
import ai.txai.database.site.Site;
import ai.txai.database.vehicle.VehicleIndicator;
import ai.txai.database.vehicle.VehicleModel;
import ai.txai.push.payload.eunms.DispatchState;
import ai.txai.push.payload.notify.DispatchStatusNotify;
import ai.txai.push.payload.notify.DispatchTripNotify;
import ai.txai.push.payload.notify.DispatchWaitingNotify;
import ai.txai.push.payload.notify.OrderStatusNotify;
import ai.txai.push.payload.notify.PaymentStatusNotify;
import ai.txai.push.payload.notify.VehicleStatusNotify;

/**
 * Time: 08/03/2022
 * Author Hay
 */
public class TripStateMachine extends StateMachine {
    public static final String Cancelled = "Cancelled";
    public static final String Paid_Failure = "Paid_Failure";
    public static final String Paid_Success = "Paid_Success";
    public static final String Paid_In_Progress = "In_Progress";
    public static final String Paid = "Paid";

    public static final String Paid_Failure_Expired = "Expired";

    private static final String TAG = "TripStateMachine";
    private TripModel tripModel;
    private OnStateChangeListener listener;

    private Site pickUpSite;
    private Site dropOffSite;
    private VehicleModel vehicleModel;
    private VehicleIndicator vehicle;
    private volatile Order order;

    private WaitingQueueBean queueBean;

    private IdleState idleState = new IdleState();
    private WaitingState waitingState = new WaitingState();
    private CarArrivingState carArrivingState = new CarArrivingState();
    private CarWaitingState carWaitingState = new CarWaitingState();
    private CarCarryState carCarryState = new CarCarryState();
    private FinishedState carArrivedState = new FinishedState();
    private PaidState paidState = new PaidState();

    private BaseState currentTrip = idleState;
    private TripState currentTripState = idleState.tripState;

    private List<BaseState> statesList = new ArrayList<>();

    private PendingCountDown pendingCountDown;

    private Map<String, Boolean> payComplete = new HashMap<>();

    protected TripStateMachine(String name, TripModel tripModel, Looper looper) {
        super(name, looper);
        this.tripModel = tripModel;
        setDbg(BuildConfig.DEBUG);
        initializeStates();
        start();
    }

    private void initializeStates() {
        addStateToMachine(idleState);
        addStateToMachine(waitingState);
        addStateToMachine(carArrivingState);
        addStateToMachine(carWaitingState);
        addStateToMachine(carCarryState);
        addStateToMachine(carArrivedState);
        addStateToMachine(paidState);
        setInitialState(idleState);
    }

    private void addStateToMachine(BaseState state) {
        addState(state);
        statesList.add(state);
    }

    public BaseState getCurrentTrip() {
        return currentTrip;
    }

    public TripState getCurrentTripState() {
        return currentTripState;
    }

    public Site getPickUpSite() {
        return pickUpSite;
    }

    public Site getDropOffSite() {
        return dropOffSite;
    }


    public void setPickUpSite(Site pickUpSite) {
        this.pickUpSite = pickUpSite;
    }

    public void setDropOffSite(Site dropOffSite) {
        this.dropOffSite = dropOffSite;
    }

    public VehicleModel getVehicleModel() {
        return vehicleModel;
    }

    public void setVehicleModel(VehicleModel vehicleModel) {
        this.vehicleModel = vehicleModel;
    }

    public VehicleIndicator getVehicle() {
        return vehicle;
    }

    public void setVehicle(VehicleIndicator vehicle) {
        this.vehicle = vehicle;
    }

    public WaitingQueueBean getQueueBean() {
        return queueBean;
    }

    public void setQueueBean(WaitingQueueBean queueBean) {
        this.queueBean = queueBean;
    }

    public OnStateChangeListener getListener() {
        return listener;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }


    public void notifyVehicleChange(TripState tripState) {
        if (listener != null) {
            listener.onVehicleChange(vehicle, calculateRouter(tripState));
        }
    }

    public PendingCountDown getPendingCountDown() {
        return pendingCountDown;
    }

    public void setPendingCountDown(PendingCountDown pendingCountDown) {
        this.pendingCountDown = pendingCountDown;
    }

    abstract class BaseState extends State {
        TripState tripState;

        public BaseState(TripState state) {
            this.tripState = state;
        }

        @Override
        public void enter() {
            LOG.i(TAG, "Entering state: [%s]", getName());
            currentTrip = this;
            currentTripState = tripState;

            if (listener != null && tripState != TripState.Idle) {
                listener.onStateChange(tripState);
            }

            if (tripModel != null) {
                tripModel.toUi(order, tripState);
            }
        }

        @Override
        public boolean processMessage(Message msg) {
            LOG.i(TAG, "processMessage what: [%s] [%s] [%s]", msg.what, msg.obj, tripState);
            switch (msg.what) {
                case Commands.COMMAND_TRANSFER_TO:
                    TripState state = (TripState) msg.obj;
                    transitionTo(state);
                    return HANDLED;
            }
            return super.processMessage(msg);
        }

        @Override
        public void exit() {
            LOG.i(TAG, "exit state: [%s]", getName());
        }

        public TripState getTripState() {
            return tripState;
        }

        @Override
        public String getName() {
            return tripState.name();
        }
    }

    private void transitionTo(TripState obj) {
        for (int i = 0; i < statesList.size(); i++) {
            BaseState baseState = statesList.get(i);
            if (baseState.getTripState() == obj) {
                gotoState(baseState);
                return;
            }
        }
    }

    public void gotoState(BaseState newState) {
        LOG.i(TAG, "gotoState: %s", newState.getName());
        BaseState s = newState;
        if (s != null) {
            BaseState preState = (BaseState) getCurrentState();
            BaseState nextState = s;

            if (preState != nextState) {
                transitionTo(s);
            } else {
                LOG.i(TAG, "Do not perform identical transition: " + s.getName());
            }
        }
    }

    public class IdleState extends BaseState {

        public IdleState() {
            super(TripState.Idle);
        }

        @Override
        public boolean processMessage(Message msg) {
            super.processMessage(msg);
            switch (msg.what) {
                case Commands.COMMAND_PAYMENT_CHANGE:
                    PaymentStatusNotify paymentStatusNotify = (PaymentStatusNotify) msg.obj;
                    tripModel.toPaySuccessUi(paymentStatusNotify);
                    break;
            }
            return false;
        }
    }

    private boolean canConfirmDispatch() {
        return pickUpSite != null && dropOffSite != null;
    }


    public class WaitingState extends BaseState {
        public WaitingState() {
            super(TripState.Pending);
        }

        @Override
        public void enter() {
            super.enter();
            if (order != null) {
                BizData.getInstance().saveOrder(order);
            }
        }

        @Override
        public boolean processMessage(Message msg) {
            super.processMessage(msg);
            switch (msg.what) {
                case Commands.COMMAND_QUEUE_CHANGE:
                    DispatchWaitingNotify waitingNotify = (DispatchWaitingNotify) msg.obj;
                    if (queueBean == null) {
                        queueBean = new WaitingQueueBean();
                    }
                    queueBean.waitingCnt = waitingNotify.waitingCnt;
                    queueBean.waitingInx = waitingNotify.waitingInx;
                    if (listener != null) {
                        listener.onQueueChange(waitingNotify);
                    }
                    return HANDLED;
                case Commands.COMMAND_ORDER_DISPATCH:
                    DispatchStatusNotify statusNotify = (DispatchStatusNotify) msg.obj;
                    if (DispatchState.Dispatched.name().equals(statusNotify.dispatchStatus)) {
                        changeOrder(statusNotify);
                        vehicle = BizData.getInstance().getVehicleIndicator(statusNotify.vehicleNo);
                        if (vehicle == null) {
                            vehicle = new VehicleIndicator(statusNotify.vehicleNo);
                        }
                        vehicle.setEmt(statusNotify.pickupEstimateTripInfo.emt);
                        vehicle.setEta(statusNotify.pickupEstimateTripInfo.eta);
                        transitionTo(carArrivingState);
                    }
                    return HANDLED;
                case Commands.COMMAND_ORDER_CHANGE:
                    orderChanged(msg);
                    return HANDLED;

            }
            return false;
        }
    }

    private void changeOrder(DispatchStatusNotify statusNotify) {
        Order order = BizData.getInstance().getOrder(statusNotify.orderId);
        order.setEstimateRouter(DataUtils.toEstPoints(statusNotify));
        order.setPickupRouter(DataUtils.toPickupPoints(statusNotify));
        order.setVehicleNo(statusNotify.vehicleNo);
        order.setCreateTime(statusNotify.dispatchedAt);
        order.setDispatchStatus(statusNotify.dispatchStatus);
        BizData.getInstance().saveOrder(order);
        this.order = order;
    }

    private void changeOrderStatus(DispatchStatusNotify statusNotify) {
        Order order = BizData.getInstance().getOrder(statusNotify.orderId);
        order.setDispatchStatus(statusNotify.dispatchStatus);
        order.setUpdateTime(statusNotify.dispatchedAt);
        BizData.getInstance().saveOrder(order);
        this.order = order;
    }

    private void changeOrderEstRouter(DispatchTripNotify tripNotify) {
        Order order = BizData.getInstance().getOrder(tripNotify.orderId);
        order.setEstimateRouter(DataUtils.toChangedPoints(tripNotify));
        BizData.getInstance().saveOrder(order);
        this.order = order;
    }

    private void changeOrderArrivingRouter(DispatchTripNotify tripNotify) {
        Order order = BizData.getInstance().getOrder(tripNotify.orderId);
        order.setPickupRouter(DataUtils.toChangedPoints(tripNotify));
        BizData.getInstance().saveOrder(order);
        this.order = order;
    }

    private void changeOrder(OrderStatusNotify orderStatusNotify) {
        Order order = BizData.getInstance().getOrder(orderStatusNotify.orderId);
        if (order == null) {
            return;
        }
        order.setOrderStatus(orderStatusNotify.orderStatus);
        if (orderStatusNotify.fareInfo != null) {
            order.setFareInfo(orderStatusNotify.fareInfo.toDBFareInfo());
        }
        if (orderStatusNotify.payOrderInfo != null) {
            order.setPayOrderInfo(orderStatusNotify.payOrderInfo.toDBPayOrderInfo());
            order.setPayStatus(orderStatusNotify.payOrderInfo.payOrderStatus);
            order.setUpdateTime(orderStatusNotify.payOrderInfo.paidAt);
        }
        order.setMemo(orderStatusNotify.memo);
        BizData.getInstance().saveOrder(order);
        this.order = order;
    }

    public class CarArrivingState extends BaseState {
        public CarArrivingState() {
            super(TripState.Dispatched);
        }

        @Override
        public boolean processMessage(Message msg) {
            super.processMessage(msg);
            switch (msg.what) {
                case Commands.COMMAND_ORDER_DISPATCH:
                    DispatchStatusNotify statusNotify = (DispatchStatusNotify) msg.obj;
                    changeOrderStatus(statusNotify);
                    if (DispatchState.Arrived.name().equals(statusNotify.dispatchStatus)) {
                        transitionTo(carWaitingState);
                    }
                    return HANDLED;
                case Commands.COMMAND_ROUTE_CHANGE:
                    DispatchTripNotify tripNotify = (DispatchTripNotify) msg.obj;
                    changeOrderArrivingRouter(tripNotify);
                    return HANDLED;
                case Commands.COMMAND_CAR_CHANGE:
                    vehiclePositionChange(tripState, msg);
                    return HANDLED;
                case Commands.COMMAND_ORDER_CHANGE:
                    orderChanged(msg);
                    return HANDLED;
            }
            return false;
        }
    }

    public Router calculateRouter(TripState tripState) {
        Router router = new Router();

        List<Point> path = null;
        if (tripState == TripState.Arriving
                || tripState == TripState.Dispatched) {
            path = order.getPickupRouter();
        } else if (tripState == TripState.Charging) {
            path = order.getEstimateRouter();
        }
        if (path == null) {
            return null;
        }
        router.setPath(new ArrayList<>(path));
        return router;
    }

    public class CarWaitingState extends BaseState {
        public CarWaitingState() {
            super(TripState.Arrived);
        }

        @Override
        public boolean processMessage(Message msg) {
            super.processMessage(msg);
            switch (msg.what) {
                case Commands.COMMAND_ORDER_DISPATCH:
                    DispatchStatusNotify statusNotify = (DispatchStatusNotify) msg.obj;
                    changeOrderStatus(statusNotify);
                    if (DispatchState.Charging.name().equals(statusNotify.dispatchStatus)) {
                        transitionTo(carCarryState);
                        if (statusNotify.estimateTripInfo != null) {
                            vehicle.setEmt(statusNotify.estimateTripInfo.emt);
                            vehicle.setEta(statusNotify.estimateTripInfo.eta);
                        }
                    }
                    return HANDLED;
                case Commands.COMMAND_ROUTE_CHANGE:
                    DispatchTripNotify tripNotify = (DispatchTripNotify) msg.obj;
                    changeOrderEstRouter(tripNotify);
                    return HANDLED;
                case Commands.COMMAND_CAR_CHANGE:
                    vehiclePositionChange(tripState, msg);
                    return HANDLED;

                case Commands.COMMAND_ORDER_CHANGE:
                    orderChanged(msg);
                    return HANDLED;
            }
            return false;
        }
    }


    private void vehiclePositionChange(TripState tripState, Message msg) {
        VehicleStatusNotify vehicleStatusNotify = (VehicleStatusNotify) msg.obj;
        Router nowRouter = calculateRouter(tripState);
        var currentVehicle = DataUtils.toVehicle(vehicleStatusNotify);
        if (vehicle == null) {
            vehicle = currentVehicle;
        } else {
            vehicle.setPoint(currentVehicle.point);
            if (vehicleStatusNotify.remainingTripInfo != null) {
                vehicle.setEmt(vehicleStatusNotify.remainingTripInfo.remainingEmt);
                vehicle.setEta(vehicleStatusNotify.remainingTripInfo.remainingEta);
            }
            if (vehicleStatusNotify.fareInfo != null) {
                vehicle.setTotalFare(vehicleStatusNotify.fareInfo.totalFare);
                vehicle.setTotalDistance(vehicleStatusNotify.fareInfo.totalMileage);
                vehicle.setDiscountFare(vehicleStatusNotify.fareInfo.discountFare);
                vehicle.setStartPrice(vehicleStatusNotify.fareInfo.baseFare);
                vehicle.setStartDistance(vehicleStatusNotify.fareInfo.baseMileage);
                vehicle.setPerMileagePrice(vehicleStatusNotify.fareInfo.perMileageSurcharge);
            }
        }
        if (listener != null) {
            listener.onVehicleChange(vehicle, nowRouter);
        }
    }

    public class CarCarryState extends BaseState {
        public CarCarryState() {
            super(TripState.Charging);
        }

        @Override
        public boolean processMessage(Message msg) {
            super.processMessage(msg);
            switch (msg.what) {
                case Commands.COMMAND_ORDER_DISPATCH:
                    DispatchStatusNotify statusNotify = (DispatchStatusNotify) msg.obj;
                    changeOrderStatus(statusNotify);
                    if (DispatchState.Finished.name().equals(statusNotify.dispatchStatus)) {
                        changeOrderPay(statusNotify);
                        transitionTo(carArrivedState);
                    }
                    return HANDLED;

                case Commands.COMMAND_ORDER_CHANGE:
                    orderChanged(msg);
                    return HANDLED;
                case Commands.COMMAND_CAR_CHANGE:
                    vehiclePositionChange(tripState, msg);
                    return HANDLED;
            }
            return false;
        }
    }

    private void orderChanged(Message msg) {
        OrderStatusNotify orderStatusNotify = (OrderStatusNotify) msg.obj;
        changeOrder(orderStatusNotify);
        if (orderStatusNotify.orderStatus.equals("Cancelled")) {
            if (TextUtils.equals(orderStatusNotify.cancelType, OrderStatusNotify.AUTO_CANCEL)) {
                tripModel.toUi(order, TripState.Cancelled, orderStatusNotify.memo);
            } else {
                tripModel.toUi(order, TripState.Manual_Cancelled, orderStatusNotify.memo);
            }
            resetMachine();
            transitionTo(idleState);
        } else if (orderStatusNotify.orderStatus.equals("Completed")) {
            tripModel.toUi(order, TripState.Completed);
            resetMachine();
            transitionTo(idleState);
        }
        if (listener != null) {
            listener.onOrderStatusChange(orderStatusNotify);
        }
    }

    private void changeOrderPay(DispatchStatusNotify statusNotify) {
        Order order = BizData.getInstance().getOrder(statusNotify.orderId);
        order.setDispatchStatus(statusNotify.dispatchStatus);
        if (statusNotify.fareInfo != null) {
            order.setFareInfo(statusNotify.fareInfo.toDBFareInfo());
        }
        order.setOrderFare(statusNotify.orderFare);
        order.setDueFare(statusNotify.dueFare);
        order.setDiscountFare(statusNotify.discountFare);
        if (statusNotify.orderTripInfo != null) {
            order.setAllDistance(statusNotify.orderTripInfo.mileage.all);
            order.setAutoDistance(statusNotify.orderTripInfo.mileage.auto);
            order.setManualDistance(statusNotify.orderTripInfo.mileage.manual);
            order.setAllDuration(statusNotify.orderTripInfo.duration.all);
            order.setAutoDuration(statusNotify.orderTripInfo.duration.auto);
            order.setManualDuration(statusNotify.orderTripInfo.duration.manual);
        }
        BizData.getInstance().saveOrder(order);
        this.order = order;
    }

    public class FinishedState extends BaseState {
        public FinishedState() {
            super(TripState.Finished);
        }

        @Override
        public boolean processMessage(Message msg) {
            super.processMessage(msg);
            switch (msg.what) {
                case Commands.COMMAND_CAR_CHANGE:
                    vehiclePositionChange(tripState, msg);
                    return HANDLED;
                case Commands.COMMAND_ORDER_CHANGE:
                    orderChanged(msg);
                    return HANDLED;
                case Commands.COMMAND_PAYMENT_CHANGE:
                    PaymentStatusNotify paymentStatusNotify = (PaymentStatusNotify) msg.obj;
                    Boolean completeN = payComplete.get(paymentStatusNotify.transactionId);
                    if (completeN != null && completeN) {
                        return HANDLED;
                    }
                    orderChanged(paymentStatusNotify);
                    return HANDLED;
                case Commands.COMMAND_PAYMENT_CHANGE_MANUAL:
                    PayStatus status = (PayStatus) msg.obj;
                    Boolean complete = payComplete.get(status.transactionId);
                    if (complete != null && complete) {
                        return HANDLED;
                    }
                    orderChanged(status);

                    return HANDLED;
            }
            return false;
        }
    }

    private void orderChanged(PayStatus status) {
        if (TextUtils.equals(status.paymentStatus, Paid_Success)) {
            return;
        }
        if (!TextUtils.equals(status.paymentStatus, Paid_In_Progress)) {
            payComplete.put(status.transactionId, true);
        }
        tripModel.toUi(status);
    }

    private void orderChanged(PaymentStatusNotify notify) {
        if (TextUtils.equals(notify.paymentStatus, Paid_Success)) {
            return;
        }

        if (!TextUtils.equals(notify.paymentStatus, Paid_In_Progress)) {
            payComplete.put(notify.transactionId, true);
        }
        tripModel.toUi(notify);
    }

    public class PaidState extends BaseState {
        public PaidState() {
            super(TripState.Completed);
        }

        @Override
        public void enter() {
            super.enter();
        }

        @Override
        public boolean processMessage(Message msg) {
            super.processMessage(msg);
            return false;
        }
    }

    public void setMessageAtFront(int what, TripState tripState) {
        sendMessageAtFrontOfQueue(what, tripState);
    }

    public Router getEstRouter() {
        List<Point> estimateRouter = order.getEstimateRouter();
        if (estimateRouter == null || estimateRouter.isEmpty()) {
            return null;
        }
        return new Router(estimateRouter);
    }

    public Router getPickupRouter() {
        List<Point> pickupRouter = order.getPickupRouter();
        if (pickupRouter == null || pickupRouter.isEmpty()) {
            return null;
        }
        return new Router(pickupRouter);
    }

    public void registerOnStateChangeListener(OnStateChangeListener listener) {
        this.listener = listener;
    }

    public void unregisterOnStateChangeListener(OnStateChangeListener listener) {
        this.listener = null;
    }

    public void resetMachine() {
        LOG.i(TAG, "resetMachine");
        dropOffSite = null;
        vehicle = null;
        order = null;
    }

    public boolean inProgress() {
        return getCurrentTripState().getCode() > TripState.Idle.getCode() && getCurrentTripState().getCode() < TripState.Completed.getCode();
    }
}
