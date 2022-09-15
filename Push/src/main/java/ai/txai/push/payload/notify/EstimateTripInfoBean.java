package ai.txai.push.payload.notify;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class EstimateTripInfoBean {
    @SerializedName("emt")
    public double emt;
    @SerializedName("eta")
    public int eta;
    @SerializedName("segments")
    public List<SegmentsBean> segments;


}