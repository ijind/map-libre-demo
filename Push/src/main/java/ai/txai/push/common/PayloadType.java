package ai.txai.push.common;


import ai.txai.push.payload.AbstractMessagePayload;
import ai.txai.push.payload.AckPayload;
import ai.txai.push.payload.ConnAuthPayload;
import ai.txai.push.payload.ConnAuthRespPayload;
import ai.txai.push.payload.HeartBeatPayload;
import ai.txai.push.payload.PushNotifyPayload;

public enum PayloadType {
    HeartBeat((short) 10000, HeartBeatPayload.class),
    ConnAuth((short) 20000, ConnAuthPayload.class),
    ConnAuthResp((short) 20001, ConnAuthRespPayload.class),
    PushNotify((short) 30000, PushNotifyPayload.class),
    MessageACK((short) 30001, AckPayload.class),
    ;
    private final short code;
    private final Class<? extends AbstractMessagePayload> cls;

    PayloadType(short code, Class<? extends AbstractMessagePayload> cls) {
        this.code = code;
        this.cls = cls;
    }

    /**
     * 根据clazz获取code
     *
     * @param cls
     * @return
     */
    public static short getCode(Class<? extends AbstractMessagePayload> cls) {
        PayloadType[] values = PayloadType.values();
        for (PayloadType v : values) {
            if (cls.equals(v.cls)) {
                return v.code;
            }
        }
        return 0;
    }

    /**
     * 根据code获得枚举
     *
     * @param code
     * @return
     * PayloadType
     */
    public static PayloadType fromCode(short code) {
        PayloadType[] values = PayloadType.values();
        for (PayloadType v : values) {
            if (code == v.code) {
                return v;
            }
        }
        return null;
    }


    public short getCode() {
        return this.code;
    }

    public Class<?> getCls() {
        return this.cls;
    }
}
