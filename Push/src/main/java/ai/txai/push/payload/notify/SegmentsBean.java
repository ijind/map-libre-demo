package ai.txai.push.payload.notify;

import com.google.gson.annotations.SerializedName;

public class SegmentsBean {
    @SerializedName("latitude")
    public double latitude;
    @SerializedName("longitude")
    public double longitude;
}