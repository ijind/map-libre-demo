package ai.txai.commonbiz.bean;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import ai.txai.database.enums.TripState;
import ai.txai.database.location.Point;
import ai.txai.database.order.Order;

/**
 * Time: 15/03/2022
 * Author Hay
 */
public class OrderDetailResponse {
    @SerializedName("order_id")
    public String orderId;
    @SerializedName("user_id")
    public String userId;
    @SerializedName("nick_name")
    public String nickName;
    @SerializedName("contact_no")
    public String contactNo;
    @SerializedName("order_channel")
    public String orderChannel;
    @SerializedName("order_status")
    public String orderStatus;
    @SerializedName("memo")
    public String memo;
    @SerializedName("created_at")
    public long createdAt;
    @SerializedName("finished_at")
    public long finishedAt;
    @SerializedName("vehicle_no")
    public String vehicleNo;
    @SerializedName("vehicle_plate_no")
    public String vehiclePlateNo;
    @SerializedName("vehicle_type_id")
    public String vehicleTypeId;
    @SerializedName("vehicle_type_name")
    public String vehicleTypeName;
    @SerializedName("pickup_poi_id")
    public String pickupPoiId;
    @SerializedName("pickup_poi_name")
    public String pickupPoiName;
    @SerializedName("dropoff_poi_id")
    public String dropoffPoiId;
    @SerializedName("dropoff_poi_name")
    public String dropoffPoiName;
    @SerializedName("duration")
    public int duration;
    @SerializedName("mileage")
    public double mileage;
    @SerializedName("order_fare")
    public double orderFare;
    @SerializedName("due_fare")
    public double dueFare;
    @SerializedName("discount_fare")
    public double discountFare;
    @SerializedName("fare_info")
    public FareInfoBean fareInfo;
    @SerializedName("pay_order_info")
    public PayOrderInfoBean payOrderInfo;
    @SerializedName("dispatch_order_list")
    public List<DispatchOrderListBean> dispatchOrderList;
    @SerializedName("dispatch_track_info")
    public DispatchTrackInfoBean dispatchTrackInfo;

    public static class DispatchOrderListBean {
        @SerializedName("order_id")
        public String orderId;
        @SerializedName("dispatch_order_status")
        public String dispatchOrderStatus;
        @SerializedName("dispatched_at")
        public long dispatchedAt;
        @SerializedName("vehicle_type_id")
        public String vehicleTypeId;
        @SerializedName("provider_id")
        public String providerId;
        @SerializedName("vehicle_no")
        public String vehicleNo;
        @SerializedName("trip_info_payload")
        public TripInfoPayloadBean tripInfoPayload;
        @SerializedName("pickup_trip_info_payload")
        public TripInfoPayloadBean pickupTripInfoPayload;
        @SerializedName("waiting_queue")
        public WaitingQueueBean waitingQueueBean;
        @SerializedName("estimate_trip_info")
        public EstimateTripInfoBean estimateTripInfo;
        @SerializedName("pickup_estimate_trip_info")
        public EstimateTripInfoBean pickupEstimateTripInfo;


        public static class TripInfoPayloadBean {
            @SerializedName("duration")
            public MileageBean duration;
            @SerializedName("mileage")
            public MileageBean mileage;
        }

        public static class EstimateTripInfoBean {
            @SerializedName("eta")
            public int eta;
            @SerializedName("emt")
            public double emt;
            @SerializedName("segments")
            public List<SegmentsBean> segments;
        }
    }


    public Order toOrder() {
        Order order = new Order();
        order.setId(orderId);
        order.setPickUpId(pickupPoiId);
        order.setDropOffId(dropoffPoiId);
        order.setVehicleNo(vehicleNo);
        order.setCreateTime(createdAt);
        order.setVehicleModelId(vehicleTypeId);
        order.setOrderStatus(orderStatus);
        order.setOrderFare(orderFare);
        order.setDueFare(dueFare);
        order.setDiscountFare(discountFare);
        order.setOrderStatus(orderStatus);
        if (payOrderInfo != null) {
            order.setPayStatus(payOrderInfo.payOrderStatus);
            order.setPayOrderInfo(payOrderInfo.toDBPayOrderInfo());
        }
        if (fareInfo != null) {
            order.setFareInfo(fareInfo.toDBFareInfo());
        }
        order.setAllDistance(mileage);

        order.setUpdateTime(finishedAt);
        TripState currentState = TripState.Idle;
        for (int i = 0; i < dispatchOrderList.size(); i++) {
            DispatchOrderListBean dispatchedList = dispatchOrderList.get(i);
            TripState tripState = TripState.valueOf(dispatchedList.dispatchOrderStatus);
            if (tripState.getCode() > currentState.getCode()) {
                currentState = tripState;
            }
            if (tripState == TripState.Dispatched) {
                if (TextUtils.isEmpty(vehicleNo)) {
                    order.setVehicleNo(dispatchedList.vehicleNo);
                }
                if (finishedAt == 0) {
                    order.setUpdateTime(dispatchedList.dispatchedAt);
                }
                if (dispatchedList.estimateTripInfo != null && dispatchedList.estimateTripInfo.segments != null) {
                    order.setEstimateRouter(toPoints(dispatchedList.estimateTripInfo.segments));
                }
                if (dispatchedList.pickupEstimateTripInfo != null && dispatchedList.pickupEstimateTripInfo.segments != null) {
                    order.setPickupRouter(toPoints(dispatchedList.pickupEstimateTripInfo.segments));
                }
            } else if (tripState == TripState.Arrived) {
                if (finishedAt == 0) {
                    order.setUpdateTime(dispatchedList.dispatchedAt);
                }
            } else if (tripState == TripState.Finished) {
                order.setAutoDuration(dispatchedList.tripInfoPayload.duration.auto);
                order.setManualDuration(dispatchedList.tripInfoPayload.duration.manual);
                order.setAutoDistance(dispatchedList.tripInfoPayload.mileage.auto);
                order.setManualDistance(dispatchedList.tripInfoPayload.mileage.manual);
            }
        }
        order.setDispatchStatus(currentState.name());
        order.setMemo(memo);
        return order;
    }

    public WaitingQueueBean getWaitingQueueBean() {
        TripState currentState = TripState.Idle;
        for (int i = 0; i < dispatchOrderList.size(); i++) {
            DispatchOrderListBean dispatchedList = dispatchOrderList.get(i);
            TripState tripState = TripState.valueOf(dispatchedList.dispatchOrderStatus);
            if (tripState.getCode() > currentState.getCode()) {
                currentState = tripState;
            }
        }

        if (currentState == TripState.Pending) {
            return dispatchOrderList.get(0).waitingQueueBean;
        }
        return null;
    }

    private static List<Point> toPoints(List<SegmentsBean> segments) {
        List<Point> points = new ArrayList<>();
        for (int i = 0; i < segments.size(); i++) {
            SegmentsBean segmentsBean = segments.get(i);
            points.add(new Point(segmentsBean.longitude, segmentsBean.latitude));
        }
        return points;
    }

    public static class DispatchTrackInfoBean {
        @SerializedName("order_id")
        public String orderId;
        @SerializedName("vehicle_no")
        public String vehicleNo;
        @SerializedName("pickup_poi_id")
        public String pickupPoiId;
        @SerializedName("dropoff_poi_id")
        public String dropoffPoiId;
        @SerializedName("track_type")
        public String trackType;
        @SerializedName("vehicle_segments")
        public List<SegmentsBean> vehicleSegments;
    }
}
