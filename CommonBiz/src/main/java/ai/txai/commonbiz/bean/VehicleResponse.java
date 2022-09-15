package ai.txai.commonbiz.bean;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import ai.txai.database.vehicle.VehicleIndicator;

/**
 * Time: 15/03/2022
 * Author Hay
 */
public class VehicleResponse {
    @SerializedName("vehicle_no")
    public String vehicleNo;
    @SerializedName("longitude")
    public double longitude;
    @SerializedName("latitude")
    public double latitude;


    public VehicleIndicator toVehicle() {
        return new VehicleIndicator(vehicleNo, longitude, latitude);
    }

    public static List<VehicleIndicator> toVehicles(List<VehicleResponse> responses) {
        List<VehicleIndicator> vehicles = new ArrayList<>();
        for (int i = 0; i < responses.size(); i++) {
            vehicles.add(responses.get(i).toVehicle());
        }
        return vehicles;
    }
}
