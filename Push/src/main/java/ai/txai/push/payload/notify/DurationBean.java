package ai.txai.push.payload.notify;

import com.google.gson.annotations.SerializedName;

public class DurationBean {
    @SerializedName("auto")
    public double auto;
    @SerializedName("manual")
    public double manual;
    @SerializedName("all")
    public double all;
}