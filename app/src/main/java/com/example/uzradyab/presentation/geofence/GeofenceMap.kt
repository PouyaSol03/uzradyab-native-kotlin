package com.example.uzradyab.presentation.geofence

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.uzradyab.map.OsmdroidConfig
import com.example.uzradyab.map.tile.TileSourceRegistry
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.Polyline
import com.example.uzradyab.domain.model.GeofenceShape

@Composable
fun GeofenceMap(
    state: GeofenceState,
    onMapClick: (Double, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // 1. Initialize the MapView ONCE
    val mapView = remember {
        OsmdroidConfig.configure(context)
        MapView(context).apply {
            setMultiTouchControls(true)
            setBuiltInZoomControls(false)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)

            // Add MapEventsOverlay to intercept clicks
            val mapEventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
                override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                    p?.let { onMapClick(it.latitude, it.longitude) }
                    return true
                }
                override fun longPressHelper(p: GeoPoint?): Boolean {
                    return false
                }
            })
            overlays.add(mapEventsOverlay)
        }
    }

    // 2. Set center on first load only
    LaunchedEffect(state.devicePosition) {
        if (mapView.tag == null && state.devicePosition != null) {
            mapView.tag = "loaded"
            mapView.controller.setZoom(14.0)
            mapView.controller.setCenter(
                GeoPoint(state.devicePosition.latitude, state.devicePosition.longitude)
            )
        }
    }

    // 3. Draw Shapes ONLY when relevant data changes (Fixes the Lag)
    LaunchedEffect(
        state.geofences,
        state.addingMode,
        state.activeDrawingPoints,
        state.newGeofenceRadius,
        state.drawMode
    ) {
        // Clear old polygons and polylines
        mapView.overlays.removeAll { it is Polygon || it is Polyline }

        // Draw existing geofences
        state.geofences.forEach { geofence ->
            when (val shape = geofence.shape) {
                is GeofenceShape.Circle -> {
                    val circle = Polygon(mapView).apply {
                        points = Polygon.pointsAsCircle(GeoPoint(shape.lat, shape.lon), shape.radius)
                        fillPaint.color = AndroidColor.argb(25, 0, 150, 255)
                        outlinePaint.color = AndroidColor.argb(200, 0, 150, 255)
                        outlinePaint.strokeWidth = 2f
                    }
                    mapView.overlays.add(circle)
                }
                is GeofenceShape.Polygon -> {
                    val poly = Polygon(mapView).apply {
                        points = shape.points.map { GeoPoint(it.first, it.second) }
                        fillPaint.color = AndroidColor.argb(25, 0, 150, 255)
                        outlinePaint.color = AndroidColor.argb(200, 0, 150, 255)
                        outlinePaint.strokeWidth = 2f
                    }
                    mapView.overlays.add(poly)
                }
                is GeofenceShape.LineString -> {
                    val line = Polyline(mapView).apply {
                        setPoints(shape.points.map { GeoPoint(it.first, it.second) })
                        outlinePaint.color = AndroidColor.argb(200, 0, 150, 255)
                        outlinePaint.strokeWidth = 4f
                    }
                    mapView.overlays.add(line)
                }
                else -> {}
            }
        }

        // Draw new active geofence
        if (state.addingMode && state.activeDrawingPoints.isNotEmpty()) {
            val activeGeoPoints = state.activeDrawingPoints.map { GeoPoint(it.first, it.second) }
            when (state.drawMode) {
                DrawMode.CIRCLE -> {
                    val pt = activeGeoPoints.first()
                    val newCircle = Polygon(mapView).apply {
                        points = Polygon.pointsAsCircle(pt, state.newGeofenceRadius)
                        fillPaint.color = AndroidColor.argb(35, 255, 0, 0)
                        outlinePaint.color = AndroidColor.argb(200, 255, 0, 0)
                        outlinePaint.strokeWidth = 3f
                    }
                    mapView.overlays.add(newCircle)
                }
                DrawMode.POLYGON -> {
                    val poly = Polygon(mapView).apply {
                        points = activeGeoPoints
                        fillPaint.color = AndroidColor.argb(35, 255, 0, 0)
                        outlinePaint.color = AndroidColor.argb(200, 255, 0, 0)
                        outlinePaint.strokeWidth = 3f
                    }
                    mapView.overlays.add(poly)

                    activeGeoPoints.forEach { pt ->
                        val ptCircle = Polygon(mapView).apply {
                            points = Polygon.pointsAsCircle(pt, 10.0)
                            fillPaint.color = AndroidColor.RED
                            outlinePaint.color = AndroidColor.WHITE
                        }
                        mapView.overlays.add(ptCircle)
                    }
                }
                DrawMode.LINESTRING -> {
                    val line = Polyline(mapView).apply {
                        setPoints(activeGeoPoints)
                        outlinePaint.color = AndroidColor.argb(200, 255, 0, 0)
                        outlinePaint.strokeWidth = 5f
                    }
                    mapView.overlays.add(line)

                    activeGeoPoints.forEach { pt ->
                        val ptCircle = Polygon(mapView).apply {
                            points = Polygon.pointsAsCircle(pt, 10.0)
                            fillPaint.color = AndroidColor.RED
                            outlinePaint.color = AndroidColor.WHITE
                        }
                        mapView.overlays.add(ptCircle)
                    }
                }
            }
        }


        mapView.invalidate()
    }

    // 4. Animate to Selected Geofence
    LaunchedEffect(state.selectedGeofenceId) {
        val selectedId = state.selectedGeofenceId
        if (selectedId != null) {
            val targetGeofence = state.geofences.find { it.id == selectedId }

            // Extract the center point based on the shape type
            val targetPoint = when (val shape = targetGeofence?.shape) {
                is GeofenceShape.Circle -> GeoPoint(shape.lat, shape.lon)
                is GeofenceShape.Polygon -> shape.points.firstOrNull()?.let { GeoPoint(it.first, it.second) }
                is GeofenceShape.LineString -> shape.points.firstOrNull()?.let { GeoPoint(it.first, it.second) }
                else -> null
            }

            // If we found a valid point, animate the map to it
            targetPoint?.let { point ->
                // Mark map as loaded so device position initial centering doesn't override this
                mapView.tag = "loaded"
                // Set a better zoom in level
                mapView.controller.setZoom(16.5)
                mapView.controller.animateTo(point)
            }
        }
    }

    // 4. AndroidView purely for rendering the remembered instance
    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { mapView },
        update = { view ->
            // Keep Map Tile Source updates fast and simple here
            val tileSource = TileSourceRegistry.resolve(state.mapStyle)
            if (view.tileProvider.tileSource != tileSource) {
                view.setTileSource(tileSource)
                view.setMaxZoomLevel(tileSource.maximumZoomLevel.toDouble())
            }
        }
    )
}
