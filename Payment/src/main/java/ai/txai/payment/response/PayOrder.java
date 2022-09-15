package ai.txai.payment.response;

import com.google.gson.annotations.SerializedName;

import ai.txai.common.router.bean.PayStatus;

/**
 * Time: 06/06/2022
 * Author Hay
 */
public class PayOrder {
    @SerializedName("order_id")
    public String orderId;
    @SerializedName("transaction_id")
    public String transactionId;
    @SerializedName("pay_method")
    public String payMethod;
    @SerializedName("payment_status")
    public String paymentStatus;
    @SerializedName("created_at")
    public long createdAt;
    @SerializedName("expired_at")
    public long expiredAt;
    @SerializedName("finished_at")
    public long finishedAt;
    @SerializedName("memo")
    public String memo;
    @SerializedName("inter_action_uri")
    public String interActionUri;


    public PayStatus toPayStatus() {
        PayStatus status = new PayStatus();
        status.paymentStatus = paymentStatus;
        status.transactionId = transactionId;
        status.memo = memo;
        status.orderId = orderId;
        status.finishedAt = finishedAt;
        return status;
    }
}
