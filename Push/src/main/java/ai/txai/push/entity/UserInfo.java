package ai.txai.push.entity;


/**
 * @Description UserInfo
 * @Author Jiaming.gong
 * @Date 08/01/2022
 */

public class UserInfo {
    private String userId;
    private String endpoint;
    private String authKey;

    private UserInfo(Builder builder) {
        userId = builder.userId;
        endpoint = builder.endpoint;
        authKey = builder.authKey;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getAuthKey() {
        return authKey;
    }

    public void setAuthKey(String authKey) {
        this.authKey = authKey;
    }

    public static class Builder {
        private String userId;
        private String endpoint;
        private String authKey;

        private Builder() {
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder endpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public Builder authKey(String authKey) {
            this.authKey = authKey;
            return this;
        }

        public UserInfo build() {
            return new UserInfo(this);
        }
    }
}