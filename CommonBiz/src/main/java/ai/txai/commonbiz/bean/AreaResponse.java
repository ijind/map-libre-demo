package ai.txai.commonbiz.bean;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import ai.txai.database.location.Point;

/**
 * Time: 20/03/2022
 * Author Hay
 */
public class AreaResponse {

    @SerializedName("area_id")
    public String areaId;
    @SerializedName("name")
    public String name;
    @SerializedName("picture")
    public String picture;
    @SerializedName("op_end_time")
    public String opEndTime;
    @SerializedName("op_start_time")
    public String opStartTime;

    @SerializedName("op_week_days")
    public List<?> opWeekDays;

    @SerializedName("boundary")
    public String boundary;


    public List<Point> getBoundary() {
        List<Point> points = new ArrayList<>();
        if (TextUtils.isEmpty(boundary)) {
            return points;
        }
        String[] segments = boundary.split(";");
        for (int i = 0; i < segments.length; i++) {
            String segment = segments[i];
            String[] pointStr = segment.split(",");
            points.add(new Point(Double.parseDouble(pointStr[0]), Double.parseDouble(pointStr[1])));
        }
        return points;
    }
}
