package ai.txai.commonbiz.bean;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import ai.txai.database.vehicle.VehicleModel;

/**
 * Time: 16/03/2022
 * Author Hay
 */
public class VehicleModelResponse {
    @SerializedName("desc")
    public String desc;
    @SerializedName("max_passenger_num")
    public int maxPassengerNum;
    @SerializedName("name")
    public String name;
    @SerializedName("picture")
    public String picture;
    @SerializedName("pstrategy_id")
    public String pstrategyId;
    @SerializedName("status")
    public int status;
    @SerializedName("vmodel_id")
    public String vmodelId;
    @SerializedName("color")
    public String color;


    public VehicleModel toVehicleModel() {
        VehicleModel model = new VehicleModel();
        model.setId(vmodelId);
        model.setName(name);
        model.setPictureUrl(picture);
        model.setDescription(desc);
        model.setMaxPassengerNum(maxPassengerNum);
        model.setColor(color);
        return model;
    }

    public static List<VehicleModel> toVehicleModels(List<VehicleModelResponse> responseList) {
        List<VehicleModel> list = new ArrayList<>();
        for (int i = 0; i < responseList.size(); i++) {
            list.add(responseList.get(i).toVehicleModel());
        }
        return list;
    }
}
