package ai.txai.push.payload.notify;

import com.google.gson.annotations.SerializedName;

/**
 * Time: 15/03/2022
 * Author Hay
 */
public class LoginStatusNotify {

    @SerializedName("endpoint")
    public String endpoint;
    @SerializedName("ip")
    public String ip;
    @SerializedName("memo")
    public String memo;
}
