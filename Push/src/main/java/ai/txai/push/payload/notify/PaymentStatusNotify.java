package ai.txai.push.payload.notify;

import com.google.gson.annotations.SerializedName;

/**
 * Time: 10/06/2022
 * Author Hay
 */
public class PaymentStatusNotify {
    @SerializedName("orderId")
    public String orderId;
    @SerializedName("paymentOrderId")
    public String paymentOrderId;
    @SerializedName("transactionId")
    public String transactionId;
    @SerializedName("payMethod")
    public String payMethod;
    @SerializedName("paymentStatus")
    public String paymentStatus;
    @SerializedName("failureType")
    public String failureType;
    @SerializedName("finishedAt")
    public long finishedAt;
    @SerializedName("memo")
    public String memo;
}
