package ai.txai.commonbiz.bean;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import ai.txai.database.location.Point;
import ai.txai.database.site.Site;

/**
 * Time: 08/03/2022
 * Author Hay
 */
public class POIResponse {


    @SerializedName("poi_id")
    public String poiId;
    @SerializedName("area_id")
    public String areaId;
    @SerializedName("name")
    public String name;
    @SerializedName("longitude")
    public double longitude;
    @SerializedName("latitude")
    public double latitude;
    @SerializedName("address")
    public String address;
    @SerializedName("classifier_id")
    public String classifierId;

    public Site toSite() {
        Site site = new Site();
        site.setPoint(new Point(longitude, latitude));
        site.setId(poiId);
        site.setName(name);
        site.setDescription(address);
        site.setAreaId(areaId);
        return site;
    }

    public static List<Site> toSite(List<POIResponse> responses) {
        if (responses == null || responses.isEmpty()) {
            return new ArrayList<>();
        }
        List<Site> result = new ArrayList<>();
        for (int i = 0; i < responses.size(); i++) {
            result.add(responses.get(i).toSite());
        }
        return result;
    }
}
