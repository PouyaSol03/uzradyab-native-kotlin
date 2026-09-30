package com.example.uzradyab.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.uzradyab.map.MapLibreStyles
import com.example.uzradyab.ui.theme.RoutingColors
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapLibreMapOptions
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.plugins.annotation.CircleManager
import org.maplibre.android.plugins.annotation.CircleOptions
import org.maplibre.android.plugins.annotation.LineManager
import org.maplibre.android.plugins.annotation.LineOptions
import org.maplibre.android.plugins.annotation.SymbolManager
import org.maplibre.android.plugins.annotation.SymbolOptions
import org.maplibre.android.style.layers.Property

// ---- Scene description ----

@Immutable
data class MapLine(
    val points: List<LatLng>,
    val color: Color,
    val width: Float,
    /** White outline drawn under the line, total width. */
    val casingWidth: Float? = null,
    val dotted: Boolean = false,
)

@Immutable
data class MapDot(
    val position: LatLng,
    val radius: Float,
    val color: Color,
    val opacity: Float = 1f,
    val strokeColor: Color? = null,
    val strokeWidth: Float = 0f,
)

@Immutable
data class MapMarker(
    val icon: MarkerIcon,
    val position: LatLng,
    /** "center", "bottom", ... */
    val anchor: String = Property.ICON_ANCHOR_CENTER,
    /** Offset in dp, (x, y). */
    val offset: Pair<Float, Float> = 0f to 0f,
    /** Bearing in degrees; only used for markers that turn with the map (the puck). */
    val rotation: Float = 0f,
    val rotatesWithMap: Boolean = false,
)

@Immutable
data class MapScene(
    val lines: List<MapLine> = emptyList(),
    val dots: List<MapDot> = emptyList(),
    val markers: List<MapMarker> = emptyList(),
)

/** What the camera should do. A new [key] repeats a one-shot move. */
@Immutable
sealed interface MapCamera {
    /** North-up, fitted to [points]. */
    data class Fit(val points: List<LatLng>, val key: Any, val maxZoom: Double = 17.0) : MapCamera

    data class Center(val target: LatLng, val zoom: Double, val key: Any) : MapCamera

    /** Follows the driver; [bearing] 0 means north-up. Updated on every fix. */
    data class Follow(val target: LatLng, val bearing: Double, val zoom: Double = 17.0) : MapCamera

    /** Rotates to north without moving. */
    data class NorthUp(val key: Any) : MapCamera
}

/** Parts of the map covered by overlays, in px. */
@Immutable
data class MapInsets(val top: Int = 0, val bottom: Int = 0, val side: Int = 0)

private class Managers(view: MapView, map: MapLibreMap, style: Style) {
    val dotted = LineManager(view, map, style).apply {
        lineCap = Property.LINE_CAP_ROUND
        lineDasharray = arrayOf(0f, 1.8f)
    }
    val lines = LineManager(view, map, style).apply { lineCap = Property.LINE_CAP_ROUND }
    val circles = CircleManager(view, map, style)
    val symbols = SymbolManager(view, map, style).apply {
        iconAllowOverlap = true
        iconIgnorePlacement = true
        iconRotationAlignment = Property.ICON_ROTATION_ALIGNMENT_VIEWPORT
    }
    val puck = SymbolManager(view, map, style).apply {
        iconAllowOverlap = true
        iconIgnorePlacement = true
        iconRotationAlignment = Property.ICON_ROTATION_ALIGNMENT_MAP
    }

    fun destroy() {
        runCatching { dotted.onDestroy() }
        runCatching { lines.onDestroy() }
        runCatching { circles.onDestroy() }
        runCatching { symbols.onDestroy() }
        runCatching { puck.onDestroy() }
    }
}

private fun Color.hex(): String = String.format("#%06X", 0xFFFFFF and toArgb())

@Composable
fun NavigationMap(
    scene: MapScene,
    camera: MapCamera?,
    insets: MapInsets,
    onUserGesture: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var map by remember { mutableStateOf<MapLibreMap?>(null) }
    var managers by remember { mutableStateOf<Managers?>(null) }
    val currentOnUserGesture by rememberUpdatedState(onUserGesture)

    DisposableEffect(lifecycleOwner, mapView) {
        val view = mapView
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> view?.onStart()
                Lifecycle.Event.ON_RESUME -> view?.onResume()
                Lifecycle.Event.ON_PAUSE -> view?.onPause()
                Lifecycle.Event.ON_STOP -> view?.onStop()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    DisposableEffect(Unit) {
        onDispose {
            managers?.destroy()
            managers = null
            mapView?.let {
                it.onPause()
                it.onStop()
                it.onDestroy()
            }
        }
    }

    AndroidView(
        modifier = modifier.background(RoutingColors.mapBackground),
        factory = { ctx ->
            MapLibre.getInstance(ctx)
            val options = MapLibreMapOptions.createFromAttributes(ctx).textureMode(true)
            MapView(ctx, options).apply {
                onCreate(null)
                mapView = this
                getMapAsync { m ->
                    m.uiSettings.isCompassEnabled = false
                    m.uiSettings.isLogoEnabled = false
                    m.uiSettings.isAttributionEnabled = false
                    m.uiSettings.isTiltGesturesEnabled = false
                    m.addOnCameraMoveStartedListener { reason ->
                        if (reason == MapLibreMap.OnCameraMoveStartedListener.REASON_API_GESTURE) {
                            currentOnUserGesture()
                        }
                    }
                    m.setStyle(Style.Builder().fromUri(MapLibreStyles.NESHAN_STANDARD_LIGHT)) { style ->
                        managers = Managers(this, m, style)
                        map = m
                    }
                }
            }
        },
    )

    // Draw the scene
    LaunchedEffect(managers, scene) {
        val m = managers ?: return@LaunchedEffect
        val style = map?.style ?: return@LaunchedEffect
        if (!style.isFullyLoaded) return@LaunchedEffect

        scene.markers.map { it.icon }.distinctBy { it.id }.forEach { icon ->
            if (style.getImage(icon.id) == null) {
                style.addImage(icon.id, NavigationMapIcons.render(context, icon))
            }
        }

        m.dotted.deleteAll()
        m.lines.deleteAll()
        m.circles.deleteAll()
        m.symbols.deleteAll()
        m.puck.deleteAll()

        val solidLines = mutableListOf<LineOptions>()
        val dottedLines = mutableListOf<LineOptions>()
        scene.lines.filter { it.points.size >= 2 }.forEach { line ->
            val target = if (line.dotted) dottedLines else solidLines
            line.casingWidth?.let { casing ->
                solidLines += LineOptions()
                    .withLatLngs(line.points)
                    .withLineColor("#FFFFFF")
                    .withLineWidth(casing)
                    .withLineJoin(Property.LINE_JOIN_ROUND)
            }
            target += LineOptions()
                .withLatLngs(line.points)
                .withLineColor(line.color.hex())
                .withLineWidth(line.width)
                .withLineJoin(Property.LINE_JOIN_ROUND)
        }
        if (dottedLines.isNotEmpty()) m.dotted.create(dottedLines)
        if (solidLines.isNotEmpty()) m.lines.create(solidLines)

        if (scene.dots.isNotEmpty()) {
            m.circles.create(
                scene.dots.map { dot ->
                    CircleOptions()
                        .withLatLng(dot.position)
                        .withCircleRadius(dot.radius)
                        .withCircleColor(dot.color.hex())
                        .withCircleOpacity(dot.opacity)
                        .withCircleStrokeColor((dot.strokeColor ?: dot.color).hex())
                        .withCircleStrokeWidth(dot.strokeWidth)
                },
            )
        }

        val (turning, fixed) = scene.markers.partition { it.rotatesWithMap }
        if (fixed.isNotEmpty()) {
            m.symbols.create(
                fixed.mapIndexed { index, marker ->
                    SymbolOptions()
                        .withLatLng(marker.position)
                        .withIconImage(marker.icon.id)
                        .withIconAnchor(marker.anchor)
                        .withIconOffset(arrayOf(marker.offset.first, marker.offset.second))
                        .withSymbolSortKey(index.toFloat())
                },
            )
        }
        if (turning.isNotEmpty()) {
            m.puck.create(
                turning.map { marker ->
                    SymbolOptions()
                        .withLatLng(marker.position)
                        .withIconImage(marker.icon.id)
                        .withIconRotate(marker.rotation)
                },
            )
        }
    }

    // Move the camera
    val cameraKey: Any? = when (camera) {
        is MapCamera.Fit -> camera.key
        is MapCamera.Center -> camera.key
        is MapCamera.NorthUp -> camera.key
        is MapCamera.Follow -> camera
        null -> null
    }
    LaunchedEffect(map, cameraKey, insets) {
        val m = map ?: return@LaunchedEffect
        val view = mapView ?: return@LaunchedEffect
        when (camera) {
            is MapCamera.Fit -> {
                val points = camera.points
                if (points.size < 2) {
                    points.firstOrNull()?.let { m.animateCamera(CameraUpdateFactory.newLatLngZoom(it, 16.0), 800) }
                    return@LaunchedEffect
                }
                val bounds = LatLngBounds.Builder().apply { points.forEach { include(it) } }.build()
                val padding = intArrayOf(insets.side, insets.top, insets.side, insets.bottom)
                val fitted = m.getCameraForLatLngBounds(bounds, padding, 0.0, 0.0) ?: return@LaunchedEffect
                val position = CameraPosition.Builder(fitted)
                    .zoom(fitted.zoom.coerceAtMost(camera.maxZoom))
                    .bearing(0.0)
                    .tilt(0.0)
                    .padding(0.0, 0.0, 0.0, 0.0)
                    .build()
                m.animateCamera(CameraUpdateFactory.newCameraPosition(position), 900)
            }
            is MapCamera.Center -> {
                val position = CameraPosition.Builder()
                    .target(camera.target)
                    .zoom(camera.zoom)
                    .bearing(0.0)
                    .tilt(0.0)
                    .padding(0.0, insets.top.toDouble(), 0.0, insets.bottom.toDouble())
                    .build()
                m.animateCamera(CameraUpdateFactory.newCameraPosition(position), 800)
            }
            is MapCamera.Follow -> {
                // Keep the puck at about two thirds of the screen height, above the bottom bar.
                val height = view.height.toDouble()
                val top = (0.32 * height + insets.bottom).coerceAtMost(height * 0.8)
                val position = CameraPosition.Builder()
                    .target(camera.target)
                    .zoom(camera.zoom)
                    .bearing(camera.bearing)
                    .tilt(0.0)
                    .padding(0.0, top, 0.0, insets.bottom.toDouble())
                    .build()
                m.easeCamera(CameraUpdateFactory.newCameraPosition(position), 900)
            }
            is MapCamera.NorthUp -> m.animateCamera(CameraUpdateFactory.bearingTo(0.0), 500)
            null -> Unit
        }
    }
}
