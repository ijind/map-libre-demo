package ai.txai.commonbiz.bean;

import com.google.gson.annotations.SerializedName;

/**
 * Time: 15/03/2022
 * Author Hay
 */
public class PayResponse {

    @SerializedName("discount_fare")
    public int discountFare;
    @SerializedName("memo")
    public String memo;
    @SerializedName("order_id")
    public String orderId;
    @SerializedName("paid_fare")
    public int paidFare;
    @SerializedName("pay_channel")
    public String payChannel;
    @SerializedName("pay_created_at")
    public int payCreatedAt;
    @SerializedName("pay_finished_at")
    public int payFinishedAt;
    @SerializedName("pay_order_status")
    public String payOrderStatus;
    @SerializedName("pay_transaction_id")
    public String payTransactionId;
    @SerializedName("payee")
    public String payee;
    @SerializedName("payer")
    public String payer;
}
