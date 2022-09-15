package ai.txai.payment.response;

import com.google.gson.annotations.SerializedName;

public class PayMethodInfo {
    @SerializedName("card_token")
    public String cardToken;
    @SerializedName("brand")
    public String brand;
    @SerializedName("card_type")
    public String cardType;
    @SerializedName("last4")
    public String last4;
    @SerializedName("expired_at")
    public long expiredAt;
    @SerializedName("is_expired")
    public boolean expired;
}