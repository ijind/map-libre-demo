package ai.txai.lostfound.bean;

import com.google.gson.annotations.SerializedName;

/**
 * Time: 10/05/2022
 * Author Hay
 */
public class LostDetailEntry {


    @SerializedName("id")
    public String id;
    @SerializedName("order_id")
    public String orderId;
    @SerializedName("item_type")
    public int itemType;
    @SerializedName("item_type_name")
    public String itemTypeName;
    @SerializedName("calling_code")
    public int callingCode;
    @SerializedName("phone")
    public String phone;
    @SerializedName("email")
    public String email;
    @SerializedName("result")
    public int result;
    @SerializedName("status")
    public int status;
    @SerializedName("summary")
    public String summary;
    @SerializedName("created_at")
    public long createdAt;
    @SerializedName("handle_start")
    public long handleStart;
    @SerializedName("completed_at")
    public long completedAt;
}
