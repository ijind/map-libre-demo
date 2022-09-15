package ai.txai.lostfound.bean;

import com.google.gson.annotations.SerializedName;

public class LostItem {
    @SerializedName("created_at")
    public long createdAt;
    @SerializedName("id")
    public String id;
    @SerializedName("summary")
    public String summary;
    @SerializedName("user_id")
    public String userId;
    @SerializedName("status")
    public int status;
}