package ai.txai.push.payload.notify;

import com.google.gson.annotations.SerializedName;

/**
 * Time: 15/03/2022
 * Author Hay
 */
public class DispatchStatusNotify {

    @SerializedName("orderId")
    public String orderId;
    @SerializedName("vehicleNo")
    public String vehicleNo;
    @SerializedName("dispatchStatus")
    public String dispatchStatus;
    @SerializedName("dispatchedAt")
    public long dispatchedAt;
    @SerializedName("estimateTripInfo")
    public EstimateTripInfoBean estimateTripInfo;
    @SerializedName("pickupEstimateTripInfo")
    public EstimateTripInfoBean pickupEstimateTripInfo;
    @SerializedName("orderTripInfo")
    public OrderTripInfoBean orderTripInfo;
    @SerializedName("fareInfo")
    public FareInfoBean fareInfo;
    @SerializedName("orderFare")
    public double orderFare;
    @SerializedName("dueFare")
    public double dueFare;
    @SerializedName("discountFare")
    public double discountFare;
    @SerializedName("memo")
    public String memo;


    public static class OrderTripInfoBean {
        @SerializedName("duration")
        public DurationBean duration;
        @SerializedName("mileage")
        public DurationBean mileage;
    }
}
