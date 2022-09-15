package ai.txai.commonbiz.data;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ThreadUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.txai.common.log.LOG;
import ai.txai.common.observer.CommonObserver;
import ai.txai.common.thread.TScheduler;
import ai.txai.common.value.Result;
import ai.txai.commonbiz.bean.AreaResponse;
import ai.txai.commonbiz.bean.POIResponse;
import ai.txai.commonbiz.bean.VehicleDetailResponse;
import ai.txai.commonbiz.bean.VehicleModelResponse;
import ai.txai.commonbiz.onetrip.SelfDrivingTaxi;
import ai.txai.commonbiz.repository.BizApiRepository;
import ai.txai.database.GreenDaoHelper;
import ai.txai.database.order.Order;
import ai.txai.database.site.Site;
import ai.txai.database.vehicle.Vehicle;
import ai.txai.database.vehicle.VehicleIndicator;
import ai.txai.database.vehicle.VehicleModel;
import ai.txai.greendao.DaoSession;
import ai.txai.greendao.OrderDao;
import ai.txai.greendao.VehicleDao;

/**
 * Time: 18/03/2022
 * Author Hay
 * 数据操作类
 */
public class BizData {
    public static final String TAG = "BizData";
    private final Map<String, Vehicle> vehicleMap = new HashMap<>();
    private final Map<String, Order> orderMap = new HashMap<>();

    private final List<VehicleModelChangeListener> vehicleModelChangeListeners = new ArrayList<>();
    private final Map<String, List<VehicleChangeListener>> vehicleListenerMap = new HashMap<>();
    private final List<SiteChangeListener> siteChangeListeners = new ArrayList<>();

    public static BizData getInstance() {
        return Holder.instance;
    }

    private static class Holder {
        private static BizData instance = new BizData();
    }

    private BizData() {
    }

    public DaoSession getDaoSession() {
        GreenDaoHelper.getInstance().waitUserDb();
        return GreenDaoHelper.getInstance().userDaoSession();
    }

    public boolean siteLoaded() {
        return SelfDrivingTaxi.getInstance().getSites().isComplete();
    }

    public Site getSite(String id) {
        if (TextUtils.isEmpty(id)) {
            return null;
        }
        Map<String, Site> data = SelfDrivingTaxi.getInstance().getSites().getData();
        if (data == null) {
            return null;
        }
        return data.get(id);
    }

    public Vehicle getVehicle(String id) {
        Vehicle vehicle = vehicleMap.get(id);
        if (vehicle == null) {
            VehicleDao vehicleDao = getDaoSession().getVehicleDao();
            vehicle = vehicleDao.load(id);
            if (vehicle != null) {
                vehicleMap.put(id, vehicle);
            }
        }
        return vehicle;
    }

    private void saveVehicle(Vehicle vehicle) {
        if (vehicle != null) {
            VehicleDao vehicleDao = getDaoSession().getVehicleDao();
            vehicleDao.insertOrReplace(vehicle);
            vehicleMap.put(vehicle.getId(), vehicle);
        }
    }


    public Order getOrder(String id) {
        LOG.d(TAG, "getOrder %s", id);
        Order order = orderMap.get(id);
        if (order == null) {
            OrderDao orderDao = getDaoSession().getOrderDao();
            order = orderDao.load(id);
        }
        return order;
    }


    public boolean saveOrder(Order order) {
        if (order == null) {
            return false;
        }
        OrderDao orderDao = getDaoSession().getOrderDao();
        orderDao.insertOrReplace(order);
        orderMap.put(order.getId(), order);
        return true;
    }

    public VehicleIndicator getVehicleIndicator(String id) {
        return SelfDrivingTaxi.getInstance().getVehicleIndicator(id);
    }

    public AreaResponse getArea(String areaId) {
        return SelfDrivingTaxi.getInstance().getArea(areaId);
    }

    public VehicleModel getVehicleModel(String id) {
        Result<List<VehicleModel>> vehicles = SelfDrivingTaxi.getInstance().getVehicleModels();
        if (vehicles.getData() != null && !vehicles.getData().isEmpty()) {
            for (VehicleModel model : vehicles.getData()) {
                if (TextUtils.equals(model.getId(), id)) {
                    return model;
                }
            }
        }
        return null;
    }

    public void requestSites(SiteChangeListener listener) {
        Result<Map<String, Site>> sitesResult = SelfDrivingTaxi.getInstance().getSites();
        if (sitesResult.getData() != null && !sitesResult.getData().isEmpty()) {
            listener.onLoaded(new ArrayList<>(sitesResult.getData().values()));
            return;
        }

        siteChangeListeners.add(listener);
        if (siteChangeListeners.size() == 1) {
            BizApiRepository.INSTANCE.poiList().subscribeOn(TScheduler.INSTANCE.io())
                    .subscribe(new CommonObserver<>() {
                        @Override
                        public void onSuccess(@Nullable List<POIResponse> responses) {
                            List<Site> siteList = POIResponse.toSite(responses);
                            ThreadUtils.runOnUiThread(() -> notifySitesFinish(siteList, ""));
                        }

                        @Override
                        public void onFailed(@Nullable String msg) {
                            ThreadUtils.runOnUiThread(() -> notifySitesFinish(null, msg));
                        }
                    });
        }
    }


    public void requestSite(SingleSiteChangeListener listener, String... ids) {
        requestSites(new SiteChangeListener() {
            @Override
            public void onLoaded(List<Site> siteList) {
                Site[] sites = new Site[ids.length];
                for (int i = 0; i < ids.length; i++) {
                    Site site = getSite(ids[i]);
                    sites[i] = site;
                }

                if (listener != null) {
                    listener.onLoaded(sites);
                }
            }

            @Override
            public void onFailed(String msg) {

            }
        });
    }

    public void requestVehicleModel(VehicleModelChangeListener listener) {
        Result<List<VehicleModel>> vehicles = SelfDrivingTaxi.getInstance().getVehicleModels();
        if (vehicles.getData() != null && !vehicles.getData().isEmpty()) {
            listener.finishLoaded(vehicles.getData());
            return;
        }

        if (vehicleModelChangeListeners.isEmpty()) {
            vehicleModelChangeListeners.add(listener);
            BizApiRepository.INSTANCE.vehicleModels().subscribeOn(TScheduler.INSTANCE.io())
                    .subscribe(new CommonObserver<>() {
                        @Override
                        public void onSuccess(@Nullable List<VehicleModelResponse> responseList) {
                            List<VehicleModel> list = VehicleModelResponse.toVehicleModels(responseList);
                            ThreadUtils.runOnUiThread(() -> notifyVehicleFinish(list));

                        }

                        @Override
                        public void onFailed(@Nullable String msg) {
                            ThreadUtils.runOnUiThread(() -> notifyVehicleFinish(null));
                        }
                    });
        } else {
            vehicleModelChangeListeners.add(listener);
        }
    }


    public void requestVehicleDetails(String id, VehicleChangeListener listener) {
        Vehicle vehicle = BizData.getInstance().getVehicle(id);
        if (vehicle != null) {
            listener.onLoaded(vehicle);
            return;
        }
        List<VehicleChangeListener> vehicleChangeListeners = vehicleListenerMap.get(id);
        if (vehicleChangeListeners == null) {
            vehicleChangeListeners = new ArrayList<>();
        }
        vehicleChangeListeners.add(listener);
        vehicleListenerMap.put(id, vehicleChangeListeners);
        if (vehicleChangeListeners.size() == 1) {
            BizApiRepository.INSTANCE.vehicleDetails(id)
                    .subscribeOn(TScheduler.INSTANCE.io())
                    .subscribe(new CommonObserver<>() {
                        @Override
                        public void onSuccess(@Nullable VehicleDetailResponse response) {
                            super.onSuccess(response);
                            Vehicle vehicleFromServer = response.toVehicle();
                            if (vehicleFromServer != null) {
                                saveVehicle(vehicleFromServer);
                                ThreadUtils.runOnUiThread(() -> notifyVehicleFinish(id, vehicleFromServer));
                            }
                        }

                        @Override
                        public void onFailed(@Nullable String msg) {
                            super.onFailed(msg);
                            ThreadUtils.runOnUiThread(() -> notifyVehicleFinish(id, null));
                        }
                    });
        }
    }

    private void notifyVehicleFinish(List<VehicleModel> vehicleModels) {
        for (int i = 0; i < vehicleModelChangeListeners.size(); i++) {
            vehicleModelChangeListeners.get(i).finishLoaded(vehicleModels);
        }
        vehicleModelChangeListeners.clear();
    }

    private void notifySitesFinish(List<Site> sites, String msg) {
        for (int i = 0; i < siteChangeListeners.size(); i++) {
            if (sites != null) {
                siteChangeListeners.get(i).onLoaded(sites);
            } else {
                siteChangeListeners.get(i).onFailed(msg);
            }
        }
        siteChangeListeners.clear();
    }

    private void notifyVehicleFinish(String id, Vehicle vehicle) {
        List<VehicleChangeListener> remove = vehicleListenerMap.remove(id);
        if (remove == null) {
            return;
        }
        for (int i = 0; i < remove.size(); i++) {
            remove.get(i).onLoaded(vehicle);
        }
    }

    public interface VehicleModelChangeListener {
        void finishLoaded(List<VehicleModel> vehicleModels);
    }

    public interface VehicleChangeListener {
        void onLoaded(Vehicle vehicle);
    }

    public interface SiteChangeListener {
        void onLoaded(List<Site> siteList);

        void onFailed(String msg);
    }

    public interface SingleSiteChangeListener {
        void onLoaded(Site... site);
    }
}
