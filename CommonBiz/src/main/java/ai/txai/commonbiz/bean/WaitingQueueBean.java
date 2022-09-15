package ai.txai.commonbiz.bean;

import com.google.gson.annotations.SerializedName;

/**
 * Time: 15/03/2022
 * Author Hay
 */
public class WaitingQueueBean {
    @SerializedName("waiting_inx")
    public int waitingInx;
    @SerializedName("waiting_cnt")
    public int waitingCnt;
    @SerializedName("estimate_waiting_time")
    public int estimateWaitingTime;
}
