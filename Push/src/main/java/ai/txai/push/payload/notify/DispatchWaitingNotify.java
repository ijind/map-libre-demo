package ai.txai.push.payload.notify;

import com.google.gson.annotations.SerializedName;

/**
 * Time: 15/03/2022
 * Author Hay
 */
public class DispatchWaitingNotify {
    @SerializedName("orderId")
    public String orderId;
    @SerializedName("pickupPoiId")
    public String pickupPoiId;
    @SerializedName("vehicleTypeId")
    public String vehicleTypeId;
    @SerializedName("estimateWaitingTime")
    public int estimateWaitingTime;
    @SerializedName("waitingInx")
    public int waitingInx;
    @SerializedName("waitingCnt")
    public int waitingCnt;
}
