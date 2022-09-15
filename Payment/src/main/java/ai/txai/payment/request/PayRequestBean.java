package ai.txai.payment.request;

import com.google.gson.annotations.SerializedName;

/**
 * Time: 10/06/2022
 * Author Hay
 */
public class PayRequestBean {
    @SerializedName("user_id")
    public String userId;

    @SerializedName("biz_type")
    public String bizType;

    @SerializedName("biz_subject")
    public String bizSubject;

    @SerializedName("order_id")
    public String orderId;
    @SerializedName("expired_time")
    public Long expiredTime;
    @SerializedName("total_amount")
    public double totalAmount;

    @SerializedName("pay_method")
    public String payMethod;

    @SerializedName("pay_method_info")
    public Object payMethodInfo;

    @SerializedName("ext_info")
    public Object extInfo;

    public PayRequestBean() {
    }

    private PayRequestBean(Builder builder) {
        userId = builder.userId;
        bizType = builder.bizType;
        bizSubject = builder.bizSubject;
        orderId = builder.orderId;
        totalAmount = builder.totalAmount;
        payMethod = builder.payMethod;
        payMethodInfo = builder.payMethodInfo;
        extInfo = builder.extInfo;
    }


    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String userId;
        private String bizType;
        private String bizSubject;
        private String orderId;
        private double totalAmount;
        private String payMethod;
        private Object payMethodInfo;
        private Object extInfo;

        private Builder() {
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder bizType(String bizType) {
            this.bizType = bizType;
            return this;
        }

        public Builder bizSubject(String bizSubject) {
            this.bizSubject = bizSubject;
            return this;
        }

        public Builder orderId(String orderId) {
            this.orderId = orderId;
            return this;
        }

        public Builder totalAmount(double totalAmount) {
            this.totalAmount = totalAmount;
            return this;
        }

        public Builder payMethod(String payMethod) {
            this.payMethod = payMethod;
            return this;
        }

        public Builder payMethodInfo(Object payMethodInfo) {
            this.payMethodInfo = payMethodInfo;
            return this;
        }

        public Builder extInfo(Object extInfo) {
            this.extInfo = extInfo;
            return this;
        }

        public PayRequestBean build() {
            return new PayRequestBean(this);
        }
    }
}
