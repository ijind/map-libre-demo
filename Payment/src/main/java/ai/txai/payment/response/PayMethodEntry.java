package ai.txai.payment.response;

import com.google.gson.annotations.SerializedName;

/**
 * Time: 06/06/2022
 * Author Hay
 */
public class PayMethodEntry {

    @SerializedName("pay_method")
    public String payMethod;
    @SerializedName("pay_method_info")
    public PayMethodInfo payMethodInfo;

    public PayMethodEntry() {}

    private PayMethodEntry(Builder builder) {
        payMethod = builder.payMethod;
        payMethodInfo = builder.payMethodInfo;
    }


    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String payMethod;
        private PayMethodInfo payMethodInfo;

        private Builder() {
        }

        public Builder payMethod(String payMethod) {
            this.payMethod = payMethod;
            return this;
        }

        public Builder payMethodInfo(PayMethodInfo payMethodInfo) {
            this.payMethodInfo = payMethodInfo;
            return this;
        }

        public PayMethodEntry build() {
            return new PayMethodEntry(this);
        }
    }
}
