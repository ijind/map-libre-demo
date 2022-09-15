package ai.txai.commonbiz.onetrip;

/**
 * Time: 08/03/2022
 * Author Hay
 */
public class Commands {

    /**
     * 修改状态
     */
    public final static int COMMAND_TRANSFER_TO = 10001;

    /**
     * 排队变更
     */
    public final static int COMMAND_QUEUE_CHANGE = 10002;

    /**
     * 订单履约变更
     */
    public final static int COMMAND_ORDER_DISPATCH = 10003;

    /**
     * 路线变更
     */
    public final static int COMMAND_ROUTE_CHANGE = 10004;

    /**
     * 车辆变更
     */
    public final static int COMMAND_CAR_CHANGE = 10005;

    /**
     * 订单变更
     */
    public final static int COMMAND_ORDER_CHANGE = 10006;

    /**
     * 支付状态变化
     */
    public final static int COMMAND_PAYMENT_CHANGE = 10007;

    public final static int COMMAND_PAYMENT_CHANGE_MANUAL = 10008;

}
