package ai.txai.commonbiz.bean;

import com.google.gson.annotations.SerializedName;

public class PayOrderInfoBean {
    @SerializedName("order_id")
    public String orderId;
    @SerializedName("pay_order_status")
    public String payOrderStatus;
    @SerializedName("pay_method")
    public String payMethod;
    @SerializedName("memo")
    public String memo;
    @SerializedName("paid_fare")
    public double paidFare;
    @SerializedName("paid_at")
    public long paidAt;

    public ai.txai.database.order.bean.PayOrderInfoBean toDBPayOrderInfo() {
        ai.txai.database.order.bean.PayOrderInfoBean payOrderInfoBean = new ai.txai.database.order.bean.PayOrderInfoBean();
        payOrderInfoBean.payOrderStatus = payOrderStatus;
        payOrderInfoBean.payMethod = payMethod;
        payOrderInfoBean.memo = memo;
        payOrderInfoBean.paidFare = paidFare;
        payOrderInfoBean.paidAt = paidAt;
        return payOrderInfoBean;
    }
}