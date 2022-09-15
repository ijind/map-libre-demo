package ai.txai.commonbiz.bean;

import com.google.gson.annotations.SerializedName;

/**
 * Time: 15/03/2022
 * Author Hay
 */
public class DispatchOrderListBean {
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
}
