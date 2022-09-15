package ai.txai.commonbiz.onetrip;

import android.app.Activity;
import android.os.Bundle;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.google.gson.Gson;
import com.mapbox.geojson.Point;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ai.txai.common.base.BaseActivity;
import ai.txai.common.dialog.DialogCreator;
import ai.txai.common.json.GsonManager;
import ai.txai.common.log.LOG;
import ai.txai.common.observer.CommonObserver;
import ai.txai.common.router.ARouterConstants;
import ai.txai.common.router.ARouterUtils;
import ai.txai.common.router.ProviderManager;
import ai.txai.common.router.bean.PayStatus;
import ai.txai.common.router.provider.PaymentProvider;
import ai.txai.common.thread.TScheduler;
import ai.txai.commonbiz.R;
import ai.txai.commonbiz.bean.CancelOrderResponse;
import ai.txai.commonbiz.bean.OrderDetailResponse;
import ai.txai.commonbiz.bean.OrderResponse;
import ai.txai.commonbiz.bean.VehicleDetailResponse;
import ai.txai.commonbiz.data.BizData;
import ai.txai.commonbiz.main.CompleteActivity;
import ai.txai.commonbiz.main.FinishedActivity;
import ai.txai.commonbiz.main.TripDetailsActivity;
import ai.txai.commonbiz.repository.BizApiRepository;
import ai.txai.database.enums.TripState;
import ai.txai.database.lock.BlockingObject;
import ai.txai.database.order.Order;
import ai.txai.database.site.Site;
import ai.txai.database.vehicle.Vehicle;
import ai.txai.database.vehicle.VehicleModel;
import ai.txai.push.BlueGoPushClient;
import ai.txai.push.common.NotifyClassifyEnum;
import ai.txai.push.listener.PushNotifyListener;
import ai.txai.push.listener.PushStateListener;
import ai.txai.push.payload.PushNotifyPayload;
import ai.txai.push.payload.eunms.OrderState;
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
public class TripModel implements PushNotifyListener, PushStateListener {
    private static final String TAG = TripModel.class.getSimpleName();

    private final List<OnStateChangeListener> onStateChangeListeners = new ArrayList<>();
    private final List<PushStateListener> pushStateListener = new ArrayList<>();

    private BlockingObject<Boolean> startTripBlock = null;

    private ExecutorService msgExecutor = Executors.newSingleThreadExecutor();

    private int oldState;
    private int currentState;

    private Point currentPoint;

    private TripModel() {
        HandlerThread handlerThread = new HandlerThread("One-Trip");
        handlerThread.start();
        tripStateMachine = new TripStateMachine("One-Trip", this, handlerThread.getLooper());
        StateMonitor.getInstance().init(handlerThread.getLooper());
    }

    public void transferTo(@NotNull TripState state) {
        tripStateMachine.sendMessage(Commands.COMMAND_TRANSFER_TO, state);
    }

    public void transferToFront(@NotNull TripState state) {
        tripStateMachine.setMessageAtFront(Commands.COMMAND_TRANSFER_TO, state);
    }

    public Point getCurrentPoint() {
        return currentPoint;
    }

    public void setCurrentPoint(Point currentPoint) {
        this.currentPoint = currentPoint;
    }

    @Override
    public void onReceived(PushNotifyPayload payload) {
        msgExecutor.submit(new Runnable() {
            @Override
            public void run() {

                if (payload == null) {
                    return;
                }

                Gson gson = GsonManager.getGson();
                String jsonStr = payload.getMessageBody().toString();
                NotifyClassifyEnum notifyClassify = payload.getNotifyClassify();
                switch (notifyClassify) {
                    case DispatchWaitingNotify:
                        waitOrderCreated();
                        DispatchWaitingNotify dispatchWaitingNotify = gson.fromJson(jsonStr, DispatchWaitingNotify.class);
                        tripStateMachine.sendMessage(Commands.COMMAND_QUEUE_CHANGE, dispatchWaitingNotify);
                        break;
                    case DispatchStatusNotify:
                        waitOrderCreated();
                        DispatchStatusNotify statusNotify = gson.fromJson(jsonStr, DispatchStatusNotify.class);
                        tripStateMachine.sendMessage(Commands.COMMAND_ORDER_DISPATCH, statusNotify);
                        break;

                    case DispatchTripNotify:
                        waitOrderCreated();
                        DispatchTripNotify dispatchTripNotify = gson.fromJson(jsonStr, DispatchTripNotify.class);
                        tripStateMachine.sendMessage(Commands.COMMAND_ROUTE_CHANGE, dispatchTripNotify);
                        break;

                    case VehicleStatusNotify:
                        waitOrderCreated();
                        VehicleStatusNotify vehicleStatusNotify = gson.fromJson(jsonStr, VehicleStatusNotify.class);
                        tripStateMachine.sendMessage(Commands.COMMAND_CAR_CHANGE, vehicleStatusNotify);
                        break;
                    case OrderStatusNotify:
                        waitOrderCreated();
                        OrderStatusNotify orderStatusNotify = gson.fromJson(jsonStr, OrderStatusNotify.class);
                        tripStateMachine.sendMessage(Commands.COMMAND_ORDER_CHANGE, orderStatusNotify);
                        break;

                    case PaymentStatusNotify:
                        waitOrderCreated();
                        PaymentStatusNotify paymentStatusNotify = gson.fromJson(jsonStr, PaymentStatusNotify.class);
                        tripStateMachine.sendMessage(Commands.COMMAND_PAYMENT_CHANGE, paymentStatusNotify);
                        break;
                }
            }
        });
    }

    private void waitOrderCreated() {
        if (startTripBlock != null) {
            startTripBlock.get();
        }
    }

    boolean hasRefreshedOrder = false;

    public void refreshOrder(boolean first) {
        if (!first && !hasRefreshedOrder) {
            return;
        }
        hasRefreshedOrder = true;
        BizApiRepository.INSTANCE.recentOrder()
                .subscribeOn(TScheduler.INSTANCE.io())
                .subscribe(new CommonObserver<>() {
                    @Override
                    public void onSuccess(@Nullable OrderDetailResponse detailResponse) {
                        super.onSuccess(detailResponse);
                        Order order = detailResponse.toOrder();
                        Order oldOrder = BizData.getInstance().getOrder(order.getId());
                        if (oldOrder != null) {
                            if (oldOrder.getEstimateRouter() != null) {
                                order.setEstimateRouter(oldOrder.getEstimateRouter());
                            }
                            if (oldOrder.getPickupRouter() != null) {
                                order.setPickupRouter(oldOrder.getPickupRouter());
                            }
                        }

                        BizData.getInstance().saveOrder(order);
                        TripState tripState = TripState.valueOf(order.getDispatchStatus());
                        LOG.e(TAG, "refreshOrder %s", tripState);

                        OrderState orderState = OrderState.valueOf(order.getOrderStatus());
                        if (orderState == OrderState.In_Progress) {
                            if (tripState.getCode() > TripState.Idle.getCode() && tripState.getCode() < TripState.Completed.getCode()) {
                                tripStateMachine.setQueueBean(detailResponse.getWaitingQueueBean());
                                tripStateMachine.setOrder(order);
                                BizData.getInstance().requestSite(new BizData.SingleSiteChangeListener() {
                                    @Override
                                    public void onLoaded(Site... site) {
                                        Site pickUpSite = site[0];
                                        Site dropOffSite = site[1];
                                        tripStateMachine.setPickUpSite(pickUpSite);
                                        tripStateMachine.setDropOffSite(dropOffSite);
                                    }
                                }, detailResponse.pickupPoiId, detailResponse.dropoffPoiId);

                                transferTo(tripState);
                            }
                        } else if (tripStateMachine.inProgress()) {
                            tripStateMachine.resetMachine();
                            transferTo(TripState.Idle);
                        }
                    }
                });

    }

    public void startTrip(Site pickUpSite, Site dropOffSite, VehicleModel vehicleModel, CommonObserver<OrderResponse> observer) {
        startTripBlock = new BlockingObject<>();
        tripStateMachine.setPendingCountDown(null);
        BizApiRepository.INSTANCE.createOrder(pickUpSite, dropOffSite, vehicleModel.getId())
                .subscribeOn(TScheduler.INSTANCE.io())
                .subscribe(new CommonObserver<OrderResponse>() {
                    @Override
                    public void onSuccess(@Nullable OrderResponse response) {
                        Order order = response.toOrder();
                        order.setPickUpId(pickUpSite.getId());
                        order.setDropOffId(dropOffSite.getId());
                        order.setVehicleModelId(vehicleModel.getId());
                        tripStateMachine.setPickUpSite(pickUpSite);
                        tripStateMachine.setDropOffSite(dropOffSite);
                        tripStateMachine.setOrder(order);
                        tripStateMachine.setQueueBean(response.waitingQueue);
                        transferToFront(TripState.Pending);
                        observer.onSuccess(response);
                        startTripBlock.set(Boolean.TRUE);


                        PaymentProvider provider = ProviderManager.provide(ARouterConstants.PATH_SERVICE_PAYMENT);
                        if (provider != null) {
                            provider.preloadPaymentInfo();
                        }
                    }

                    @Override
                    public void onFailed(@Nullable String msg) {
                        observer.onFailed(msg);
                        startTripBlock.set(Boolean.TRUE);
                    }
                });


    }

    public void cancelTrip(CommonObserver<CancelOrderResponse> observer) {
        BizApiRepository.INSTANCE.cancelOrder(tripStateMachine.getOrder().getId(), "The trip was cancelled by the user")
                .subscribeOn(TScheduler.INSTANCE.io())
                .subscribe(new CommonObserver<CancelOrderResponse>() {
                    @Override
                    public void onSuccess(@Nullable CancelOrderResponse cancelOrderResponse) {
                        super.onSuccess(cancelOrderResponse);
                        Order order = BizData.getInstance().getOrder(tripStateMachine.getOrder().getId());
                        order.setMemo(cancelOrderResponse.memo);
                        BizData.getInstance().saveOrder(order);
                        resetMachine();
                        observer.onSuccess(cancelOrderResponse);
                    }

                    @Override
                    public void onFailed(@Nullable String msg) {
                        super.onFailed(msg);
                        Order order = BizData.getInstance().getOrder(tripStateMachine.getOrder().getId());
                        order.setMemo(msg);
                        BizData.getInstance().saveOrder(order);
                        observer.onFailed(msg);
                        resetMachine();
                    }
                });
    }

    private void resetMachine() {
        tripStateMachine.resetMachine();
        transferTo(TripState.Idle);
    }

    public void pay() {
        transferTo(TripState.Completed);
    }

    @Override
    public void onStateChanged(int from, int to) {
        this.oldState = from;
        this.currentState = to;
        if (to == BlueGoPushClient.STATE_CONNECTED) {
            if (BizData.getInstance().siteLoaded()) {
                refreshOrder(false);
            }
        }
        for (PushStateListener listener : pushStateListener) {
            listener.onStateChanged(from, to);
        }
    }

    public void requestPayStatus(String orderId) {
        PaymentProvider paymentProvider =
                ProviderManager.provide(ARouterConstants.PATH_SERVICE_PAYMENT);
        paymentProvider.paymentStatus(orderId, new PaymentProvider.PayStatusListener() {
            @Override
            public void result(PayStatus method) {
                tripStateMachine.sendMessage(Commands.COMMAND_PAYMENT_CHANGE_MANUAL, method);
            }
        });
    }

    public void toUi(PayStatus status) {
        toPayStatusUI(status.orderId, status.paymentStatus, status.memo, null);
    }

    private void toPayStatusUI(String orderId, String paymentStatus, String memo, String failureType) {
        ThreadUtils.runOnUiThread(() -> {
            Bundle args = new Bundle();
            args.putString(TripDetailsActivity.EXTRA_ORDER_ID, orderId);
            Activity lastActivity = BaseActivity.getLastActivity();
            if (TextUtils.equals(paymentStatus, TripStateMachine.Cancelled)) {
                if (tripStateMachine.getOrder() != null && TextUtils.equals(tripStateMachine.getOrder().getId(), orderId)) {
                    args.putString(FinishedActivity.EXTRA_PAY_STATUS, paymentStatus);
                    args.putString(FinishedActivity.EXTRA_PAY_MEMO, lastActivity.getString(R.string.biz_order_payment_cancelled));
                    ARouterUtils.navigation(lastActivity, ARouterConstants.PATH_ACTIVITY_FINISH, args);
                }
                clearPayingStatus(orderId);
            } else if (TextUtils.equals(paymentStatus, TripStateMachine.Paid_Failure)) {
                if (tripStateMachine.getOrder() != null && TextUtils.equals(tripStateMachine.getOrder().getId(), orderId)) {
                    args.putString(FinishedActivity.EXTRA_PAY_STATUS, paymentStatus);
                    args.putString(FinishedActivity.EXTRA_PAY_MEMO, memo);
                    args.putString(FinishedActivity.EXTRA_PAY_FAILURE_TYPE, failureType);
                    ARouterUtils.navigation(lastActivity, ARouterConstants.PATH_ACTIVITY_FINISH, args);
                }
                clearPayingStatus(orderId);
            } else if (TextUtils.equals(paymentStatus, TripStateMachine.Paid_Success)) {
                args.putString(FinishedActivity.EXTRA_PAY_STATUS, paymentStatus);
                ARouterUtils.navigation(lastActivity, ARouterConstants.PATH_ACTIVITY_COMPLETE, args);
                clearPayingStatus(orderId);
            } else if (TextUtils.equals(paymentStatus, TripStateMachine.Paid_In_Progress)) {
                args.putString(FinishedActivity.EXTRA_PAY_STATUS, paymentStatus);
                ARouterUtils.navigation(lastActivity, ARouterConstants.PATH_ACTIVITY_FINISH, args);
            }
        });
    }

    private void clearPayingStatus(String orderId) {
        PaymentProvider provider = ProviderManager.provide(ARouterConstants.PATH_SERVICE_PAYMENT);
        if (provider == null) {
            return;
        }
        provider.clearPaying(orderId);
    }

    public void toUi(PaymentStatusNotify notify) {
        LOG.i(TAG, "toUi PaymentStatusNotify memo: %s ", GsonManager.GsonString(notify));
        if (notify == null) {
            return;
        }
        toPayStatusUI(notify.orderId, notify.paymentStatus, notify.memo, notify.failureType);
    }

    public void toUi(Order order, TripState tripState, String... reasons) {
        final String orderId;
        if (order != null) {
            orderId = order.getId();
        } else {
            orderId = "";
        }
        ThreadUtils.runOnUiThread(() -> {
            Bundle args = new Bundle();
            args.putString(TripDetailsActivity.EXTRA_ORDER_ID, orderId);
            switch (tripState) {
                case Pending:
                    ARouterUtils.navigation(BaseActivity.getLastActivity(), ARouterConstants.PATH_ACTIVITY_PENDING, args);
                    break;
                case Dispatched:
                case Arriving:
                    ARouterUtils.navigation(BaseActivity.getLastActivity(), ARouterConstants.PATH_ACTIVITY_ARRIVING, args);
                    break;
                case Arrived:
                    ARouterUtils.navigation(BaseActivity.getLastActivity(), ARouterConstants.PATH_ACTIVITY_ARRIVED, args);
                    break;
                case Charging:
                    ARouterUtils.navigation(BaseActivity.getLastActivity(), ARouterConstants.PATH_ACTIVITY_CHARGING, args);
                    break;
                case Finished:
                    ARouterUtils.navigation(BaseActivity.getLastActivity(), ARouterConstants.PATH_ACTIVITY_FINISH, args);
                    break;
                case Completed:
                    ARouterUtils.navigation(BaseActivity.getLastActivity(), ARouterConstants.PATH_ACTIVITY_COMPLETE, args);
                    break;
                case Cancelled:
                    DialogCreator.showConfirmDialog(BaseActivity.getLastActivity(), reasons[0], new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ARouterUtils.navigation(BaseActivity.getLastActivity(), ARouterConstants.PATH_ACTIVITY_V2_MAIN);
                        }
                    });
                    break;
                case Manual_Cancelled:
                    ARouterUtils.navigation(BaseActivity.getLastActivity(), ARouterConstants.PATH_ACTIVITY_CANCEL, args);
                    break;
            }
        });
    }

    public void toPaySuccessUi(PaymentStatusNotify notify) {
        if (notify == null) {
            return;
        }
        ThreadUtils.runOnUiThread(() -> {
            Activity lastActivity = BaseActivity.getLastActivity();
            if (lastActivity instanceof CompleteActivity) {
                if (TextUtils.equals(notify.paymentStatus, TripStateMachine.Paid_Success)) {
                    CompleteActivity activity = (CompleteActivity) lastActivity;
                    activity.hideLoading();
                    activity.showToast(activity.getString(R.string.biz_order_payment_success), true);
                }
            }
        });
    }


    private static class Holder {
        private static TripModel instance = new TripModel();
    }

    public static TripModel getInstance() {
        return Holder.instance;
    }

    /**
     * this version only have one trip at same time
     */
    private TripStateMachine tripStateMachine;


    public void setOrder(Order order) {
        tripStateMachine.sendMessage(Commands.COMMAND_ORDER_CHANGE, order);
    }

    private boolean canCancel() {
        TripStateMachine.BaseState currentTrip = tripStateMachine.getCurrentTrip();
        switch (currentTrip.tripState) {
            case Idle:
            case Completed:
            case Charging:
                LOG.e(TAG, "Cannot cancel, current State %s", currentTrip.tripState);
                return false;
        }
        return true;
    }


    public void checkStateMachine() {
        if (tripStateMachine == null) {
            LOG.e(new Throwable(), "tripStateMachine Cannot null ");
        }
    }

    public void registerOnStateChangeListener(OnStateChangeListener stateChangeListener) {
        this.onStateChangeListeners.add(stateChangeListener);
        tripStateMachine.registerOnStateChangeListener(stateChangeListener);
    }

    public void unregisterOnStateChangeListener(OnStateChangeListener stateChangeListener) {
        this.onStateChangeListeners.remove(stateChangeListener);
        tripStateMachine.unregisterOnStateChangeListener(stateChangeListener);
    }

    public void registerPushStateListener(PushStateListener listener) {
        this.pushStateListener.add(listener);
        listener.onStateChanged(oldState, currentState);
    }

    public void unregisterPushStateListener(PushStateListener listener) {
        this.pushStateListener.remove(listener);
    }

    public TripStateMachine getTripStateMachine() {
        return tripStateMachine;
    }
}
