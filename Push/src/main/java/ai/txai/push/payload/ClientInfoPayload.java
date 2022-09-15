package ai.txai.push.payload;

import java.util.Map;

public class ClientInfoPayload {
    // 客户端设备ID
    private String clientSId;
    // 客户端App版本
    private String appVersion;
    // 客户端系统版本 11.0
    private String osVersion;
    // 客户端系统标识 iOS/Android
    private String os;
    // 客户端型号
    private String model;
    // 客户端语言
    private String local;
    // 客户端网络类型 WIFI
    private String networkType;
    // 当前用户国家码
    private String countryCode;
    // 客户端其他扩展数据
    private Map<String, Object> extInfo;

    public String getClientSId() {
        return clientSId;
    }

    public void setClientSId(String clientSId) {
        this.clientSId = clientSId;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public String getNetworkType() {
        return networkType;
    }

    public void setNetworkType(String networkType) {
        this.networkType = networkType;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public Map<String, Object> getExtInfo() {
        return extInfo;
    }

    public void setExtInfo(Map<String, Object> extInfo) {
        this.extInfo = extInfo;
    }
}