package ai.txai.commonbiz.bean;

import com.google.gson.annotations.SerializedName;

import ai.txai.database.vehicle.Vehicle;

/**
 * Time: 18/03/2022
 * Author Hay
 */
public class VehicleDetailResponse {
    @SerializedName("vehicle_no")
    public String vehicleNo;
    @SerializedName("plate_no")
    public String plateNo;
    @SerializedName("status")
    public int status;
    @SerializedName("color")
    public String color;
    @SerializedName("passenger_num")
    public int passengerNum;
    @SerializedName("brand")
    public String brand;
    @SerializedName("price_strategy_id")
    public String priceStrategyId;
    @SerializedName("car_model_id")
    public String carModelId;
    @SerializedName("demo")
    public boolean demo;


    public Vehicle toVehicle() {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(vehicleNo);
        vehicle.setPassengerNum(passengerNum);
        vehicle.setBrand(brand);
        vehicle.setDemo(demo);
        vehicle.setVehicleNo(vehicleNo);
        vehicle.setPlateNo(plateNo);
        vehicle.setColor(color);
        return vehicle;
    }
}
