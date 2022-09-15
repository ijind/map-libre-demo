package ai.txai.commonbiz.bean;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import ai.txai.database.order.Order;

/**
 * Time: 15/03/2022
 * Author Hay
 */
public class OrderIntroResponse {
    @SerializedName("more")
    public boolean more;
    @SerializedName("order_info_list")
    public List<OrderBean> orderInfoList;

    public static class OrderBean {
        @SerializedName("order_id")
        public String orderId;
        @SerializedName("order_status")
        public String orderStatus;
        @SerializedName("created_at")
        public long createdAt;
        @SerializedName("finished_at")
        public long finishedAt;
        @SerializedName("vehicle_no")
        public String vehicleNo;
        @SerializedName("vehicle_type_id")
        public String vehicleTypeId;
        @SerializedName("pickup_poi_id")
        public String pickupPoiId;
        @SerializedName("dropoff_poi_id")
        public String dropoffPoiId;
        @SerializedName("order_fare")
        public double orderFare;
        @SerializedName("due_fare")
        public double dueFare;
        @SerializedName("discount_fare")
        public double discountFare;
        @SerializedName("pay_order_status")
        public String payOrderStatus;
        @SerializedName("dispatch_order_status")
        public String dispatchOrderStatus;

        public Order toOrder() {
            Order order = new Order();
            order.setId(orderId);
            order.setOrderStatus(orderStatus);
            order.setCreateTime(createdAt);
            order.setUpdateTime(finishedAt);
            order.setPickUpId(pickupPoiId);
            order.setDropOffId(dropoffPoiId);
            order.setVehicleNo(vehicleNo);
            order.setVehicleModelId(vehicleTypeId);
            order.setOrderFare(orderFare);
            order.setDueFare(dueFare);
            order.setDiscountFare(discountFare);
            order.setPayStatus(payOrderStatus);
            order.setDispatchStatus(dispatchOrderStatus);
            return order;
        }

    }

    public List<Order> toOrderList() {
        List<Order> orderList = new ArrayList<>();
        for (int i = 0; i < orderInfoList.size(); i++) {
            orderList.add(orderInfoList.get(i).toOrder());
        }
        return orderList;
    }
}
