package ai.txai.common.router.bean;

/**
 * Time: 16/06/2022
 * Author Hay
 */
public class PayMethod {
    public int logoRes;
    public String name;
    public boolean isCard;

    private PayMethod(Builder builder) {
        logoRes = builder.logoRes;
        name = builder.name;
        isCard = builder.isCard;
    }


    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int logoRes;
        private String name;
        private boolean isCard;

        private Builder() {
        }

        public Builder logoRes(int logoRes) {
            this.logoRes = logoRes;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder isCard(boolean isCard) {
            this.isCard = isCard;
            return this;
        }

        public PayMethod build() {
            return new PayMethod(this);
        }
    }
}
