package ai.txai.payment.request;

import com.google.gson.annotations.SerializedName;

import ai.txai.common.log.LOG;
import ai.txai.payment.utils.BankUtils;

/**
 * Time: 10/06/2022
 * Author Hay
 */
public class BankCardTokenBean {
    @SerializedName("pay_type")
    public int payType = 1;

    @SerializedName("card_token")
    public String cardToken;

    @SerializedName("cvv")
    public String cvv;


    @SerializedName("email")
    public String email;

    public BankCardTokenBean() {
    }

    private BankCardTokenBean(Builder builder) {
        payType = builder.payType;
        cardToken = builder.cardToken;
        cvv = builder.cvv;
        email = builder.email;
    }


    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int payType;
        private String cardToken;
        private String cvv;
        private String email;

        private Builder() {
        }

        public Builder payType(int payType) {
            this.payType = payType;
            return this;
        }

        public Builder cardToken(String cardToken) {
            this.cardToken = cardToken;
            return this;
        }

        public Builder cvv(String cvv) {
            this.cvv = BankUtils.encryptAES(cvv);
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public BankCardTokenBean build() {
            return new BankCardTokenBean(this);
        }
    }
}
