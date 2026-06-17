package com.example.uzradyab.presentation.geofence

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            OsmdroidConfig.configure(context)
            MapView(context).apply {
                val tileSource = TileSourceRegistry.resolve("osm")
                setTileSource(tileSource)
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
        },
        update = { mapView ->
            // Update tile source if changed
            val tileSource = TileSourceRegistry.resolve(state.mapStyle)
            if (mapView.tileProvider.tileSource != tileSource) {
                mapView.setTileSource(tileSource)
                mapView.setMaxZoomLevel(tileSource.maximumZoomLevel.toDouble())
            }

            // Update center first time
            val isFirstLoad = mapView.tag == null
            if (isFirstLoad) {
                mapView.tag = "loaded"
                val centerLat = state.devicePosition?.latitude ?: 35.6892
                val centerLon = state.devicePosition?.longitude ?: 51.3890
                mapView.controller.setZoom(14.0)
                mapView.controller.setCenter(GeoPoint(centerLat, centerLon))
            }

            // Clear old polygons and polylines
            mapView.overlays.removeAll { it is Polygon || it is Polyline }

            // Draw existing geofences
            state.geofences.forEach { geofence ->
                when (val shape = geofence.shape) {
                    is GeofenceShape.Circle -> {
                        val circle = Polygon(mapView).apply {
                            points = Polygon.pointsAsCircle(GeoPoint(shape.lat, shape.lon), shape.radius)
                            fillPaint.color = AndroidColor.argb(50, 0, 150, 255)
                            outlinePaint.color = AndroidColor.argb(200, 0, 150, 255)
                            outlinePaint.strokeWidth = 2f
                        }
                        mapView.overlays.add(circle)
                    }
                    is GeofenceShape.Polygon -> {
                        val poly = Polygon(mapView).apply {
                            points = shape.points.map { GeoPoint(it.first, it.second) }
                            fillPaint.color = AndroidColor.argb(50, 0, 150, 255)
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
                            fillPaint.color = AndroidColor.argb(80, 255, 0, 0)
                            outlinePaint.color = AndroidColor.argb(200, 255, 0, 0)
                            outlinePaint.strokeWidth = 3f
                        }
                        mapView.overlays.add(newCircle)
                    }
                    DrawMode.POLYGON -> {
                        val poly = Polygon(mapView).apply {
                            points = activeGeoPoints
                            fillPaint.color = AndroidColor.argb(80, 255, 0, 0)
                            outlinePaint.color = AndroidColor.argb(200, 255, 0, 0)
                            outlinePaint.strokeWidth = 3f
                        }
                        mapView.overlays.add(poly)
                        // Show points as markers or small circles
                        activeGeoPoints.forEach { pt ->
                            val ptCircle = Polygon(mapView).apply {
                                points = Polygon.pointsAsCircle(pt, 10.0) // small radius
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
                                points = Polygon.pointsAsCircle(pt, 10.0) // small radius
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
    )
}
