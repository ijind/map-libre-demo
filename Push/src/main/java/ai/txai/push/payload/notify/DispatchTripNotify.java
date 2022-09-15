package ai.txai.push.payload.notify;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Time: 15/03/2022
 * Author Hay
 */
public class DispatchTripNotify {

    @SerializedName("orderId")
    public String orderId;
    @SerializedName("vehicleNo")
    public String vehicleNo;
    @SerializedName("remainingTripInfo")
    public EstimateTripInfoBean remainingTripInfo;
}
