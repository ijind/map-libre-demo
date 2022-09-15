package ai.txai.commonbiz.entries;

import ai.txai.commonbiz.bean.TripPlanResponse;
import ai.txai.commonbiz.bean.VehicleModelResponse;
import ai.txai.database.vehicle.VehicleModel;

/**
 * Time: 16/03/2022
 * Author Hay
 */
public class RequestRideItem {
    private String vModelId;

    private VehicleModel vehicleModel;

    private TripPlanResponse tripPlan;

    public String getvModelId() {
        return vModelId;
    }

    public void setvModelId(String vModelId) {
        this.vModelId = vModelId;
    }

    public VehicleModel getVehicleModel() {
        return vehicleModel;
    }

    public void setVehicleModel(VehicleModel vehicleModel) {
        this.vehicleModel = vehicleModel;
    }

    public TripPlanResponse getTripPlan() {
        return tripPlan;
    }

    public void setTripPlan(TripPlanResponse tripPlan) {
        this.tripPlan = tripPlan;
    }
}
