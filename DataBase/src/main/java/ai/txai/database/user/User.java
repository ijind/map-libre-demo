package ai.txai.database.user;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

/**
 * Time: 2/23/22
 * Author Hay
 */
@Entity
public class User {
    @Id
    private String uid;
    private String nickname;
    private long phoneNumber;
    private int countryCode;
    private String email;
    private String avatar;
    private String apiToken;
    private String pushToken;
    private String updateToken;
    private String isoCode;

    @Generated(hash = 67392962)
    public User(String uid, String nickname, long phoneNumber, int countryCode,
            String email, String avatar, String apiToken, String pushToken,
            String updateToken, String isoCode) {
        this.uid = uid;
        this.nickname = nickname;
        this.phoneNumber = phoneNumber;
        this.countryCode = countryCode;
        this.email = email;
        this.avatar = avatar;
        this.apiToken = apiToken;
        this.pushToken = pushToken;
        this.updateToken = updateToken;
        this.isoCode = isoCode;
    }
    @Generated(hash = 586692638)
    public User() {
    }
    public String getUid() {
        return this.uid;
    }
    public void setUid(String uid) {
        this.uid = uid;
    }
    public String getNickname() {
        return this.nickname;
    }
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    public long getPhoneNumber() {
        return this.phoneNumber;
    }
    public void setPhoneNumber(long phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public int getCountryCode() {
        return this.countryCode;
    }
    public void setCountryCode(int countryCode) {
        this.countryCode = countryCode;
    }
    public String getEmail() {
        return this.email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getAvatar() {
        return this.avatar;
    }
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
    public String getApiToken() {
        return this.apiToken;
    }
    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }
    public String getPushToken() {
        return this.pushToken;
    }
    public void setPushToken(String pushToken) {
        this.pushToken = pushToken;
    }
    public String getUpdateToken() {
        return this.updateToken;
    }
    public void setUpdateToken(String updateToken) {
        this.updateToken = updateToken;
    }
    public String getIsoCode() { return this.isoCode; }
    public void setIsoCode(String isoCode) { this.isoCode = isoCode; }
}
