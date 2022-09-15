package ai.txai.push.payload.notify;

import com.google.gson.annotations.SerializedName;

/**
 * Time: 15/03/2022
 * Author Hay
 */
public class VehicleStatusNotify {

    @SerializedName("orderId")
    public String orderId;
    @SerializedName("vehicleNo")
    public String vehicleNo;
    @SerializedName("vehicleStatus")
    public int vehicleStatus;
    @SerializedName("remainingTripInfo")
    public RemainingTripInfoBean remainingTripInfo;
    @SerializedName("vehicleSegment")
    public VehicleSegmentBean vehicleSegment;
    @SerializedName("orderFare")
    public double orderFare;
    @SerializedName("fareInfo")
    public FareInfoBean fareInfo;

    public static class RemainingTripInfoBean {
        @SerializedName("remainingEta")
        public int remainingEta;
        @SerializedName("remainingEmt")
        public double remainingEmt;
    }

    public static class VehicleSegmentBean {
        @SerializedName("latitude")
        public double latitude;
        @SerializedName("longitude")
        public double longitude;
    }
}
