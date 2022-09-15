package ai.txai.push.payload.notify;

import com.google.gson.annotations.SerializedName;

/**
 * Time: 15/03/2022
 * Author Hay
 */
public class OrderStatusNotify {
    public static final String AUTO_CANCEL = "Auto_Cancelled";
    public static final String ADMIN_CANCEL = "Admin_Cancelled";

    @SerializedName("orderId")
    public String orderId;
    @SerializedName("orderStatus")
    public String orderStatus;
    @SerializedName("finishedAt")
    public long finishedAt;
    @SerializedName("memo")
    public String memo;
    @SerializedName("cancelType")
    public String cancelType;
    @SerializedName("vehicleNo")
    public String vehicleNo;
    @SerializedName("vehicleTypeId")
    public String vehicleTypeId;
    @SerializedName("pickupPoiId")
    public String pickupPoiId;
    @SerializedName("dropoffPoiId")
    public String dropoffPoiId;
    @SerializedName("duration")
    public int duration;
    @SerializedName("mileage")
    public double mileage;
    @SerializedName("orderFare")
    public double orderFare;
    @SerializedName("dueFare")
    public double dueFare;
    @SerializedName("discountFare")
    public double discountFare;
    @SerializedName("fareInfo")
    public FareInfoBean fareInfo;
    @SerializedName("payOrderInfo")
    public PayOrderInfoBean payOrderInfo;



    public static class PayOrderInfoBean {
        @SerializedName("orderId")
        public String orderId;
        @SerializedName("payOrderStatus")
        public String payOrderStatus;
        @SerializedName("paidFare")
        public double paidFare;
        @SerializedName("memo")
        public String memo;
        @SerializedName("paidAt")
        public long paidAt;
        @SerializedName("payMethod")
        public String payMethod;


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
}
