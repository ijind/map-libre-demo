package ai.txai.commonbiz.onetrip;

import java.util.List;

import ai.txai.common.value.Result;
import ai.txai.commonbiz.bean.AreaResponse;
import ai.txai.database.vehicle.VehicleIndicator;

/**
 * Time: 27/06/2022
 * Author Hay
 */
public interface SelfDrivingListener {
    void loadedAreas(Result<List<AreaResponse>> areas);

    void loadedVehicles(Result<List<VehicleIndicator>> vehicles);
}
