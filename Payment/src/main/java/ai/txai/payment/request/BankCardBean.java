package ai.txai.payment.request;

import com.google.gson.annotations.SerializedName;

import ai.txai.payment.utils.BankUtils;

/**
 * Time: 10/06/2022
 * Author Hay
 */
public class BankCardBean {
    @SerializedName("pay_type")
    public int payType = 0;

    @SerializedName("card_no")
    public String cardNo;

    @SerializedName("holder_name")
    public String holderName;

    @SerializedName("cvv")
    public String cvv;

    @SerializedName("exp_year")
    public String exp_year;

    @SerializedName("exp_month")
    public String exp_month;

    @SerializedName("email")
    public String email;

    @SerializedName("save_card")
    public boolean saveCard;

    public BankCardBean(){}

    private BankCardBean(Builder builder) {
        payType = builder.payType;
        cardNo = builder.cardNo;
        holderName = builder.holderName;
        cvv = builder.cvv;
        exp_year = builder.exp_year;
        exp_month = builder.exp_month;
        email = builder.email;
        saveCard = builder.saveCard;
    }


    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int payType;
        private String cardNo;
        private String holderName;
        private String cvv;
        private String exp_year;
        private String exp_month;
        private String email;
        private boolean saveCard;

        private Builder() {
        }

        public Builder payType(int payType) {
            this.payType = payType;
            return this;
        }

        public Builder cardNo(String cardNo) {
            this.cardNo = BankUtils.encryptAES(cardNo);
            return this;
        }

        public Builder holderName(String holderName) {
            this.holderName = BankUtils.encryptAES(holderName);
            return this;
        }

        public Builder cvv(String cvv) {
            this.cvv = BankUtils.encryptAES(cvv);
            return this;
        }

        public Builder exp_year(String exp_year) {
            this.exp_year = exp_year;
            return this;
        }

        public Builder exp_month(String exp_month) {
            this.exp_month = exp_month;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder saveCard(boolean saveCard) {
            this.saveCard = saveCard;
            return this;
        }

        public BankCardBean build() {
            return new BankCardBean(this);
        }
    }
}
