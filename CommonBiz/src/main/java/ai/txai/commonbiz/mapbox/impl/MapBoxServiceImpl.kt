package ai.txai.commonbiz.mapbox.impl

import ai.txai.common.countdown.CountDownTimerManager
import ai.txai.common.countdown.OnCountDownTickListener
import ai.txai.common.log.LOG
import ai.txai.common.utils.AndroidUtils
import ai.txai.common.utils.BitmapUtils
import ai.txai.common.utils.FormatUtils
import ai.txai.common.utils.setDebounceClickListener
import ai.txai.common.widget.txaitextview.StrokeTextView
import ai.txai.commonbiz.R
import ai.txai.commonbiz.mapbox.animation.PuckPulsingAnimator
import ai.txai.commonbiz.mapbox.service.MapService
import ai.txai.commonbiz.mapbox.service.MapboxLocationProvider
import ai.txai.database.site.Site
import ai.txai.database.vehicle.VehicleIndicator
import ai.txai.mapsdk.utils.MapBoxUtils
import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import com.blankj.utilcode.util.SizeUtils
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.MapboxDirections
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.core.constants.Constants.PRECISION_6
import com.mapbox.geojson.*
import com.mapbox.maps.*
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.extension.style.StyleExtensionImpl
import com.mapbox.maps.extension.style.expressions.dsl.generated.interpolate
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.addLayerAbove
import com.mapbox.maps.extension.style.layers.addLayerBelow
import com.mapbox.maps.extension.style.layers.generated.*
import com.mapbox.maps.extension.style.layers.getLayer
import com.mapbox.maps.extension.style.layers.properties.generated.LineCap
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin
import com.mapbox.maps.extension.style.layers.properties.generated.SymbolPlacement
import com.mapbox.maps.extension.style.layers.properties.generated.Visibility
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.sources.getSourceAs
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.animation.MapAnimationOptions.Companion.mapAnimationOptions
import com.mapbox.maps.plugin.animation.easeTo
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.annotation.AnnotationConfig
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.attribution.attribution
import com.mapbox.maps.plugin.compass.compass
import com.mapbox.maps.plugin.gestures.*
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.logo.logo
import com.mapbox.maps.plugin.scalebar.scalebar
import com.mapbox.maps.viewannotation.viewAnnotationOptions
import com.mapbox.turf.TurfMeasurement
import retrofit2.Call
import retrofit2.Response
import java.util.concurrent.TimeUnit


/**
 * Time: 01/04/2022
 * Author Hay
 */
class MapBoxServiceImpl() : OnMapClickListener, MapService {
    private lateinit var mapView: MapView
    private lateinit var mapboxMap: MapboxMap

    private var vehicleMaps: MutableMap<String, VehicleIndicator> = mutableMapOf()
    private var annotationMaps: MutableMap<String, PointAnnotation> = mutableMapOf()
    private val viewMaps: MutableMap<String, View> = mutableMapOf()
    private var puckPulsingAnimator: PuckPulsingAnimator? = null
    private var vehicleAnnotationManager: PointAnnotationManager? = null
    private var pointAnnotationManager: PointAnnotationManager? = null
    var positionChangeListener: OnIndicatorPositionChangedListener? = null
    var mapClickListener: OnMapClickListener? = null
    private var countDownTimer: CountDownTimer? = null

    private var animator: AnimatorSet? = null
    private var centerAnimator: ValueAnimator? = null

    private var currentPoint: Point? = null
    private lateinit var gesturesPlugin: GesturesPlugin

    private var locationProvider: MapboxLocationProvider? = null


    private fun initMapView(context: Context): View {
        mapView = LayoutInflater.from(context).inflate(R.layout.mapbox_view, null, false) as MapView
        mapboxMap = mapView.getMapboxMap()
        mapboxMap.addOnMapClickListener(this)
        mapView.compass.enabled = false
        mapView.gestures.rotateEnabled = false
        mapView.scalebar.enabled = false
        mapView.attribution.enabled = false
        val builder =
            StyleExtensionImpl.Builder("mapbox://styles/hanyang6278/cl24dspfh000414nnerf7tg6k")
        mapboxMap.loadStyle(builder.build())
        gesturesPlugin = mapView.gestures
//        mapboxMap.getStyle(){ style ->
//            style.styleLayers.forEach {
//                LOG.i("MapBox", "layers %s", it.id)
//            }
//        }

        return mapView
    }

    override fun getCenterPoint(): Point {
        return mapboxMap.cameraState.center
    }

    override fun currentUserPoint(): Point? {
        return currentPoint
    }

    override fun enableUserLocation(b: Boolean) {
        initLocationPuck()
    }

    override fun addOnIndicatorPositionChangeListener(listener: OnIndicatorPositionChangedListener?) {
        this.positionChangeListener = listener
    }

    override fun addOnMoveListener(listener: OnMoveListener) {
        mapView.gestures.addOnMoveListener(listener)
    }

    private fun initLocationPuck() {
        var location = mapView.location
        locationProvider = MapboxLocationProvider(mapView.context.applicationContext)
        location.setLocationProvider(locationProvider!!)
        location.updateSettings {
            enabled = true
            pulsingEnabled = true
            pulsingMaxRadius = 50f
            pulsingColor = Color.parseColor("#4B7DF6")
            locationPuck = LocationPuck2D(
                topImage = AppCompatResources.getDrawable(
                    mapView.context,
                    R.drawable.biz_ic_mapbox_user_icon
                ),
                bearingImage = AppCompatResources.getDrawable(
                    mapView.context,
                    R.drawable.biz_ic_mapbox_user_bearing_icon
                ),
                shadowImage = AppCompatResources.getDrawable(
                    mapView.context,
                    R.drawable.biz_ic_mapbox_user_stroke_icon
                ),
                scaleExpression = interpolate {
                    linear()
                    zoom()
                    stop {
                        literal(0.0)
                        literal(0.6)
                    }
                    stop {
                        literal(20.0)
                        literal(1.0)
                    }
                }.toJson(),

                )
            location.addOnIndicatorPositionChangedListener { point ->
                currentPoint = point
                positionChangeListener?.onIndicatorPositionChanged(point)
            }
        }
    }

    private fun initSymbolLayer(style: Style) {
        if (style.styleLayerExists(MapService.SITE_LAYER)) {
            return
        }
        style.addImage(
            MapService.SITE_IMAGE,
            BitmapFactory.decodeResource(mapView.context.resources, R.mipmap.biz_ic_site)
        )

        style.addLayer(
            symbolLayer(MapService.SITE_LAYER, MapService.SITE_SOURCE) {
                iconImage(MapService.SITE_IMAGE)
                iconAllowOverlap(true)
            }
        )
    }

    private fun initRouteLayer(style: Style, color: Int) {
        if (!style.styleSourceExists(MapService.SOURCE_ROUTER_BLUE)) {
            style.addSource(geoJsonSource(MapService.SOURCE_ROUTER_BLUE))
        }
        if (!style.styleLayerExists(MapService.LAYER_ROUTER_BLUE)) {
            style.addLayerBelow(
                lineLayer(MapService.LAYER_ROUTER_BLUE, MapService.SOURCE_ROUTER_BLUE) {
                    lineColor(color)
                    lineCap(LineCap.ROUND)
                    lineJoin(LineJoin.ROUND)
                    lineWidth(MapService.WIDTH_ROUTER_BLUE)
                },
                "road-rail"
            )
        } else {
            val layer = style.getLayer(MapService.LAYER_ROUTER_BLUE) as LineLayer
            layer.lineColor(color)
        }
    }

    private fun initArrowSymbolLayer(style: Style) {
        if (style.styleLayerExists(MapService.ROUTER_LAYER)) {
            return
        }
        if (!style.styleSourceExists(MapService.ROUTER_SOURCE)) {
            style.addSource(geoJsonSource(MapService.ROUTER_SOURCE))
        }
        style.addImage(
            MapService.ROUTER_IMAGE,
            BitmapUtils.bitmapFromDrawableRes(mapView.context, R.drawable.ic_more_white)!!
        )

        style.addLayerAbove(
            symbolLayer(MapService.ROUTER_LAYER, MapService.ROUTER_SOURCE) {
                iconImage(MapService.ROUTER_IMAGE)
                symbolPlacement(SymbolPlacement.LINE)
                symbolSpacing(30.0)
            },
            MapService.LAYER_ROUTER_BLUE
        )
    }

    override fun onMapClick(point: Point): Boolean {
        mapClickListener?.onMapClick(point)
        return false
    }

    private fun formatVehicleId(id: String): String {
        return String.format("%s%s", MapService.PREFIX_VEHICLE, id)
    }

    private fun formatSiteId(id: String): String {
        return String.format("%s%s", MapService.PREFIX_SITE, id)
    }

    private fun formatMarkerTag(tag: String): String {
        return String.format("%s%s", MapService.PREFIX_MARKER, tag)
    }

    private fun formatViewTag(tag: String): String {
        return String.format("%s%s", MapService.PREFIX_VIEW, tag)
    }


    private fun drawVehicle(vehicleIndicator: VehicleIndicator) {
        mapboxMap.getStyle {
            ensureVehicleAnnotationManager(it)
            var formatVehicleId = formatVehicleId(vehicleIndicator.id)

            var annotation = annotationMaps[formatVehicleId]
            var vehicleHistory = vehicleMaps[formatVehicleId]

            if (vehicleIndicator.prePoint == null) {
                vehicleIndicator.prePoint = vehicleHistory?.point
            }

            if (vehicleHistory != null
                && vehicleIndicator.prePoint != null
                && vehicleIndicator.prePoint.longitude == vehicleIndicator.point.longitude
                && vehicleIndicator.prePoint.latitude == vehicleIndicator.point.latitude
            ) {
                return@getStyle
            }

            if (vehicleIndicator.prePoint == null) {
                vehicleIndicator.prePoint = vehicleIndicator.point
            }
            var bearing = 0.0
            if (vehicleIndicator.prePoint != null) {
                val perPoint = Point.fromLngLat(
                    vehicleIndicator.prePoint.longitude,
                    vehicleIndicator.prePoint.latitude
                )

                val point = Point.fromLngLat(
                    vehicleIndicator.point.longitude,
                    vehicleIndicator.point.latitude
                )
                bearing = TurfMeasurement.bearing(perPoint, point)
            }
            if (annotation == null) {
                vehicleAnnotationManager?.apply {
                    BitmapUtils.bitmapFromDrawableRes(
                        mapView.context,
                        R.mipmap.ic_map_car
                    )?.let {
                        val noAnimationOption = PointAnnotationOptions()
                            .withPoint(
                                Point.fromLngLat(
                                    vehicleIndicator.point.longitude,
                                    vehicleIndicator.point.latitude
                                )
                            )
                            .withIconImage(it)
                            .withIconRotate(bearing)
                        annotation = create(noAnimationOption)
                        annotationMaps[formatVehicleId] = annotation!!
                        vehicleMaps[formatVehicleId] = vehicleIndicator
                    }
                }
            } else {
                annotation!!.point =
                    Point.fromLngLat(
                        vehicleIndicator.point.longitude,
                        vehicleIndicator.point.latitude
                    )
                annotation!!.iconRotate = bearing
                vehicleAnnotationManager?.update(annotation!!)
                vehicleMaps[formatVehicleId] = vehicleIndicator
            }
        }
    }

    private fun drawVehicle(vehicleIndicator: VehicleIndicator, bearing: Double) {
        mapboxMap.getStyle {
            ensureVehicleAnnotationManager(it)
            var formatVehicleId = formatVehicleId(vehicleIndicator.id)

            var annotation = annotationMaps[formatVehicleId]
            var vehicleHistory = vehicleMaps[formatVehicleId]

            if (vehicleIndicator.prePoint == null) {
                vehicleIndicator.prePoint = vehicleHistory?.point
            }
            if (vehicleIndicator.prePoint == null) {
                vehicleIndicator.prePoint = vehicleIndicator.point
            }
            if (annotation == null) {
                vehicleAnnotationManager?.apply {
                    BitmapUtils.bitmapFromDrawableRes(
                        mapView.context,
                        R.mipmap.ic_map_car
                    )?.let {
                        val noAnimationOption = PointAnnotationOptions()
                            .withPoint(
                                Point.fromLngLat(
                                    vehicleIndicator.point.longitude,
                                    vehicleIndicator.point.latitude
                                )
                            )
                            .withIconImage(it)
                            .withIconRotate(bearing)
                        annotation = create(noAnimationOption)
                        annotationMaps[formatVehicleId] = annotation!!
                        vehicleMaps[formatVehicleId] = vehicleIndicator
                    }
                }
            } else {
                annotation!!.point =
                    Point.fromLngLat(
                        vehicleIndicator.point.longitude,
                        vehicleIndicator.point.latitude
                    )
                annotation!!.iconRotate = bearing
                vehicleAnnotationManager?.update(annotation!!)
                vehicleMaps[formatVehicleId] = vehicleIndicator
            }
        }
    }

    private fun ensureAnnotationManager() {
        if (pointAnnotationManager == null) {
            pointAnnotationManager = mapView.annotations.createPointAnnotationManager()
        }
    }

    private fun ensureVehicleAnnotationManager(style: Style) {
        if (vehicleAnnotationManager == null) {
            initSymbolLayer(style)
            val config = AnnotationConfig(belowLayerId = MapService.SITE_LAYER, layerId = MapService.VEHICLE_LAYER)
            vehicleAnnotationManager = mapView.annotations.createPointAnnotationManager(config)
        }
    }

    override fun init(context: Context): View {
        return initMapView(context)
    }

    override fun drawSites(sites: MutableList<Site>) {
        mapView.getMapboxMap().getStyle { style ->
            initSymbolLayer(style)
            var source = geoJsonSource(MapService.SITE_SOURCE) {
                featureCollection(
                    FeatureCollection.fromFeatures(
                        sites.map {
                            Feature.fromGeometry(
                                Point.fromLngLat(
                                    it.point.longitude,
                                    it.point.latitude
                                )
                            )
                        }
                    ))
            }
            if (!style.styleSourceExists(MapService.SITE_SOURCE)) {
                style.addSource(source)
            }
        }
    }

    override fun drawVehicles(vehicles: MutableList<VehicleIndicator>) {
        vehicles.forEach {
            drawVehicle(it)
        }
    }

    override fun drawMarker(
        tag: String,
        iconRes: Int,
        point: ai.txai.database.location.Point,
        offset: List<Double>
    ) {
        ensureAnnotationManager()
        val formatMarker = formatMarkerTag(tag)

        var annotation = annotationMaps[formatMarker]

        if (annotation == null) {
            pointAnnotationManager?.apply {
                BitmapUtils.bitmapFromDrawableRes(
                    mapView.context,
                    iconRes
                )?.let {
                    val noAnimationOption = PointAnnotationOptions()
                        .withPoint(Point.fromLngLat(point.longitude, point.latitude))
                        .withIconImage(it)
                        .withIconOffset(offset)
                    annotation = create(noAnimationOption)
                    annotationMaps[formatMarker] = annotation!!
                }
            }
        } else {
            annotation!!.point = Point.fromLngLat(point.longitude, point.latitude)
            pointAnnotationManager?.update(annotation!!)
        }
    }

    override fun drawRouter(points: MutableList<ai.txai.database.location.Point>, color: Int) {
        mapboxMap.getStyle { st ->
            initRouteLayer(st, color)
            initArrowSymbolLayer(st)
            st.getSourceAs<GeoJsonSource>(MapService.SOURCE_ROUTER_BLUE)?.apply {
                feature(
                    Feature.fromGeometry(
                        LineString.fromLngLats(
                            MapBoxUtils.transferToMapboxPoints(
                                points
                            )
                        )
                    )
                )
            }

            st.getSourceAs<GeoJsonSource>(MapService.ROUTER_SOURCE)?.apply {
                feature(
                    Feature.fromGeometry(
                        LineString.fromLngLats(
                            MapBoxUtils.transferToMapboxPoints(
                                points
                            )
                        )
                    )
                )
            }
        }
    }

    override fun drawArrivingRouter(
        router: MutableList<ai.txai.database.location.Point>,
        vehicle: VehicleIndicator,
        duration: Int,
        color: Int,
        tag: String,
        top: Int,
        left: Int,
        bottom: Int,
        right: Int,
        person: ai.txai.database.location.Point?,
        listener: View.OnClickListener?
    ) {
        var formatVehicleId = formatVehicleId(vehicle.id)
        var historyVehicle = vehicleMaps[formatVehicleId]
        if (historyVehicle == null) {
            drawRouter(MapBoxUtils.calculateRouter(router, vehicle.point), color)
            drawVehicle(vehicle)
            if (MapService.CHARGING_NOTICE == tag) {
                drawChargingNotice(vehicle, listener!!)
            } else if (MapService.RUNNING_NOTICE == tag) {
                drawRunningNotice(vehicle)
            }
            return
        }
        var srcPoint = historyVehicle.point
        var desPoint = vehicle.point
        var pointIndies = MapBoxUtils.calculateRouter(router, srcPoint, desPoint)

        if (pointIndies?.second == null || pointIndies.second.isEmpty()) {
            drawRouter(MapBoxUtils.calculateRouter(router, vehicle.point), color)
            drawVehicle(vehicle)
            if (MapService.CHARGING_NOTICE == tag) {
                drawChargingNotice(vehicle, listener!!)
            } else if (MapService.RUNNING_NOTICE == tag) {
                drawRunningNotice(vehicle)
            }
            return
        }

        val itemDuration = duration / (pointIndies.second.size - 1)
        val list = mutableListOf<ValueAnimator>()

        for (i in 0 until pointIndies.second.size - 1) {
            val subList = ArrayList(router.subList(pointIndies.first + i, router.size))
            if (i == 0) {
                val arrayList = ArrayList(subList)
                person?.let {
                    if (MapBoxUtils.distance(person, vehicle.point) < 10) {
                        arrayList.add(person)
                    }
                }
                center(arrayList, top, left, bottom, right, null)
            }
            list.add(
                routerAnimator(
                    subList, vehicle, pointIndies.second[i], pointIndies.second[i + 1],
                    color, itemDuration, tag, listener
                )
            )
        }

        if (list.isEmpty()) {
            return
        }
        animator?.cancel()
        animator = AnimatorSet().apply {
            playSequentially(*list.toTypedArray())
            start()
        }

    }

    override fun drawRouter(
        router: MutableList<ai.txai.database.location.Point>,
        vehicle: VehicleIndicator,
        duration: Int,
        color: Int,
        tag: String,
        top: Int,
        left: Int,
        bottom: Int,
        right: Int,
        listener: View.OnClickListener?
    ) {
        var formatVehicleId = formatVehicleId(vehicle.id)
        var historyVehicle = vehicleMaps[formatVehicleId]
        if (historyVehicle == null) {
            drawRouter(MapBoxUtils.calculateRouter(router, vehicle.point), color)
            drawVehicle(vehicle)
            if (MapService.CHARGING_NOTICE == tag) {
                drawChargingNotice(vehicle, listener!!)
            } else if (MapService.RUNNING_NOTICE == tag) {
                drawRunningNotice(vehicle)
            }
            return
        }
        var srcPoint = historyVehicle.point
        var desPoint = vehicle.point
        var pointIndies = MapBoxUtils.calculateRouter(router, srcPoint, desPoint)

        if (pointIndies?.second == null || pointIndies.second.isEmpty()) {
            drawRouter(MapBoxUtils.calculateRouter(router, vehicle.point), color)
            drawVehicle(vehicle)
            if (MapService.CHARGING_NOTICE == tag) {
                drawChargingNotice(vehicle, listener!!)
            } else if (MapService.RUNNING_NOTICE == tag) {
                drawRunningNotice(vehicle)
            }
            return
        }

        val itemDuration = duration / (pointIndies.second.size - 1)
        val list = mutableListOf<ValueAnimator>()

        for (i in 0 until pointIndies.second.size - 1) {
            val subList = ArrayList(router.subList(pointIndies.first + i, router.size))
            if (i == 0) {
                center(subList, top, left, bottom, right, null)
            }
            list.add(
                routerAnimator(
                    subList, vehicle, pointIndies.second[i], pointIndies.second[i + 1],
                    color, itemDuration, tag, listener
                )
            )
        }
        if (list.isEmpty()) {
            return
        }
        animator?.cancel()
        animator = AnimatorSet().apply {
            playSequentially(*list.toTypedArray())
            start()
        }
    }

    private fun routerAnimator(
        router: MutableList<ai.txai.database.location.Point>,
        vehicle: VehicleIndicator,
        pointSrc: ai.txai.database.location.Point,
        pointDes: ai.txai.database.location.Point,
        color: Int,
        duration: Int,
        tag: String,
        listener: View.OnClickListener?
    ): ValueAnimator {
        val pointEvaluator =
            TypeEvaluator<ai.txai.database.location.Point> { fraction, startValue, endValue ->
                ai.txai.database.location.Point(
                    startValue.longitude + fraction * (endValue.longitude - startValue.longitude),
                    startValue.latitude + fraction * (endValue.latitude - startValue.latitude)
                )
            }
        var bearing = MapBoxUtils.bearing(pointSrc, pointDes)
        var animator = ValueAnimator().apply {
            setObjectValues(
                pointSrc,
                pointDes
            )
            setEvaluator(pointEvaluator)
            addUpdateListener {
                val currentPoint = it.animatedValue as ai.txai.database.location.Point
                if (router.size > 0) {
                    router.removeAt(0)
                }
                router.add(0, currentPoint)
                drawRouter(router, color)
                vehicle.point = currentPoint
                drawVehicle(vehicle, bearing)
                if (MapService.CHARGING_NOTICE == tag) {
                    drawChargingNotice(vehicle, listener!!)
                } else if (MapService.RUNNING_NOTICE == tag) {
                    drawRunningNotice(vehicle)
                }
            }
            this.duration = duration.toLong()
        }
        return animator
    }

    override fun drawView(
        tag: String,
        layoutRes: Int,
        point: ai.txai.database.location.Point,
        offset: List<Double>,
        clickListener: View.OnClickListener?
    ): View {
        return drawView(tag, layoutRes, point, offset, false, clickListener)
    }

    private fun drawView(
        tag: String,
        layoutRes: Int,
        point: ai.txai.database.location.Point,
        offset: List<Double>,
        selected: Boolean,
        clickListener: View.OnClickListener?
    ): View {
        val viewAnnotationManager = mapView.viewAnnotationManager
        val formatViewTag = formatViewTag(tag)
        val options = viewAnnotationOptions {
            geometry(Point.fromLngLat(point.longitude, point.latitude))
            offsetX(offset[0].toInt())
            offsetY(offset[1].toInt())
            allowOverlap(true)
            if (selected) {
                selected(selected)
            }
        }
        if (viewMaps.containsKey(formatViewTag)) {
            val view = viewMaps[formatViewTag]
            viewAnnotationManager.updateViewAnnotation(view!!, options)
            return view
        }
        val viewAnnotation = viewAnnotationManager.addViewAnnotation(
            resId = layoutRes,
            options = options
        )
        viewAnnotation.setDebounceClickListener { clickListener?.onClick(viewAnnotation) }
        viewMaps[formatViewTag] = viewAnnotation
        return viewAnnotation
    }

    override fun zoom(zoom: Double) {
        mapboxMap.setCamera(CameraOptions.Builder().zoom(zoom).build())
    }

    override fun center(
        point: ai.txai.database.location.Point,
        animatorListener: Animator.AnimatorListener?
    ) {
        innerCenter(Point.fromLngLat(point.longitude, point.latitude), animatorListener)
    }

    override fun center(
        point: MutableList<ai.txai.database.location.Point>,
        top: Int,
        left: Int,
        bottom: Int,
        right: Int,
        animatorListener: Animator.AnimatorListener?
    ) {
        val mapboxPoints = MapBoxUtils.transferToMapboxPoints(point)
        if (mapboxPoints.size <= 0) {
            return
        }
        var camera: CameraOptions? = null
        if (mapboxPoints.size == 1) {
            camera = cameraOptions {
                center(mapboxPoints[0])
                padding(
                    EdgeInsets(
                        top.toDouble(),
                        left.toDouble(),
                        bottom.toDouble(),
                        right.toDouble()
                    )
                )
                zoom(14.5)
            }
        } else {
            camera = mapboxMap.cameraForCoordinates(
                mapboxPoints,
                EdgeInsets(
                    top.toDouble(),
                    left.toDouble(),
                    bottom.toDouble(),
                    right.toDouble()
                ),
                0.0, 0.0
            )
        }

        LOG.i("MapBoxService", "center %s - %s", mapboxPoints.size, camera!!.toString())
        mapboxMap.easeTo(
            camera!!,
            mapAnimationOptions {
                duration(500)
                interpolator(AccelerateInterpolator())
                animatorListener?.let {
                    animatorListener(animatorListener)
                }
            }
        )
    }

    override fun centerWithAnimation(
        point: ai.txai.database.location.Point,
        animatorListener: Animator.AnimatorListener
    ) {
        mapboxMap.easeTo(
            cameraOptions {
                center(Point.fromLngLat(point.longitude, point.latitude))
            },
            mapAnimationOptions {
                duration(500)
                interpolator(AccelerateInterpolator())
                animatorListener(animatorListener)
            }
        )
    }

    private fun innerCenter(point: Point, animatorListener: Animator.AnimatorListener?) {
        mapboxMap.easeTo(
            cameraOptions {
                center(point)
            },
            mapAnimationOptions {
                duration(0)
                interpolator(AccelerateInterpolator())
                animatorListener?.let {
                    animatorListener(animatorListener)
                }
            }
        )
    }

    override fun gestureFocalPoint(point: ai.txai.database.location.Point) {
        gesturesPlugin.focalPoint =
            mapboxMap.pixelForCoordinate(Point.fromLngLat(point.longitude, point.latitude))
    }

    override fun drawPersonRoute(from: Point, to: Point) {
        val routeOptions = RouteOptions
            .builder()
            .coordinatesList(listOf(from, to))
            .overview(DirectionsCriteria.OVERVIEW_FULL)
            .profile(DirectionsCriteria.PROFILE_WALKING)
            .build()

        var context = AndroidUtils.getApplicationContext()
        var stringRes =
            context.resources.getIdentifier("mapbox_access_token", "string", context.packageName)
        val client = MapboxDirections.builder()
            .routeOptions(routeOptions)
            .accessToken(
                context.getString(stringRes)
            )
            .build()

        client.enqueueCall(object : retrofit2.Callback<DirectionsResponse> {
            override fun onResponse(
                call: Call<DirectionsResponse>,
                response: Response<DirectionsResponse>
            ) {
                if (response.body() == null) {
                    return
                } else if (response.body()?.routes()?.size ?: 0 < 1) {
                    return
                }
                var route = response.body()?.routes()?.get(0)
                route?.let {
                    mapboxMap.getStyle() { st ->
                        initPersonRouteLayer(st)

                        val directionsRouteFeatureList: MutableList<Feature> = ArrayList()
                        val lineString = LineString.fromPolyline(route.geometry()!!, PRECISION_6)
                        val coordinates = lineString.coordinates()
                        for (i in coordinates.indices) {
                            directionsRouteFeatureList.add(
                                Feature.fromGeometry(
                                    LineString.fromLngLats(
                                        coordinates
                                    )
                                )
                            )
                        }

                        st.getSourceAs<GeoJsonSource>(MapService.SOURCE_ROUTER_PERSON)?.apply {
                            feature(
                                Feature.fromGeometry(
                                    lineString
                                )
                            )
                        }
                    }
                }
            }

            override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
            }
        })
    }

    override fun logoBottom(bottomHeight: Int) {
        mapView.logo.marginBottom = bottomHeight.toFloat() + SizeUtils.dp2px(10f)
    }

    private fun initPersonRouteLayer(style: Style) {
        if (!style.styleSourceExists(MapService.SOURCE_ROUTER_PERSON)) {
            style.addSource(geoJsonSource(MapService.SOURCE_ROUTER_PERSON))
        }
        if (!style.styleLayerExists(MapService.LAYER_ROUTER_PERSON)) {
            style.addLayerBelow(
                lineLayer(MapService.LAYER_ROUTER_PERSON, MapService.SOURCE_ROUTER_PERSON) {
                    lineDasharray(listOf(1.0, 2.0))
                    lineColor(Color.parseColor("#6592FF"))
                    lineTranslate(listOf(0.0, 4.0))
                    lineCap(LineCap.ROUND)
                    lineJoin(LineJoin.ROUND)
                    lineWidth(3.0)
                },
                "road-rail"
            )
        }
    }

    override fun drawPickUpSmall(point: ai.txai.database.location.Point) {
        drawMarker(
            MapService.MARKER_PICK_UP_SMALL,
            R.mipmap.biz_ic_pick_up_point_small,
            point,
            listOf(0.0, -20.0)
        )
    }

    override fun drawDropOffSmall(point: ai.txai.database.location.Point) {
        drawMarker(
            MapService.MARKER_DROP_OFF_SMALL,
            R.mipmap.biz_ic_drop_off_point_small,
            point,
            listOf(0.0, -20.0)
        )
    }

    /**
     * white background whit more icon
     */
    override fun drawPickUpNameV2(site: Site, clickListener: View.OnClickListener?) {
        val viewPickup = drawView(
            MapService.NOTICE_PICK_UP,
            R.layout.biz_item_location_notice_text,
            site.point,
            listOf(0.0, 170.0),
            clickListener
        )

        val namePickUp = viewPickup.findViewById<TextView>(R.id.site_name)
        namePickUp.text = site.name
        if (clickListener == null) {
            viewPickup.findViewById<View>(R.id.iv_more).visibility = View.GONE
        }
    }

    /**
     * white background whit more icon
     */
    override fun drawDropOffNameV2(site: Site, clickListener: View.OnClickListener?) {
        val viewPickup = drawView(
            MapService.NOTICE_DROP_OFF,
            R.layout.biz_item_location_notice_text,
            site.point,
            listOf(0.0, 170.0),
            clickListener
        )

        val namePickUp = viewPickup.findViewById<TextView>(R.id.site_name)
        namePickUp.text = site.name
        if (clickListener == null) {
            viewPickup.findViewById<View>(R.id.iv_more).visibility = View.GONE
        }
    }

    /**
     * only name with transparent background
     */
    override fun drawPickUpName(site: Site) {
        val widthString14sp = MapBoxUtils.widthString14sp(site.name)
        val viewPickup = drawView(
            MapService.NAME_PICK_UP,
            R.layout.biz_item_name_only,
            site.point,
            listOf(widthString14sp / 2.0 + 40, 20.0)
        ) { }

        val namePickUp = viewPickup.findViewById<StrokeTextView>(R.id.site_name)
        namePickUp.setText(site.name)
    }

    override fun drawPickUpNameNoOffset(site: Site) {
        val viewPickup = drawView(
            MapService.NAME_PICK_UP_NO_OFFSET,
            R.layout.biz_item_name_only,
            site.point,
            listOf(0.0, -30.0)
        ) { }

        val namePickUp = viewPickup.findViewById<StrokeTextView>(R.id.site_name)
        namePickUp.setText(site.name)
    }

    /**
     * only name with transparent background
     */
    override fun drawDropOffName(site: Site) {
        val widthString14sp = MapBoxUtils.widthString14sp(site.name)
        val viewPickup = drawView(
            MapService.NAME_DROP_OFF,
            R.layout.biz_item_name_only,
            site.point,
            listOf(widthString14sp / 2.0 + 40, 20.0)
        ) { }

        val namePickUp = viewPickup.findViewById<StrokeTextView>(R.id.site_name)
        namePickUp.setText(site.name)
    }

    override fun ringPoint(point: ai.txai.database.location.Point) {
        mapboxMap.getStyle() { style ->
            initLocationIndicator(style)
            val layer = style.getLayer(MapService.LAYER_CIRCLE) as LocationIndicatorLayer

            layer.location(
                listOf(
                    point.latitude,
                    point.longitude,
                    0.0
                )
            )
            if (puckPulsingAnimator == null) {
                puckPulsingAnimator = PuckPulsingAnimator()
            }
            puckPulsingAnimator?.setLocationLayerRenderer(layer)
            puckPulsingAnimator?.animateInfinite()
        }
    }

    override fun onFinish() {
        puckPulsingAnimator?.cancelRunning()
        countDownTimer?.cancel()
        centerAnimator?.cancel()
        animator?.cancel()
    }

    private fun initLocationIndicator(style: Style) {
        ensureVehicleAnnotationManager(style)
        style.addLayerBelow(
            locationIndicatorLayer(MapService.LAYER_CIRCLE) {
                imagePitchDisplacement(5.0)
                topImageSize(1.5)
            },
            MapService.VEHICLE_LAYER
        )
    }

    override fun toCurrentPoint(listener: Animator.AnimatorListener): ai.txai.database.location.Point? {
        currentPoint?.let {
            val point = ai.txai.database.location.Point(it.longitude(), it.latitude())
            mapboxMap.flyTo(
                cameraOptions {
                    center(it)
                    zoom(14.0)
                },
                mapAnimationOptions {
                    duration(1000)
                    interpolator(AccelerateInterpolator())
                    animatorListener(listener)
                }
            )
            return point
        }

        return null
    }

    override fun drawCountdown(site: Site, time: Int, tickListener: OnCountDownTickListener) {
        val viewAnnotation = drawView(
            MapService.WAITING_COUNT_DOWN,
            R.layout.biz_item_waiting_countdown,
            site.point,
            listOf(0.0, 80.0)
        ) {

        }
        var nameTv = viewAnnotation.findViewById<TextView>(R.id.tv_time)
        nameTv.text = FormatUtils.buildTimeGapNoUnit(time)
        countDownTimer?.cancel()
        countDownTimer = CountDownTimerManager.startCountdown(
            time.toLong(),
            TimeUnit.SECONDS,
            60,
            object : OnCountDownTickListener {
                override fun onTick(time: Long) {
                    nameTv.post { nameTv.text = time.toString() }
                }

                override fun onFinish() {
                    LOG.i("MapBoxService", "CountDownTimer finish")
                    tickListener?.onFinish()
                    countDownTimer = null
                }
            })
        center(site.point, null)
    }

    override fun drawWaitingWhenArrived(time: Int, site: Site) {
        val viewAnnotation = drawView(
            MapService.WAITING_WHEN_ARRIVED,
            R.layout.biz_item_waiting_when_arrived,
            site.point,
            listOf(0.0, 150.0)
        ) {

        }
        var timeTv = viewAnnotation.findViewById<TextView>(R.id.tv_time)
        timeTv.text = FormatUtils.buildTimeGapNoUnit(time)
        countDownTimer?.cancel()
        countDownTimer = CountDownTimerManager.startCountdown(
            time.toLong(),
            TimeUnit.SECONDS,
            60,
            object : OnCountDownTickListener {
                override fun onTick(time: Long) {
                    timeTv.post { timeTv.text = time.toString() }
                }

                override fun onFinish() {
                    countDownTimer = null
                }
            })
        center(site.point, null)
    }

    override fun drawChargingNotice(
        vehicleIndicator: VehicleIndicator,
        listener: View.OnClickListener
    ) {
        val viewAnnotation = drawView(
            MapService.CHARGING_NOTICE,
            R.layout.biz_item_charging_notice,
            vehicleIndicator.point,
            listOf(0.0, 150.0)
        ) {
            listener.onClick(it)
        }

        var distanceTv = viewAnnotation.findViewById<TextView>(R.id.est_distance)
        var timeTv = viewAnnotation.findViewById<TextView>(R.id.est_time)
        var amountTv = viewAnnotation.findViewById<TextView>(R.id.tv_aed_amount)
        var dismountTv = viewAnnotation.findViewById<TextView>(R.id.tv_aed_dismount)
        distanceTv.text = FormatUtils.buildDistance(vehicleIndicator.emt)
        timeTv.text = FormatUtils.buildTimeGap(vehicleIndicator.eta)
        amountTv.text = mapView.context.getString(
            R.string.biz_aed_with_amount,
            AndroidUtils.buildAmount(vehicleIndicator.totalFare - vehicleIndicator.discountFare)
        )
        var discount = AndroidUtils.buildAmount(vehicleIndicator.discountFare)
        if (vehicleIndicator.discountFare == 0.0) {
            dismountTv.visibility = View.GONE
        } else {
            dismountTv.visibility = View.VISIBLE
        }
        dismountTv.text = mapView.context.getString(
            R.string.biz_aed_with_amount,
            discount
        )
        dismountTv.paint.flags = Paint.STRIKE_THRU_TEXT_FLAG
    }

    override fun drawEstRouteNotice(
        point: ai.txai.database.location.Point,
        eta: Int,
        emt: Double
    ) {
        val viewAnnotation = drawView(
            MapService.ROUTE_NOTICE,
            R.layout.biz_item_est_notice,
            point,
            listOf(0.0, 150.0)
        ) {

        }
        var distanceTv = viewAnnotation?.findViewById<TextView>(R.id.est_distance)
        var timeTv = viewAnnotation?.findViewById<TextView>(R.id.est_time)
        distanceTv?.text = String.format("Route ${FormatUtils.buildDistance(emt)}")
        timeTv?.text = String.format("ETA ${FormatUtils.buildTimeGap(eta)}")
    }

    override fun drawRunningNotice(vehicleIndicator: VehicleIndicator) {
        val viewAnnotation = drawView(
            MapService.RUNNING_NOTICE,
            R.layout.biz_item_running_notice,
            vehicleIndicator.point,
            listOf(0.0, 150.0),
            true
        ) {}
        var distanceTv = viewAnnotation.findViewById<TextView>(R.id.est_distance)
        var timeTv = viewAnnotation.findViewById<TextView>(R.id.est_time)
        distanceTv.text = FormatUtils.buildDistance(vehicleIndicator.emt)
        timeTv.text = FormatUtils.buildTimeGap(vehicleIndicator.eta)
    }

    override fun drawFillWithLine(points: List<ai.txai.database.location.Point>) {
        initFillAndLine()
        mapboxMap.getStyle() {
            it.getSourceAs<GeoJsonSource>(MapService.SOURCE_FILL)?.apply {
                feature(
                    Feature.fromGeometry(
                        Polygon.fromLngLats(
                            mutableListOf(
                                MapBoxUtils.transferToMapboxPoints(
                                    points
                                )
                            )
                        )
                    )
                )
            }
        }

        mapboxMap.getStyle() {
            it.getSourceAs<GeoJsonSource>(MapService.SOURCE_FILL_FRAME)?.apply {
                feature(
                    Feature.fromGeometry(
                        LineString.fromLngLats(
                            MapBoxUtils.transferToMapboxPoints(
                                points
                            )
                        )
                    )
                )
            }
        }
    }

    private fun initFillAndLine() {
        mapboxMap.getStyle() {
            if (!it.styleSourceExists(MapService.SOURCE_FILL)) {
                it.addSource(geoJsonSource(MapService.SOURCE_FILL))
            }
            if (!it.styleLayerExists(MapService.LAYER_FILL)) {
                it.addLayerBelow(
                    fillLayer(MapService.LAYER_FILL, MapService.SOURCE_FILL) {
                        fillColor(Color.parseColor("#FF7A38")).fillOpacity(0.1)
                    },
                    "road-rail"
                )
            }

            if (!it.styleSourceExists(MapService.SOURCE_FILL_FRAME)) {
                it.addSource(geoJsonSource(MapService.SOURCE_FILL_FRAME))
            }

            if (!it.styleLayerExists(MapService.LAYER_FILL_FRAME)) {
                it.addLayer(
                    lineLayer(MapService.LAYER_FILL_FRAME, MapService.SOURCE_FILL_FRAME) {
                        lineDasharray(listOf(1.0, 2.0))
                        lineColor(Color.parseColor("#FF7A38"))
                        lineCap(LineCap.ROUND)
                        lineJoin(LineJoin.ROUND)
                        lineWidth(3.0)
                    }
                )
            }
        }
    }

    override fun hideFillWithLine() {
        mapboxMap.getStyle {
            it.getLayer(MapService.LAYER_FILL)?.visibility(Visibility.NONE)
//            it.getLayer(MapService.LAYER_FILL_FRAME)?.visibility(Visibility.NONE)
        }
    }
}