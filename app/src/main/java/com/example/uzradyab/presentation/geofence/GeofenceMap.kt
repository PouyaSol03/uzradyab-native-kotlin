package com.example.uzradyab.presentation.geofence

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.uzradyab.domain.model.GeofenceShape
import com.example.uzradyab.map.MapLibreStyles
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapLibreMapOptions
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.plugins.annotation.Circle
import org.maplibre.android.plugins.annotation.CircleManager
import org.maplibre.android.plugins.annotation.CircleOptions
import org.maplibre.android.plugins.annotation.Fill
import org.maplibre.android.plugins.annotation.FillManager
import org.maplibre.android.plugins.annotation.FillOptions
import org.maplibre.android.plugins.annotation.Line
import org.maplibre.android.plugins.annotation.LineManager
import org.maplibre.android.plugins.annotation.LineOptions
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private fun createCirclePoints(centerLat: Double, centerLon: Double, radiusMeters: Double, points: Int = 64): List<LatLng> {
    val earthRadius = 6371000.0 // meters
    val lat = Math.toRadians(centerLat)
    val lon = Math.toRadians(centerLon)
    val d = radiusMeters / earthRadius

    val result = mutableListOf<LatLng>()
    for (i in 0..points) {
        val bearing = 2 * PI * i / points
        val targetLat = Math.asin(sin(lat) * cos(d) + cos(lat) * sin(d) * cos(bearing))
        val targetLon = lon + Math.atan2(
            sin(bearing) * sin(d) * cos(lat),
            cos(d) - sin(lat) * sin(targetLat)
        )
        result.add(LatLng(Math.toDegrees(targetLat), Math.toDegrees(targetLon)))
    }
    return result
}

@Composable
fun GeofenceMap(
    state: GeofenceState,
    onMapClick: (Double, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var mapLibreMapRef by remember { mutableStateOf<MapLibreMap?>(null) }

    var fillManager by remember { mutableStateOf<FillManager?>(null) }
    var lineManager by remember { mutableStateOf<LineManager?>(null) }
    var circleManager by remember { mutableStateOf<CircleManager?>(null) }

    var geofenceFills by remember { mutableStateOf<List<Fill>>(emptyList()) }
    var geofenceLines by remember { mutableStateOf<List<Line>>(emptyList()) }
    var geofenceCircles by remember { mutableStateOf<List<Circle>>(emptyList()) }
    
    val tracker = remember { object {
        var lastMapStyle: String? = null
        var styleToken: Int = 0
    } }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapViewRef?.onStart()
                Lifecycle.Event.ON_RESUME -> mapViewRef?.onResume()
                Lifecycle.Event.ON_PAUSE -> mapViewRef?.onPause()
                Lifecycle.Event.ON_STOP -> mapViewRef?.onStop()
                Lifecycle.Event.ON_DESTROY -> mapViewRef?.onDestroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val isDarkTheme = isSystemInDarkTheme()

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = {
            val options = MapLibreMapOptions.createFromAttributes(it).textureMode(true)
            MapView(it, options).apply {
                mapViewRef = this
                onCreate(null)
                
                getMapAsync { map ->
                    mapLibreMapRef = map
                    
                    map.uiSettings.isCompassEnabled = false
                    map.uiSettings.isLogoEnabled = false
                    map.uiSettings.isAttributionEnabled = false
                    
                    tracker.lastMapStyle = state.mapStyle
                    tracker.styleToken++
                    val currentToken = tracker.styleToken
                    map.setStyle(Style.Builder().fromJson(MapLibreStyles.getStyleJson(state.mapStyle, isDarkTheme))) { style ->
                        if (currentToken != tracker.styleToken) return@setStyle
                        fillManager = FillManager(this, map, style)
                        lineManager = LineManager(this, map, style)
                        circleManager = CircleManager(this, map, style)
                    }

                    map.addOnMapClickListener { point ->
                        onMapClick(point.latitude, point.longitude)
                        true
                    }
                }
            }
        },
        update = { mapView ->
            val map = mapLibreMapRef ?: return@AndroidView
            
            // Style updates
            if (tracker.lastMapStyle != state.mapStyle) {
                tracker.lastMapStyle = state.mapStyle
                tracker.styleToken++
                val currentToken = tracker.styleToken
                
                fillManager?.onDestroy()
                lineManager?.onDestroy()
                circleManager?.onDestroy()
                
                geofenceFills = emptyList()
                geofenceLines = emptyList()
                geofenceCircles = emptyList()
                fillManager = null
                lineManager = null
                circleManager = null
                
                map.setStyle(Style.Builder().fromJson(MapLibreStyles.getStyleJson(state.mapStyle, isDarkTheme))) { newStyle ->
                    if (currentToken != tracker.styleToken) return@setStyle
                    fillManager = FillManager(mapView, map, newStyle)
                    lineManager = LineManager(mapView, map, newStyle)
                    circleManager = CircleManager(mapView, map, newStyle)
                }
            }

            // Set center on first load only
            val isFirstLoad = mapView.tag == null
            if (isFirstLoad && state.devicePosition != null) {
                mapView.tag = "loaded"
                map.cameraPosition = CameraPosition.Builder()
                    .target(LatLng(state.devicePosition.latitude, state.devicePosition.longitude))
                    .zoom(14.0)
                    .build()
            }

            // Animate to Selected Geofence
            val selectedId = state.selectedGeofenceId
            if (selectedId != null) {
                val targetGeofence = state.geofences.find { it.id == selectedId }
                val targetPoint = when (val shape = targetGeofence?.shape) {
                    is GeofenceShape.Circle -> LatLng(shape.lat, shape.lon)
                    is GeofenceShape.Polygon -> shape.points.firstOrNull()?.let { LatLng(it.first, it.second) }
                    is GeofenceShape.LineString -> shape.points.firstOrNull()?.let { LatLng(it.first, it.second) }
                    else -> null
                }
                targetPoint?.let { point ->
                    mapView.tag = "loaded"
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 16.5))
                }
            }

            // Draw Shapes
            map.getStyle { _ ->
                val fm = fillManager ?: return@getStyle
                val lm = lineManager ?: return@getStyle
                val cm = circleManager ?: return@getStyle
                
                // Clear old shapes
                fm.deleteAll()
                lm.deleteAll()
                cm.deleteAll()
                
                val fills = mutableListOf<FillOptions>()
                val lines = mutableListOf<LineOptions>()
                val circles = mutableListOf<CircleOptions>()

                // Draw existing geofences
                state.geofences.forEach { geofence ->
                    when (val shape = geofence.shape) {
                        is GeofenceShape.Circle -> {
                            val circlePoints = createCirclePoints(shape.lat, shape.lon, shape.radius)
                            fills.add(FillOptions()
                                .withLatLngs(listOf(circlePoints))
                                .withFillColor("#0096FF")
                                .withFillOpacity(0.1f)
                            )
                            lines.add(LineOptions()
                                .withLatLngs(circlePoints)
                                .withLineColor("#0096FF")
                                .withLineWidth(2f)
                            )
                        }
                        is GeofenceShape.Polygon -> {
                            val polyPoints = shape.points.map { LatLng(it.first, it.second) }
                            fills.add(FillOptions()
                                .withLatLngs(listOf(polyPoints))
                                .withFillColor("#0096FF")
                                .withFillOpacity(0.1f)
                            )
                            lines.add(LineOptions()
                                .withLatLngs(polyPoints)
                                .withLineColor("#0096FF")
                                .withLineWidth(2f)
                            )
                        }
                        is GeofenceShape.LineString -> {
                            val linePoints = shape.points.map { LatLng(it.first, it.second) }
                            lines.add(LineOptions()
                                .withLatLngs(linePoints)
                                .withLineColor("#0096FF")
                                .withLineWidth(4f)
                            )
                        }
                        else -> {}
                    }
                }

                // Draw new active geofence
                if (state.addingMode && state.activeDrawingPoints.isNotEmpty()) {
                    val activePoints = state.activeDrawingPoints.map { LatLng(it.first, it.second) }
                    
                    when (state.drawMode) {
                        DrawMode.CIRCLE -> {
                            val pt = activePoints.first()
                            val circlePoints = createCirclePoints(pt.latitude, pt.longitude, state.newGeofenceRadius)
                            fills.add(FillOptions()
                                .withLatLngs(listOf(circlePoints))
                                .withFillColor("#FF0000")
                                .withFillOpacity(0.15f)
                            )
                            lines.add(LineOptions()
                                .withLatLngs(circlePoints)
                                .withLineColor("#FF0000")
                                .withLineWidth(3f)
                            )
                        }
                        DrawMode.POLYGON -> {
                            fills.add(FillOptions()
                                .withLatLngs(listOf(activePoints))
                                .withFillColor("#FF0000")
                                .withFillOpacity(0.15f)
                            )
                            lines.add(LineOptions()
                                .withLatLngs(activePoints)
                                .withLineColor("#FF0000")
                                .withLineWidth(3f)
                            )
                            activePoints.forEach {
                                circles.add(CircleOptions()
                                    .withLatLng(it)
                                    .withCircleColor("#FF0000")
                                    .withCircleStrokeColor("#FFFFFF")
                                    .withCircleStrokeWidth(2f)
                                    .withCircleRadius(6f)
                                )
                            }
                        }
                        DrawMode.LINESTRING -> {
                            lines.add(LineOptions()
                                .withLatLngs(activePoints)
                                .withLineColor("#FF0000")
                                .withLineWidth(5f)
                            )
                            activePoints.forEach {
                                circles.add(CircleOptions()
                                    .withLatLng(it)
                                    .withCircleColor("#FF0000")
                                    .withCircleStrokeColor("#FFFFFF")
                                    .withCircleStrokeWidth(2f)
                                    .withCircleRadius(6f)
                                )
                            }
                        }
                    }
                }

                fm.create(fills)
                lm.create(lines)
                cm.create(circles)
            }
        }
    )
}
