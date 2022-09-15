package ai.txai.push.util;


import com.blankj.utilcode.util.DeviceUtils;

import ai.txai.common.network.NetUtils;
import ai.txai.database.user.User;
import ai.txai.database.utils.CommonData;
import ai.txai.push.payload.ClientInfoPayload;

/**
 * Time: 28/03/2022
 * Author Hay
 */
public class PushUtils {

    public static ClientInfoPayload defaultClientInfo() {
        ClientInfoPayload payload = new ClientInfoPayload();
        payload.setOsVersion(String.valueOf(DeviceUtils.getSDKVersionCode()));
        payload.setOs("Android");
        payload.setAppVersion(ai.txai.common.utils.DeviceUtils.INSTANCE.getVersion());
        User user = CommonData.getInstance().currentUser();
        payload.setCountryCode(user.getIsoCode());
        payload.setModel(DeviceUtils.getModel());
        payload.setNetworkType(NetUtils.INSTANCE.getNetworkType());
        payload.setLocal(ai.txai.common.utils.DeviceUtils.INSTANCE.getSystemLanguage());
        payload.setClientSId(ai.txai.common.utils.DeviceUtils.INSTANCE.getDeviceId());
        return payload;
    }

    public static String fromCode(int code) {
        switch (code) {
            case 0:
                return "idle";
            case 1:
                return "connecting";
            case 2:
                return "authing";
            case 3:
                return "connected";
            case 4:
                return "connect failed";
            case 5:
                return "reconnecting";
        }
        return "unknown";
    }
}
