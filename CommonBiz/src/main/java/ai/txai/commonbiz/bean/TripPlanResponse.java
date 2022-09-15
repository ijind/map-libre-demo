package ai.txai.commonbiz.bean;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import ai.txai.common.log.LOG;
import ai.txai.database.location.Point;
import ai.txai.database.router.Router;

/**
 * Time: 10/03/2022
 * Author Hay
 */
public class TripPlanResponse {


    @SerializedName("user_id")
    public String userId;
    @SerializedName("vehicle_type_id")
    public String vehicleTypeId;
    @SerializedName("pickup_poi_id")
    public String pickupPoiId;
    @SerializedName("dropoff_poi_id")
    public String dropoffPoiId;
    @SerializedName("estimate_trip_info")
    public EstimateTripInfoBean estimateTripInfo;
    @SerializedName("estimate_fare")
    public double estimateFare;
    @SerializedName("fare_info")
    public FareInfoBean fareInfo;

    public static class EstimateTripInfoBean {
        @SerializedName("eta")
        public int eta;
        @SerializedName("emt")
        public double emt;
        @SerializedName("segments")
        public List<SegmentsBean> segments;


    }

    public static class FareInfoBean {
        @SerializedName("base_fare")
        public double baseFare;
        @SerializedName("base_mileage")
        public double baseMileage;
        @SerializedName("per_mileage_surcharge")
        public double perMileageSurcharge;
        @SerializedName("mileage_surcharge")
        public double mileageSurcharge;
        @SerializedName("total_fare")
        public double totalFare;
        @SerializedName("discount_fare")
        public double discountFare;
        @SerializedName("free_order")
        public boolean freeOrder;
    }

    public static Router toRouter(TripPlanResponse response) {
        Router router = new Router();
        router.setPath(new ArrayList<>());
        try {
            List<SegmentsBean> segments = response.estimateTripInfo.segments;
            for (int i = 0; i < segments.size(); i++) {
                router.getPath().add(new Point(segments.get(i).longitude, segments.get(i).latitude));
            }
            return router;
        } catch (Exception e) {
            LOG.e(e, "cannot get trip plan");
        }
        return new Router();
    }
}