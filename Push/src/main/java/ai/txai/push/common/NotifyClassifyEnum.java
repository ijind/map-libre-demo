package ai.txai.push.common;

import java.util.ArrayList;
import java.util.List;

/**
 * 推送通知状态
 * @author <a href="{+}jiaming.gong@ctechm.com+"></a>
 * @version 1.0.0 2022/3/2
 */
public enum NotifyClassifyEnum {
    DispatchWaitingNotify (1, "DispatchWaitingNotify"),
    DispatchStatusNotify (2, "DispatchStatusNotify"),
    OrderStatusNotify (3, "OrderStatusNotify"),
    VehicleStatusNotify (4, "VehicleStatusNotify"),
    DispatchTripNotify (5, "DispatchTripNotify"),
    LoginStatusNotify (6, "LoginStatusNotify"),
    PaymentStatusNotify (7, "PaymentStatusNotify"),
    ;

    private final int code;
    private final String message;

    NotifyClassifyEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static List<Integer> codes() {
        List<Integer> codes = new ArrayList<>();
        for (NotifyClassifyEnum item : NotifyClassifyEnum.values()) {
            codes.add(item.getCode());
        }
        return codes;
    }


    public static NotifyClassifyEnum fromCode(int code) {
        for (NotifyClassifyEnum item : NotifyClassifyEnum.values()) {
            if (code == item.getCode()) {
                return item;
            }
        }
        return null;
    }
}
