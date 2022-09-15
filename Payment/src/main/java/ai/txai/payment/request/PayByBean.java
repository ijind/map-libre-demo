package ai.txai.payment.request;

import com.google.gson.annotations.SerializedName;

/**
 * Time: 10/06/2022
 * Author Hay
 */
public class PayByBean {
    @SerializedName("ewallet_code")
    public String ewalletCode;

    public PayByBean(){}

    private PayByBean(Builder builder) {
        ewalletCode = builder.ewalletCode;
    }


    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String ewalletCode;

        private Builder() {
        }

        public Builder ewalletCode(String ewalletCode) {
            this.ewalletCode = ewalletCode;
            return this;
        }

        public PayByBean build() {
            return new PayByBean(this);
        }
    }
}
