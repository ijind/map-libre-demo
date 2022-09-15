package ai.txai.mapsdk.utils;


import android.graphics.Paint;
import android.os.Build;
import android.util.Pair;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.mapbox.geojson.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.mapbox.maps.Projection;
import com.mapbox.turf.TurfConstants;
import com.mapbox.turf.TurfMeasurement;

import ai.txai.common.log.LOG;
import ai.txai.database.enums.TripState;
import ai.txai.database.router.Router;
import ai.txai.database.site.Site;

/**
 * Time: 07/03/2022
 * Author Hay
 */
public class MapBoxUtils {

    public static List<com.mapbox.geojson.Point> router2Point(Router router) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return router.getPath().stream().map(point -> com.mapbox.geojson.Point.fromLngLat(point.getLongitude(), point.getLatitude())).collect(Collectors.toList());
        } else {
            List<com.mapbox.geojson.Point> list = new ArrayList<>();
            List<ai.txai.database.location.Point> path = router.getPath();
            for (ai.txai.database.location.Point point : path) {
                list.add(com.mapbox.geojson.Point.fromLngLat(point.getLongitude(), point.getLatitude()));
            }
            return list;
        }
    }

    public static Point centerMapboxPoint(List<Point> points) {
        double latitude = 0.0;
        double longitude = 0.0;
        for (Point point : points) {
            latitude += point.latitude();
            longitude += point.longitude();
        }
        return Point.fromLngLat(longitude / points.size(), latitude / points.size());
    }

    public static ai.txai.database.location.Point centerPoint(List<ai.txai.database.location.Point> points) {
        double latitude = 0.0;
        double longitude = 0.0;
        for (ai.txai.database.location.Point point : points) {
            latitude += point.getLatitude();
            longitude += point.getLongitude();
        }
        return new ai.txai.database.location.Point(longitude / points.size(), latitude / points.size());
    }


    /**
     * @param points
     * @return 单位米
     */
    public static double distance(List<Point> points) {
        double latMax = Double.MIN_VALUE;
        double lonMax = Double.MIN_VALUE;
        double latMin = Double.MAX_VALUE;
        double lonMin = Double.MAX_VALUE;
        for (Point point : points) {
            latMax = Math.max(latMax, point.latitude());
            lonMax = Math.max(lonMax, point.longitude());
            latMin = Math.min(latMin, point.latitude());
            lonMin = Math.min(lonMin, point.longitude());
        }

        return TurfMeasurement.distance(Point.fromLngLat(lonMax, latMax), Point.fromLngLat(lonMin, latMin), TurfConstants.UNIT_METERS);
    }

    public static double getZoom(List<Point> points) {
        Point point = MapBoxUtils.centerMapboxPoint(points);
        double distance = distance(points);
        return getZoom(point, distance);
    }

    public static double getZoom(Point point, double distance) {
        for (int i = 20; i > 0; i--) {
            double metersPerPixelAtLatitude = Projection.getMetersPerPixelAtLatitude(point.latitude(), i);
            if (distance > (metersPerPixelAtLatitude * 512)) {
                continue;
            }
            return i * 1.0;
        }
        return 13.0;
    }

    public static double distance(@NonNull ai.txai.database.location.Point point1, @NonNull ai.txai.database.location.Point point2) {
        return TurfMeasurement.distance(Point.fromLngLat(point1.getLongitude(), point1.getLatitude())
                , Point.fromLngLat(point2.getLongitude(), point2.getLatitude()));
    }

    /**
     * point1 到 point2 和point3直线距离
     *
     * @param point1
     * @param point2
     * @param point3
     * @return
     */
    public static double distance(@NonNull ai.txai.database.location.Point point1, @NonNull ai.txai.database.location.Point point2, @NonNull ai.txai.database.location.Point point3) {
        double a = distance(point1, point2);
        double b = distance(point1, point3);
        double c = distance(point2, point3);

        double p = (a + b + c) / 2;
        double area = Math.sqrt(p * (p - a) * (p - b) * (p - c));
        return area / c;
    }


    public static boolean isPointInsideSegment(@NonNull ai.txai.database.location.Point point1, @NonNull ai.txai.database.location.Point point2, @NonNull ai.txai.database.location.Point point3) {
        return isAcuteAngle(point1, point2, point3) && isAcuteAngle(point1, point3, point2);
    }

    private static boolean isAcuteAngle(@NonNull ai.txai.database.location.Point point1, @NonNull ai.txai.database.location.Point point2, @NonNull ai.txai.database.location.Point point3) {
        double d12 = distance(point1, point2);
        double d13 = distance(point1, point3);
        double d23 = distance(point2, point3);

        return d12 == 0 || d13 == 0 || d13 <= Math.hypot(d12, d23);
    }


    public static boolean isAcuteAngle(double d13, double d12, double d23) {
        return d12 == 0 || d13 == 0 || d13 <= Math.hypot(d12, d23);
    }

    private static ai.txai.database.location.Point getFoot(@NonNull ai.txai.database.location.Point point1, @NonNull ai.txai.database.location.Point point2, @NonNull ai.txai.database.location.Point point3) {
        double dLon = point2.getLongitude() - point3.getLongitude();
        double dLat = point2.getLatitude() - point3.getLatitude();

        double u = (point1.getLongitude() - point2.getLongitude()) * dLon + (point1.getLatitude() - point2.getLatitude()) * dLat;
        u /= dLon * dLon + dLat * dLat;
        return new ai.txai.database.location.Point(point2.getLongitude() + u * dLon, point2.getLatitude() + u * dLat);
    }

    public static Site nearlySite(Point point, List<Site> sites) {
        if (sites == null || sites.isEmpty()) {
            return null;
        }
        double minDistance = Double.MAX_VALUE;
        Site nearlySite = sites.get(0);
        for (int i = 0; i < sites.size(); i++) {
            Site site = sites.get(i);
            double distance = TurfMeasurement.distance(point, Point.fromLngLat(site.getPoint().getLongitude(), site.getPoint().getLatitude()));
            if (minDistance < distance) {
                continue;
            }
            minDistance = distance;
            nearlySite = site;
        }

        if (minDistance > 50) {//超过5公里， 提示附近没有站点
            return null;
        }

        return nearlySite;
    }


    public static Site nearlySite(Site currentSite, List<Site> sites) {
        if (sites == null || sites.isEmpty() || currentSite == null) {
            return currentSite;
        }
        double minDistance = Double.MAX_VALUE;
        Site nearlySite = sites.get(0);
        for (int i = 0; i < sites.size(); i++) {
            Site site = sites.get(i);
            double distance = TurfMeasurement.distance(Point.fromLngLat(currentSite.getPoint().getLongitude(),
                    currentSite.getPoint().getLatitude()), Point.fromLngLat(site.getPoint().getLongitude(), site.getPoint().getLatitude()));
            if (distance == 0) {
                continue;
            }
            if (minDistance < distance) {
                continue;
            }
            minDistance = distance;
            nearlySite = site;
        }
        return nearlySite;
    }

    public static ai.txai.database.location.Point transferToPoint(Point point) {
        return new ai.txai.database.location.Point(point.longitude(), point.latitude());
    }

    public static Point transferToMapboxPoint(ai.txai.database.location.Point point) {
        return Point.fromLngLat(point.getLongitude(), point.getLatitude());
    }

    public static List<ai.txai.database.location.Point> transferToPoints(List<Point> points) {
        List<ai.txai.database.location.Point> result = new ArrayList<>();
        for (Point point : points) {
            result.add(transferToPoint(point));
        }
        return result;
    }

    public static List<Point> transferToMapboxPoints(List<ai.txai.database.location.Point> points) {
        List<Point> result = new ArrayList<>();
        for (ai.txai.database.location.Point point : points) {
            result.add(transferToMapboxPoint(point));
        }
        return result;
    }

    public static float widthString14sp(String text) {
        Paint paint = new Paint();
        paint.setTextSize(SizeUtils.sp2px(14));
        return paint.measureText(text);
    }


    public static List<ai.txai.database.location.Point> calculateRouter(List<ai.txai.database.location.Point> path, ai.txai.database.location.Point vehiclePoint) {
        if (path == null) {
            return null;
        }
        if (vehiclePoint == null) {
            return path;
        }
        double minDistance = Double.MAX_VALUE;
        boolean inLine = false;
        int index = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            ai.txai.database.location.Point point1 = path.get(i);
            ai.txai.database.location.Point point2 = path.get(i + 1);
            double distanceBegin = MapBoxUtils.distance(vehiclePoint, point1);
            double distance = MapBoxUtils.distance(vehiclePoint, point1, point2);
            boolean insideSegment = MapBoxUtils.isPointInsideSegment(vehiclePoint, point1, point2);
            if (insideSegment && minDistance > distance) {
                minDistance = distance;
                index = i;
                if (minDistance == 0) {
                    inLine = true;
                    break;
                }
            }
            if (minDistance > distanceBegin) {
                minDistance = distance;
                index = i;
            }
        }

        ai.txai.database.location.Point foot = vehiclePoint;
        if (!inLine) {
            ai.txai.database.location.Point point1 = path.get(index);
            ai.txai.database.location.Point point2 = path.get(index + 1);
            foot = getFoot(vehiclePoint, point1, point2);
        }
        List<ai.txai.database.location.Point> points = new ArrayList<>(path.subList(index + 1, path.size()));
        points.add(0, foot);
        return points;
    }

    public static double bearing(ai.txai.database.location.Point srcPoint,
                                 ai.txai.database.location.Point desPoint) {
        return TurfMeasurement.bearing(Point.fromLngLat(srcPoint.getLongitude(), srcPoint.getLatitude()), Point.fromLngLat(desPoint.getLongitude(), desPoint.getLatitude()));
    }


    /**
     *
     * @param path 路径
     * @param srcPoint 行驶起点
     * @param desPoint 行驶终点
     * @return 起始坐标点&行驶路径
     */
    public static Pair<Integer, List<ai.txai.database.location.Point>> calculateRouter(List<ai.txai.database.location.Point> path,
                                                         ai.txai.database.location.Point srcPoint,
                                                         ai.txai.database.location.Point desPoint) {
        Pair<Boolean, Integer> srcPair = closestPointIndex(path, srcPoint);
        Pair<Boolean, Integer> desPair = closestPointIndex(path, desPoint);
        if (srcPair == null || desPair == null || srcPair.second > desPair.second) {
            return null;
        }
        List<ai.txai.database.location.Point> points = new ArrayList<>();
        if (srcPair.second.equals(desPair.second)) {
            points.add(srcPoint);
            points.add(desPoint);
        } else {
            points.addAll(path.subList(srcPair.second + 1, desPair.second + 1));
            points.add(0, srcPoint);
            if (!desPair.first) {
                points.add(desPoint);
            }
        }
        return new Pair<>(srcPair.second, points);
    }


    /**
     *
     * @param path 路线
     * @param vehiclePoint 车位置
     * @return 是否是路线上的点 & 路线上当前位置前一个点的下标
     */
    public static Pair<Boolean, Integer> closestPointIndex(List<ai.txai.database.location.Point> path, ai.txai.database.location.Point vehiclePoint) {
        if (path == null) {
            return null;
        }
        if (vehiclePoint == null) {
            return null;
        }
        double minDistance = Double.MAX_VALUE;
        int index = 0;
        boolean inPoint = false;
        for (int i = 0; i < path.size() - 1; i++) {
            ai.txai.database.location.Point point1 = path.get(i);
            ai.txai.database.location.Point point2 = path.get(i + 1);
            double distanceBegin = MapBoxUtils.distance(vehiclePoint, point1);
            double distanceEnd = MapBoxUtils.distance(vehiclePoint, point2);
            if (distanceBegin == 0.0) {
                index = i;
                inPoint = true;
                break;
            }
            if (distanceEnd == 0.0) {
                index = i + 1;
                inPoint = true;
                break;
            }
            double distanceSegment = MapBoxUtils.distance(point1, point2);
            boolean insideSegment = MapBoxUtils.isAcuteAngle(distanceEnd, distanceBegin, distanceSegment) && MapBoxUtils.isAcuteAngle(distanceBegin, distanceEnd, distanceSegment);
            if (insideSegment) {
                double distance = MapBoxUtils.distance(vehiclePoint, point1, point2);
                if (minDistance > distance) {
                    minDistance = distance;
                    index = i;
                }
            }

            if (minDistance == 0.0) {
                break;
            }

            if (minDistance > distanceBegin) {
                minDistance = distanceBegin;
                index = i;
            }
            if (minDistance > distanceEnd) {
                minDistance = distanceEnd;
                index = i + 1;
            }
        }
        return new Pair<>(inPoint, index);
    }

}
