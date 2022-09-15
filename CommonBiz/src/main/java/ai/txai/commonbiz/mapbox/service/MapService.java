package ai.txai.commonbiz.mapbox.service;

import android.animation.Animator;
import android.content.Context;
import android.view.View;

import com.mapbox.maps.plugin.gestures.OnMoveListener;
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener;

import java.util.List;

import ai.txai.common.countdown.OnCountDownTickListener;
import ai.txai.commonbiz.main.MainV2Activity;
import ai.txai.commonbiz.main.MainV2ViewModel;
import ai.txai.database.location.Point;
import ai.txai.database.site.Site;
import ai.txai.database.vehicle.VehicleIndicator;

/**
 * Time: 01/03/2022
 * Author Hay
 * 1. 绘制站点（带名字）
 * 2. 绘制人所在位置，需要跳动，并指明方向
 * 3. 绘制上车点
 * 4. 绘制下车点
 * 5. 上车点跳动
 * 6. 绘制车到上车点路线
 * 7. 绘制上车点到下车点路线
 * 8. 绘制车到下车点路线(？或者上车点到车路线)
 * 9. 人到站点路线导航（暂时不做）
 */
public interface MapService {
    String PREFIX_SITE = "site_";
    String PREFIX_VEHICLE = "vehicle_";
    String PREFIX_MARKER = "marker_";
    String PREFIX_VIEW = "view_";

    String SOURCE_ROUTER_BLUE = "source-router-1";
    String LAYER_ROUTER_BLUE = "layer-user-1";
    Double WIDTH_ROUTER_BLUE = 8.0;

    String SOURCE_ROUTER_PERSON = "source-router-person";
    String LAYER_ROUTER_PERSON = "layer-user-person";
    Double WIDTH_ROUTER_PERSON = 3.0;

    String ROUTER_LAYER = "router-arrow-layer";
    String ROUTER_IMAGE = "router-arrow-image";
    String ROUTER_SOURCE = "router-arrow-source";

    String LAYER_FILL = "layer_area_fill";
    String SOURCE_FILL = "source_area_fill";
    String LAYER_FILL_FRAME = "layer_fill_area_frame";
    String SOURCE_FILL_FRAME = "source_fill_area_frame";

    String LAYER_CIRCLE = "layer-circle";

    String SITE_LAYER = "site-layer";
    String SITE_IMAGE = "site-image";
    String SITE_SOURCE = "site-source";

    String VEHICLE_LAYER = "vehicle-layer";

    String TAG = "MapService";

    String NOTICE_PICK_UP = "notice_pickup";
    String NOTICE_SELECT_SITE_GUIDE = "notice_pick_up_guide";
    String NOTICE_DROP_OFF = "notice_dropOff";
    String ROUTE_NOTICE = "route_notice";
    String WAITING_COUNT_DOWN = "waiting_count_down";
    String RUNNING_NOTICE = "running_notice";
    String WAITING_WHEN_ARRIVED = "waiting_arrived";
    String CHARGING_NOTICE = "charging_notice";

    String NAME_PICK_UP = "name_pick_up";
    String NAME_PICK_UP_NO_OFFSET = "name_pick_up_no_offset";
    String NAME_DROP_OFF = "name_drop_off";

    String MARKER_PICK_UP = "pick_up";
    String MARKER_PICK_UP_SMALL = "pick_up_small";
    String MARKER_DROP_OFF = "drop_off";
    String MARKER_DROP_OFF_SMALL = "drop_off_small";

    View init(Context context);

    void onFinish();

    /**
     * 站点
     *
     * @param sites
     */
    void drawSites(List<Site> sites);

    /**
     * 车辆
     *
     * @param vehicles
     */
    void drawVehicles(List<VehicleIndicator> vehicles);

    /**
     * 标记点
     *
     * @param iconRes
     * @param point
     * @param offset
     */
    void drawMarker(String tag, int iconRes, Point point, List<Double> offset);

    /**
     * 路线
     *
     * @param router
     * @param color
     */
    void drawRouter(List<Point> router, int color);

    /**
     * 行驶动画
     * @param router
     * @param vehicle
     * @param duration
     * @param color
     */
    void drawRouter(List<Point> router, VehicleIndicator vehicle, int duration, int color, String tag,
                    Integer top, Integer left, Integer bottom, Integer right,
                    View.OnClickListener listener);

    void drawArrivingRouter(List<Point> router, VehicleIndicator vehicle, int duration, int color, String tag,
                    Integer top, Integer left, Integer bottom, Integer right, Point person,
                    View.OnClickListener listener);

    /**
     * 自定义View
     *
     * @param tag
     * @param layoutRes
     * @param point
     * @return
     */
    View drawView(String tag, int layoutRes, Point point, List<Double> offset, View.OnClickListener clickListener);

    void zoom(Double zoom);

    void center(Point point, Animator.AnimatorListener listener);

    void center(List<Point> point, Integer top, Integer left, Integer bottom, Integer right, Animator.AnimatorListener listener);

    void drawPickUpSmall(Point point);

    void drawDropOffSmall(Point point);

    void ringPoint(Point point);

    void drawCountdown(Site site, int time, OnCountDownTickListener tickListener);

    void drawWaitingWhenArrived(int time, Site site);

    void drawChargingNotice(VehicleIndicator vehicleIndicator, View.OnClickListener listener);

    void drawRunningNotice(VehicleIndicator vehicleIndicator);

    void drawEstRouteNotice(Point point, int eta, double emt);

    Point toCurrentPoint(Animator.AnimatorListener listener);

    void enableUserLocation(boolean b);

    void addOnIndicatorPositionChangeListener(OnIndicatorPositionChangedListener listener);

    void addOnMoveListener(OnMoveListener mainV2Activity);

    void drawFillWithLine(List<Point> points);

    void hideFillWithLine();

    void centerWithAnimation(Point point, Animator.AnimatorListener listener);

    /**
     * 地图中间位置
     *
     * @return
     */
    com.mapbox.geojson.Point getCenterPoint();

    /**
     * 用户位置
     *
     * @return
     */
    com.mapbox.geojson.Point currentUserPoint();

    void drawPickUpNameNoOffset(Site site);

    /**
     * only name with transparent background
     */
    void drawPickUpName(Site pickUpSite);

    /**
     * only name with transparent background
     */
    void drawDropOffName(Site dropOffSite);
    /**
     * white background whit more icon
     */
    void drawPickUpNameV2(Site pickUpSite, View.OnClickListener listener);
    /**
     * white background whit more icon
     */
    void drawDropOffNameV2(Site dropOffSite, View.OnClickListener listener);

    void gestureFocalPoint(Point point);

    void drawPersonRoute(com.mapbox.geojson.Point from, com.mapbox.geojson.Point to);

    void logoBottom(int bottomHeight);
}
