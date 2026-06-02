package com.example.uzradyab.presentation.map

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.uzradyab.domain.model.Device
import com.example.uzradyab.domain.model.Position
import com.example.uzradyab.map.tile.ExirFirmTileSource
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

private val Tehran = GeoPoint(35.6892, 51.3890)
private const val OSMDROID_PREFS = "osmdroid"

@Composable
fun TrackingMap(
    devices: List<Device>,
    latestPositions: Map<Long, Position>,
    selectedDeviceId: Long?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val selectedPosition = latestPositions[selectedDeviceId]
    val center = selectedPosition?.toGeoPoint()
        ?: latestPositions.values.firstOrNull()?.toGeoPoint()
        ?: Tehran

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFE8F0F6)),
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                Configuration.getInstance().load(
                    it,
                    it.getSharedPreferences(OSMDROID_PREFS, Context.MODE_PRIVATE),
                )
                Configuration.getInstance().userAgentValue = context.packageName
                MapView(it).apply {
                    setTileSource(ExirFirmTileSource())
                    setMultiTouchControls(true)
                    setMinZoomLevel(3.0)
                    setMaxZoomLevel(19.0)
                    controller.setZoom(13.0)
                    controller.setCenter(center)
                }
            },
            update = { mapView ->
                mapView.setTileSource(ExirFirmTileSource())
                mapView.overlays.removeAll { it is Marker }
                devices.forEach { device ->
                    latestPositions[device.id]?.let { position ->
                        mapView.overlays.add(
                            Marker(mapView).apply {
                                this.position = position.toGeoPoint()
                                title = device.name
                                snippet = formatStatus(device.status)
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            },
                        )
                    }
                }
                mapView.controller.setCenter(center)
                mapView.invalidate()
            },
        )
        Text(
            text = "نقشه زنده",
            color = MaterialTheme.colorScheme.secondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.Center)
                .background(Color.White.copy(alpha = 0.82f), MaterialTheme.shapes.small)
                .padding(horizontal = 14.dp, vertical = 8.dp),
        )
    }
}

private fun Position.toGeoPoint(): GeoPoint = GeoPoint(latitude, longitude)

private fun formatStatus(status: String): String = when (status) {
    "online" -> "دستگاه آنلاین"
    "offline" -> "دستگاه آفلاین"
    else -> "وضعیت نامشخص"
}
