package ai.txai.commonbiz.bean;

import com.google.gson.annotations.SerializedName;

/**
 * Time: 15/03/2022
 * Author Hay
 */
public class CancelOrderResponse {

    @SerializedName("order_id")
    public String orderId;
    @SerializedName("order_status")
    public String orderStatus;
    @SerializedName("created_at")
    public String createdAt;
    @SerializedName("finished_at")
    public String finishedAt;
    @SerializedName("memo")
    public String memo;
}
