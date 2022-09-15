package ai.txai.database.enums;

/**
 * Time: 03/03/2022
 * Author Hay
 */
public enum TripState {
    Idle(1),//正常状态
    Pending(3),//等待派车
    Dispatched(4),//派车
    Arriving(5),//车去接人//
    Arrived(6),//车到达上车点
    Charging(7),//车行进过程
    Finished(8),//路程完成
    Completed(9),//支付完成

    Cancelled(10),
    Manual_Cancelled(11),
    ;
    private int code;

    TripState(int code) {
        this.code = code;
    }

    public static TripState fromCode(int code) {
        for (TripState value : TripState.values()) {
            if (value.code == code) {
                return value;
            }
        }
        return Idle;
    }

    public int getCode() {
        return code;
    }
}
