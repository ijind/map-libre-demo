package ai.txai.commonbiz.onetrip;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ThreadUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.txai.common.observer.CommonObserver;
import ai.txai.common.thread.TScheduler;
import ai.txai.common.value.Result;
import ai.txai.commonbiz.bean.AreaResponse;
import ai.txai.commonbiz.bean.VehicleResponse;
import ai.txai.commonbiz.data.BizData;
import ai.txai.commonbiz.repository.BizApiRepository;
import ai.txai.database.site.Site;
import ai.txai.database.vehicle.VehicleIndicator;
import ai.txai.database.vehicle.VehicleModel;

/**
 * Time: 27/06/2022
 * Author Hay
 */
public class SelfDrivingTaxi {



    private static class Holder {
        private static SelfDrivingTaxi instance = new SelfDrivingTaxi();
    }

    public static SelfDrivingTaxi getInstance() {
        return Holder.instance;
    }

    private SelfDrivingTaxi() {
    }

    private Result<List<AreaResponse>> areas = new Result<>();
    private Result<Map<String, Site>> sites = new Result<>();
    private Result<List<VehicleModel>> vehicleModels = new Result<>();
    private Result<List<VehicleIndicator>> vehicles = new Result<>();//车辆状态，比如位置，行驶方向等

    private final List<SelfDrivingListener> drivingListener = new ArrayList<>();

    public Result<Map<String, Site>> getSites() {
        return sites;
    }

    public Result<List<VehicleModel>> getVehicleModels() {
        return vehicleModels;
    }

    public void clearData() {
        areas = new Result<>();
        sites = new Result<>();
        vehicleModels = new Result<>();
        vehicles = new Result<>();
    }

    public void requestAreas() {
        if (areas.isComplete() && areas.getData() != null && !areas.getData().isEmpty()) {
            return;
        } else {
            requestSites();
            requestVehicleModel();
        }
        BizApiRepository.INSTANCE.areaList().observeOn(TScheduler.INSTANCE.io())
                .subscribe(new CommonObserver<List<AreaResponse>>() {
                    @Override
                    public void onSuccess(@Nullable List<AreaResponse> areaResponses) {
                        super.onSuccess(areaResponses);
                        areas.setData(areaResponses);
                        ThreadUtils.runOnUiThread(() -> {
                            notifyLoadedAreas();
                            requestSites();
//                            requestAvailableVehicles();
                            requestVehicleModel();
                        });
                    }

                    @Override
                    public void onFailed(@Nullable String msg) {
                        super.onFailed(msg);
                        areas.setMsg(msg);
                        ThreadUtils.runOnUiThread(() -> notifyLoadedAreas());
                    }
                });
    }

    private void requestSites() {
        if (sites.isComplete() && sites.getData() != null && !sites.getData().isEmpty()) {
            return;
        }
        BizData.getInstance().requestSites(new BizData.SiteChangeListener() {
            @Override
            public void onLoaded(List<Site> siteList) {
                Map<String, Site> siteMap = new HashMap<>();
                for (Site site : siteList) {
                    siteMap.put(site.getId(), site);
                }
                sites.setData(siteMap);
            }

            @Override
            public void onFailed(String msg) {
                sites.setMsg(msg);
            }
        });
    }

    private void requestVehicleModel() {
        BizData.getInstance().requestVehicleModel(new BizData.VehicleModelChangeListener() {
            @Override
            public void finishLoaded(List<VehicleModel> list) {
                vehicleModels.setData(list);
            }
        });
    }

    /**
     * 请求可用车辆，用来更新首页小车位置
     */
    public void requestAvailableVehicles() {
        BizApiRepository.INSTANCE.availableVehicle().subscribeOn(TScheduler.INSTANCE.io())
                .subscribe(new CommonObserver<>() {
                    @Override
                    public void onSuccess(@Nullable List<VehicleResponse> responses) {
                        super.onSuccess(responses);
                        List<VehicleIndicator> vehicleIndicators = VehicleResponse.toVehicles(responses);
                        vehicles.setData(vehicleIndicators);
                        ThreadUtils.runOnUiThread(() -> notifyLoadedVehicles());
                    }

                    @Override
                    public void onFailed(@Nullable String msg) {
                        super.onFailed(msg);
                        vehicles.setMsg(msg);
                        ThreadUtils.runOnUiThread(() -> notifyLoadedVehicles());
                    }
                });
    }

    private void notifyLoadedVehicles() {
        if (vehicles.isComplete()) {
            for (SelfDrivingListener selfDrivingListener : drivingListener) {
                selfDrivingListener.loadedVehicles(vehicles);
            }
        }
    }

    private void notifyLoadedAreas() {
        if (areas.isComplete()) {
            for (SelfDrivingListener selfDrivingListener : drivingListener) {
                selfDrivingListener.loadedAreas(areas);
            }
        }
    }

    public void registerSelfDrivingListener(SelfDrivingListener listener) {
        drivingListener.add(listener);
        if (areas.isComplete()) {
            listener.loadedAreas(areas);
        }
        if (vehicles.isComplete()) {
            listener.loadedVehicles(vehicles);
        }
    }

    public void unregisterSelfDrivingListener(SelfDrivingListener listener) {
        drivingListener.remove(listener);
    }

    public AreaResponse getArea(String areaId) {
        if (areas.isComplete()) {
            for (AreaResponse area : areas.getData()) {
                if (TextUtils.equals(areaId, area.areaId)) {
                    return area;
                }
            }
        }
        return null;
    }

    public VehicleIndicator getVehicleIndicator(String id) {
        if (vehicles.isComplete()) {
            for (VehicleIndicator vehicle : vehicles.getData()) {
                if (TextUtils.equals(id, vehicle.id)) {
                    return vehicle;
                }
            }
        }
        return null;
    }
}
