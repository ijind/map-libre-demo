package ai.txai.commonbiz.bean;

import com.google.gson.annotations.SerializedName;

public class MileageBean {
    @SerializedName("auto")
    public double auto;
    @SerializedName("manual")
    public double manual;
    @SerializedName("all")
    public double all;
}