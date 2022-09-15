package ai.txai.database.vehicle;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Time: 18/03/2022
 * Author Hay
 * 车辆固有属性， 比如车牌，编号，颜色，品牌等
 */
@Entity
public class Vehicle {
    @Id
    private String id;

    private String color;
    private String iconUrl;
    private String plateNo;//车牌
    private String vehicleNo;//车辆编号，暂未使用
    private String brand;
    private Integer passengerNum;
    private Boolean demo;
    @Generated(hash = 2047179978)
    public Vehicle(String id, String color, String iconUrl, String plateNo,
            String vehicleNo, String brand, Integer passengerNum, Boolean demo) {
        this.id = id;
        this.color = color;
        this.iconUrl = iconUrl;
        this.plateNo = plateNo;
        this.vehicleNo = vehicleNo;
        this.brand = brand;
        this.passengerNum = passengerNum;
        this.demo = demo;
    }
    @Generated(hash = 2006430483)
    public Vehicle() {
    }
    public String getId() {
        return this.id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getColor() {
        return this.color;
    }
    public void setColor(String color) {
        this.color = color;
    }
    public String getIconUrl() {
        return this.iconUrl;
    }
    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }
    public String getPlateNo() {
        return this.plateNo;
    }
    public void setPlateNo(String plateNo) {
        this.plateNo = plateNo;
    }
    public String getVehicleNo() {
        return this.vehicleNo;
    }
    public void setVehicleNo(String vehicleNo) {
        this.vehicleNo = vehicleNo;
    }
    public String getBrand() {
        return this.brand;
    }
    public void setBrand(String brand) {
        this.brand = brand;
    }
    public Integer getPassengerNum() {
        return this.passengerNum;
    }
    public void setPassengerNum(Integer passengerNum) {
        this.passengerNum = passengerNum;
    }
    public Boolean getDemo() {
        return this.demo;
    }
    public void setDemo(Boolean demo) {
        this.demo = demo;
    }
}
