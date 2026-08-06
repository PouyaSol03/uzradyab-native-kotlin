package com.example.uzradyab.presentation.replay

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.uzradyab.R
import com.example.uzradyab.domain.model.Position
import com.example.uzradyab.map.MapLibreStyles
import com.example.uzradyab.presentation.map.MarkerAnimator
import com.example.uzradyab.ui.theme.themedColor
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.maps.MapLibreMapOptions
import org.maplibre.android.plugins.annotation.Line
import org.maplibre.android.plugins.annotation.LineManager
import org.maplibre.android.plugins.annotation.LineOptions
import org.maplibre.android.plugins.annotation.Symbol
import org.maplibre.android.plugins.annotation.SymbolManager
import org.maplibre.android.plugins.annotation.SymbolOptions

private val Tehran = LatLng(35.6892, 51.3890)

@Composable
fun ReplayMap(
    positions: List<Position>,
    currentIndex: Int,
    mapStyle: String = "osm",
    playSpeed: Int = 1,
    onNodeClick: (Position) -> Unit = {},
    mapBottomPadding: Dp = 0.dp,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val bottomPaddingPx = with(density) { mapBottomPadding.roundToPx() }
    
    val currentPosition = positions.getOrNull(currentIndex)
    val center = currentPosition?.toLatLng() ?: Tehran

    val lifecycleOwner = LocalLifecycleOwner.current
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var mapLibreMapRef by remember { mutableStateOf<MapLibreMap?>(null) }
    
    var symbolManager by remember { mutableStateOf<SymbolManager?>(null) }
    var lineManager by remember { mutableStateOf<LineManager?>(null) }
    var deviceSymbol by remember { mutableStateOf<Symbol?>(null) }
    var routeLine by remember { mutableStateOf<Line?>(null) }
    
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
    
    val isDarkTheme = androidx.compose.foundation.isSystemInDarkTheme()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(themedColor(light = Color(0xFFE8F0F6), dark = Color(0xFF11212C))),
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
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
                        
                        tracker.lastMapStyle = mapStyle
                        tracker.styleToken++
                        val currentToken = tracker.styleToken
                        map.setStyle(Style.Builder().fromJson(MapLibreStyles.getStyleJson(mapStyle, isDarkTheme))) { style ->
                            if (currentToken != tracker.styleToken) return@setStyle
                            symbolManager = SymbolManager(this, map, style).apply {
                                iconAllowOverlap = true
                                iconIgnorePlacement = true
                            }
                            lineManager = LineManager(this, map, style)
                            
                            map.cameraPosition = CameraPosition.Builder()
                                .target(Tehran)
                                .zoom(12.0)
                                .build()
                        }
                    }
                }
            },
            update = { mapView ->
                val map = mapLibreMapRef ?: return@AndroidView
                
                // Style updates
                if (tracker.lastMapStyle != mapStyle) {
                    tracker.lastMapStyle = mapStyle
                    tracker.styleToken++
                    val currentToken = tracker.styleToken
                    
                    symbolManager?.onDestroy()
                    lineManager?.onDestroy()
                    deviceSymbol = null
                    routeLine = null
                    symbolManager = null
                    lineManager = null
                    
                    map.setStyle(Style.Builder().fromJson(MapLibreStyles.getStyleJson(mapStyle, isDarkTheme))) { newStyle ->
                        if (currentToken != tracker.styleToken) return@setStyle
                        symbolManager = SymbolManager(mapView, map, newStyle).apply {
                            iconAllowOverlap = true
                            iconIgnorePlacement = true
                        }
                        lineManager = LineManager(mapView, map, newStyle)
                    }
                }

                // Adjust for bottom panel padding
                map.uiSettings.setFocalPoint(
                    android.graphics.PointF(
                        (mapView.width / 2).toFloat(),
                        (mapView.height / 2 - bottomPaddingPx / 2).toFloat()
                    )
                )

                // First time zooming to bounds
                val isFirstLoad = mapView.tag == null
                if (isFirstLoad && positions.isNotEmpty()) {
                    mapView.tag = "loaded"
                    val lats = positions.map { it.latitude }
                    val lons = positions.map { it.longitude }
                    val bounds = LatLngBounds.Builder()
                        .include(LatLng(lats.maxOrNull() ?: 0.0, lons.maxOrNull() ?: 0.0))
                        .include(LatLng(lats.minOrNull() ?: 0.0, lons.minOrNull() ?: 0.0))
                        .build()
                        
                    mapView.post {
                        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
                    }
                }

                map.getStyle { style ->
                    val sm = symbolManager ?: return@getStyle
                    val lm = lineManager ?: return@getStyle

                    if (positions.isNotEmpty()) {
                        // Draw Route Polyline
                        val routeLatLngs = positions.map { it.toLatLng() }
                        if (routeLine == null) {
                            routeLine = lm.create(
                                LineOptions()
                                    .withLatLngs(routeLatLngs)
                                    .withLineColor("#A12887")
                                    .withLineWidth(4f)
                            )
                        } else {
                            routeLine?.let {
                                if (it.latLngs != routeLatLngs) {
                                    it.latLngs = routeLatLngs
                                    lm.update(it)
                                }
                            }
                        }

                        // Draw current car marker
                        currentPosition?.let { pos ->
                            val newLatLng = pos.toLatLng()
                            val newRotation = pos.course.toFloat()
                            val speedKmh = (pos.speed * 1.852).toInt()
                            val iconId = "marker_$speedKmh"

                            if (style.getImage(iconId) == null) {
                                style.addImage(iconId, createDeviceMarkerBitmap(context, speedKmh))
                            }

                            if (deviceSymbol == null) {
                                deviceSymbol = sm.create(
                                    SymbolOptions()
                                        .withLatLng(newLatLng)
                                        .withIconImage(iconId)
                                        .withIconAnchor("center")
                                        .withIconOffset(arrayOf(0f, 0f))
                                )
                                map.moveCamera(CameraUpdateFactory.newCameraPosition(
                                    CameraPosition.Builder()
                                        .target(newLatLng)
                                        .bearing(newRotation.toDouble())
                                        .build()
                                ))
                            } else {
                                val animationDuration = if (playSpeed == 1) 1500L else 750L
                                deviceSymbol?.iconImage = iconId
                                deviceSymbol?.let {
                                    MarkerAnimator.animateMarker(
                                        symbol = it,
                                        symbolManager = sm,
                                        mapView = map,
                                        endPosition = newLatLng,
                                        endCourse = newRotation,
                                        rotateMap = true,
                                        durationMs = animationDuration
                                    )
                                }
                            }
                        }
                    } else {
                        // Clear
                        deviceSymbol?.let { sm.delete(it) }
                        routeLine?.let { lm.delete(it) }
                        deviceSymbol = null
                        routeLine = null
                    }
                }
            }
        )
    }
}

private fun Position.toLatLng(): LatLng = LatLng(latitude, longitude)

private fun createDeviceMarkerBitmap(context: Context, speedKmh: Int): Bitmap {
    val density = context.resources.displayMetrics.density
    fun dp(value: Float): Float = value * density

    val bitmap = Bitmap.createBitmap(dp(72f).toInt(), dp(140f).toInt(), Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.scale(density, density)

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val purple = AndroidColor.rgb(162, 40, 135)
    val bubblePurple = AndroidColor.rgb(161, 40, 135)
    val centerX = 36f
    val circleCenterY = 70f

    paint.style = Paint.Style.FILL
    paint.color = AndroidColor.argb(20, 200, 0, 157)
    canvas.drawCircle(centerX, circleCenterY, 36f, paint)
    paint.color = AndroidColor.argb(61, 200, 0, 157)
    canvas.drawCircle(centerX, circleCenterY, 30f, paint)
    paint.color = purple
    canvas.drawCircle(centerX, circleCenterY, 24f, paint)

    canvas.save()
    canvas.translate(0f, 34f)
    paint.color = AndroidColor.WHITE
    val car = Path().apply {
        moveTo(21.5698f, 41.7749f)
        cubicTo(20.6279f, 41.7749f, 20f, 41.1439f, 20f, 40.1974f)
        lineTo(20f, 35.4649f)
        cubicTo(20f, 34.0452f, 21.0989f, 32.7832f, 22.3547f, 32.4677f)
        cubicTo(25.1803f, 31.679f, 29.4187f, 30.7325f, 29.4187f, 30.7325f)
        cubicTo(29.4187f, 30.7325f, 31.4595f, 28.524f, 32.8723f, 27.1042f)
        cubicTo(33.6572f, 26.4732f, 34.5991f, 26f, 35.6979f, 26f)
        lineTo(46.6865f, 26f)
        cubicTo(47.6283f, 26f, 48.4132f, 26.631f, 48.8842f, 27.4197f)
        lineTo(51.0819f, 31.9944f)
        cubicTo(51.2897f, 32.6037f, 51.3958f, 33.2434f, 51.3958f, 33.8874f)
        lineTo(51.3958f, 40.1974f)
        cubicTo(51.3958f, 41.1439f, 50.7679f, 41.7749f, 49.826f, 41.7749f)
        lineTo(47.1056f, 41.7749f)
        cubicTo(47.1062f, 41.7497f, 47.1065f, 41.7245f, 47.1065f, 41.6992f)
        cubicTo(47.1065f, 39.9992f, 45.7285f, 38.6211f, 44.0285f, 38.6211f)
        cubicTo(42.3286f, 38.6211f, 40.9505f, 39.9992f, 40.9505f, 41.6992f)
        cubicTo(40.9505f, 41.7245f, 40.9508f, 41.7497f, 40.9514f, 41.7749f)
        lineTo(31.4848f, 41.7749f)
        cubicTo(31.4854f, 41.7497f, 31.4857f, 41.7245f, 31.4857f, 41.6991f)
        cubicTo(31.4857f, 39.9992f, 30.1076f, 38.6211f, 28.4077f, 38.6211f)
        cubicTo(26.7077f, 38.6211f, 25.3297f, 39.9992f, 25.3297f, 41.6992f)
        cubicTo(25.3297f, 41.7245f, 25.33f, 41.7497f, 25.3306f, 41.7749f)
        lineTo(21.5698f, 41.7749f)
        close()
    }
    canvas.drawPath(car, paint)
    paint.color = purple
    canvas.drawRoundRect(RectF(33f, 27.3f, 39.8f, 30.9f), 0.8f, 0.8f, paint)
    canvas.drawRoundRect(RectF(41.3f, 27.3f, 46.8f, 30.9f), 0.8f, 0.8f, paint)
    paint.color = AndroidColor.WHITE
    canvas.drawCircle(44.0282f, 41.6958f, 2.4624f, paint)
    canvas.drawCircle(28.411f, 41.6958f, 2.4624f, paint)
    paint.strokeWidth = 1f
    paint.strokeCap = Paint.Cap.ROUND
    canvas.drawLine(43.8931f, 45.8361f, 48.8931f, 45.8361f, paint)
    canvas.drawLine(29.8931f, 45.8361f, 34.8931f, 45.8361f, paint)
    canvas.restore()

    paint.style = Paint.Style.FILL
    paint.color = bubblePurple
    canvas.drawRoundRect(RectF(10f, 0f, 62f, 26f), 13f, 13f, paint)
    val tail = Path().apply {
        moveTo(36.1962f, 32.5f)
        lineTo(41.3923f, 25.5f)
        lineTo(31f, 25.5f)
        close()
    }
    canvas.drawPath(tail, paint)

    val text = if (speedKmh <= 0) "متوقف" else "${speedKmh.toString().toPersianDigits()} km"
    paint.color = AndroidColor.WHITE
    paint.textAlign = Paint.Align.CENTER
    val customTypeface = androidx.core.content.res.ResourcesCompat.getFont(context, R.font.vazirmatn_regular)
    paint.typeface = customTypeface ?: Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    paint.textSize = if (speedKmh <= 0) 11f else 10f
    canvas.drawText(text, centerX, 17.8f, paint)

    return bitmap
}

private fun String.toPersianDigits(): String {
    val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    return buildString(length) {
        this@toPersianDigits.forEach { char ->
            append(if (char in '0'..'9') persianDigits[char - '0'] else char)
        }
    }
}
