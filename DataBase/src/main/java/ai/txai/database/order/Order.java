package ai.txai.database.order;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

import java.util.List;

import ai.txai.database.location.ListPointConverter;
import ai.txai.database.location.Point;
import ai.txai.database.order.bean.FareInfoBean;
import ai.txai.database.order.bean.FareInfoConverter;
import ai.txai.database.order.bean.PayOrderConverter;
import ai.txai.database.order.bean.PayOrderInfoBean;

/**
 * Time: 2/23/22
 * Author Hay
 */
@Entity
public class Order {
    @Id
    private String id;

    private String pickUpId;
    private String dropOffId;

    @Convert(converter = ListPointConverter.class, columnType = String.class)
    private List<Point> estimateRouter;

    @Convert(converter = ListPointConverter.class, columnType = String.class)
    private List<Point> pickupRouter;

    private String orderStatus;// -> OrderState, 订单状态，
    private String payStatus;// -> PayState, 支付状态，
    private String dispatchStatus;// -> TripState， 调度状态

    private long createTime;
    private long updateTime;

    public double autoDistance;
    public double manualDistance;
    public double allDistance;

    public double autoDuration;
    public double manualDuration;
    public double allDuration;

    @Convert(converter = FareInfoConverter.class, columnType = String.class)
    public FareInfoBean fareInfo;
    @Convert(converter = PayOrderConverter.class, columnType = String.class)
    public PayOrderInfoBean payOrderInfo;

    public double orderFare;
    public double dueFare;
    public double discountFare;

    private String vehicleNo;
    private String vehicleModelId;

    private String memo;

    @Generated(hash = 1102404576)
    public Order(String id, String pickUpId, String dropOffId,
            List<Point> estimateRouter, List<Point> pickupRouter,
            String orderStatus, String payStatus, String dispatchStatus,
            long createTime, long updateTime, double autoDistance,
            double manualDistance, double allDistance, double autoDuration,
            double manualDuration, double allDuration, FareInfoBean fareInfo,
            PayOrderInfoBean payOrderInfo, double orderFare, double dueFare,
            double discountFare, String vehicleNo, String vehicleModelId,
            String memo) {
        this.id = id;
        this.pickUpId = pickUpId;
        this.dropOffId = dropOffId;
        this.estimateRouter = estimateRouter;
        this.pickupRouter = pickupRouter;
        this.orderStatus = orderStatus;
        this.payStatus = payStatus;
        this.dispatchStatus = dispatchStatus;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.autoDistance = autoDistance;
        this.manualDistance = manualDistance;
        this.allDistance = allDistance;
        this.autoDuration = autoDuration;
        this.manualDuration = manualDuration;
        this.allDuration = allDuration;
        this.fareInfo = fareInfo;
        this.payOrderInfo = payOrderInfo;
        this.orderFare = orderFare;
        this.dueFare = dueFare;
        this.discountFare = discountFare;
        this.vehicleNo = vehicleNo;
        this.vehicleModelId = vehicleModelId;
        this.memo = memo;
    }

    @Generated(hash = 1105174599)
    public Order() {
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPickUpId() {
        return this.pickUpId;
    }

    public void setPickUpId(String pickUpId) {
        this.pickUpId = pickUpId;
    }

    public String getDropOffId() {
        return this.dropOffId;
    }

    public void setDropOffId(String dropOffId) {
        this.dropOffId = dropOffId;
    }

    public List<Point> getEstimateRouter() {
        return this.estimateRouter;
    }

    public void setEstimateRouter(List<Point> estimateRouter) {
        this.estimateRouter = estimateRouter;
    }

    public List<Point> getPickupRouter() {
        return this.pickupRouter;
    }

    public void setPickupRouter(List<Point> pickupRouter) {
        this.pickupRouter = pickupRouter;
    }

    public String getOrderStatus() {
        return this.orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getPayStatus() {
        return this.payStatus;
    }

    public void setPayStatus(String payStatus) {
        this.payStatus = payStatus;
    }

    public String getDispatchStatus() {
        return this.dispatchStatus;
    }

    public void setDispatchStatus(String dispatchStatus) {
        this.dispatchStatus = dispatchStatus;
    }

    public long getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getUpdateTime() {
        return this.updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public double getAutoDistance() {
        return this.autoDistance;
    }

    public void setAutoDistance(double autoDistance) {
        this.autoDistance = autoDistance;
    }

    public double getManualDistance() {
        return this.manualDistance;
    }

    public void setManualDistance(double manualDistance) {
        this.manualDistance = manualDistance;
    }

    public double getAllDistance() {
        return this.allDistance;
    }

    public void setAllDistance(double allDistance) {
        this.allDistance = allDistance;
    }

    public double getAutoDuration() {
        return this.autoDuration;
    }

    public void setAutoDuration(double autoDuration) {
        this.autoDuration = autoDuration;
    }

    public double getManualDuration() {
        return this.manualDuration;
    }

    public void setManualDuration(double manualDuration) {
        this.manualDuration = manualDuration;
    }

    public double getAllDuration() {
        return this.allDuration;
    }

    public void setAllDuration(double allDuration) {
        this.allDuration = allDuration;
    }

    public FareInfoBean getFareInfo() {
        return this.fareInfo;
    }

    public void setFareInfo(FareInfoBean fareInfo) {
        this.fareInfo = fareInfo;
    }

    public PayOrderInfoBean getPayOrderInfo() {
        return this.payOrderInfo;
    }

    public void setPayOrderInfo(PayOrderInfoBean payOrderInfo) {
        this.payOrderInfo = payOrderInfo;
    }

    public double getOrderFare() {
        return this.orderFare;
    }

    public void setOrderFare(double orderFare) {
        this.orderFare = orderFare;
    }

    public double getDueFare() {
        return this.dueFare;
    }

    public void setDueFare(double dueFare) {
        this.dueFare = dueFare;
    }

    public double getDiscountFare() {
        return this.discountFare;
    }

    public void setDiscountFare(double discountFare) {
        this.discountFare = discountFare;
    }

    public String getVehicleNo() {
        return this.vehicleNo;
    }

    public void setVehicleNo(String vehicleNo) {
        this.vehicleNo = vehicleNo;
    }

    public String getVehicleModelId() {
        return this.vehicleModelId;
    }

    public void setVehicleModelId(String vehicleModelId) {
        this.vehicleModelId = vehicleModelId;
    }

    public String getMemo() {
        return this.memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

}
