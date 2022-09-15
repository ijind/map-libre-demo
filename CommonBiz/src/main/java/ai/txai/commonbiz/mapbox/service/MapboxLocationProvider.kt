package ai.txai.commonbiz.mapbox.service

import ai.txai.commonbiz.mapbox.LocationCompassEngine
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Handler
import android.os.Looper
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.android.core.location.LocationEngineRequest
import com.mapbox.android.core.location.LocationEngineResult
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.common.Logger
import com.mapbox.geojson.Point
import com.mapbox.maps.plugin.locationcomponent.LocationComponentConstants
import com.mapbox.maps.plugin.locationcomponent.LocationConsumer
import com.mapbox.maps.plugin.locationcomponent.LocationProvider
import com.mapbox.turf.TurfMeasurement
import java.lang.ref.WeakReference
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Default Location Provider implementation, it can be overwritten by users.
 */
internal class MapboxLocationProvider(
    context: Context,
    private val locationCompassEngine: LocationCompassEngine = LocationCompassEngine(
        context
    )
) :
    LocationProvider,
    LocationEngineCallback<LocationEngineResult>,
    LocationCompassEngine.CompassListener {
    private val contextWeekRef: WeakReference<Context> = WeakReference(context)
    private val locationEngine = LocationEngineProvider.getBestLocationEngine(context)
    private val locationEngineRequest =
        LocationEngineRequest.Builder(LocationComponentConstants.DEFAULT_INTERVAL_MILLIS)
            .setFastestInterval(LocationComponentConstants.DEFAULT_FASTEST_INTERVAL_MILLIS)
            .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
            .build()

    private val locationConsumers = CopyOnWriteArrayList<LocationConsumer>()
    private var handler: Handler? = null
    private lateinit var runnable: Runnable
    private var updateDelay = INIT_UPDATE_DELAY

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdates() {
        if (PermissionsManager.areLocationPermissionsGranted(contextWeekRef.get())) {
            locationEngine.requestLocationUpdates(
                locationEngineRequest, this, Looper.getMainLooper()
            )
        } else {
            if (handler == null) {
                handler = Handler()
                runnable = Runnable { requestLocationUpdates() }
            }
            if (updateDelay * 2 < MAX_UPDATE_DELAY) {
                updateDelay *= 2
            } else {
                updateDelay = MAX_UPDATE_DELAY
            }
            handler?.postDelayed(runnable, updateDelay)
            Logger.w(
                TAG,
                "Missing location permission, location component will not take effect before location permission is granted."
            )
        }
    }

    private fun notifyLocationUpdates(location: Location) {
        locationConsumers.forEach { consumer ->
            consumer.onLocationUpdated(Point.fromLngLat(location.longitude, location.latitude))
            consumer.onBearingUpdated(location.bearing.toDouble())
        }
    }

    override fun onCompassChanged(userHeading: Float) {
        locationConsumers.forEach { consumer ->
            consumer.onBearingUpdated(userHeading.toDouble())
        }
    }

    /**
     * Invoked when new data available.
     *
     * @param result updated data.
     */
    override fun onSuccess(result: LocationEngineResult) {
        result.lastLocation?.let {
            notifyLocationUpdates(it)
        }
    }

    /**
     * Invoked when engine exception occurs.
     *
     * @param exception
     */
    override fun onFailure(exception: Exception) {
        Logger.e(
            TAG,
            "Failed to obtain location update: $exception"
        )
    }

    /**
     * Register the location consumer to the Location Provider.
     *
     * The Location Consumer will get location and bearing updates from the Location Provider.
     *
     * @param locationConsumer
     */
    @SuppressLint("MissingPermission")
    override fun registerLocationConsumer(locationConsumer: LocationConsumer) {
        if (locationConsumers.isEmpty()) {
            requestLocationUpdates()

            // Start to listen compass change in HEADING mode.
            locationCompassEngine.addCompassListener(this)
        }
        locationConsumers.add(locationConsumer)
        if (PermissionsManager.areLocationPermissionsGranted(contextWeekRef.get())) {
            locationEngine.getLastLocation(this)
        } else {
            Logger.w(
                TAG,
                "Missing location permission, location component will not take effect before location permission is granted."
            )
        }
    }

    @SuppressLint("MissingPermission")
    fun getLastLocation(callback: LocationEngineCallback<LocationEngineResult?>) {
        if (PermissionsManager.areLocationPermissionsGranted(contextWeekRef.get())) {
            locationEngine.getLastLocation(callback)
        }
    }

    /**
     * Unregister the location consumer from the Location Provider.
     *
     * @param locationConsumer
     */
    override fun unRegisterLocationConsumer(locationConsumer: LocationConsumer) {
        locationConsumers.remove(locationConsumer)
        if (locationConsumers.isEmpty()) {
            locationEngine.removeLocationUpdates(this)
            handler?.removeCallbacks(runnable)

            // Stop listening compass change when no consumer is registered to save power.
            locationCompassEngine.removeCompassListener(this)
        }
    }


    private companion object {
        private const val TAG = "MapboxLocationProvider"
        private const val INIT_UPDATE_DELAY = 100L
        private const val MAX_UPDATE_DELAY = 5000L
    }
}