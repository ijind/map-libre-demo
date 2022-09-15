package ai.txai.commonbiz.bean;

import com.google.gson.annotations.SerializedName;

import ai.txai.database.order.Order;

/**
 * Time: 15/03/2022
 * Author Hay
 */
public class OrderResponse {
    @SerializedName("order_id")
    public String orderId;
    @SerializedName("order_status")
    public String orderStatus;
    @SerializedName("created_at")
    public long createdAt;
    @SerializedName("waiting_queue")
    public WaitingQueueBean waitingQueue;

    public Order toOrder() {
        Order order = new Order();
        order.setId(orderId);
        order.setCreateTime(createdAt);
        order.setOrderStatus(orderStatus);
        return order;
    }
}
