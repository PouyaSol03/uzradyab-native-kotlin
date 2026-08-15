package com.example.uzradyab.presentation.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.uzradyab.core.utils.ImmutableMapWrapper
import com.example.uzradyab.domain.model.Position
import com.example.uzradyab.map.MapLibreStyles
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.maps.MapLibreMapOptions

import org.maplibre.android.plugins.annotation.SymbolManager
import org.maplibre.android.plugins.annotation.SymbolOptions
import org.maplibre.android.plugins.annotation.LineManager
import org.maplibre.android.plugins.annotation.LineOptions
import org.maplibre.android.plugins.annotation.Symbol
import org.maplibre.android.plugins.annotation.Line
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.uzradyab.R
import com.example.uzradyab.ui.theme.themedColor

private val Tehran = LatLng(35.6892, 51.3890)
private const val SELECTED_DEVICE_MARKER_ID = "selected-device-marker"

@Composable
fun TrackingMap(
    latestPositions: ImmutableMapWrapper<Long, Position>,
    selectedDeviceId: Long?,
    mapStyle: String = "osm",
    isMapLocked: Boolean,
    mapBottomPadding: Dp = 0.dp,
    onMapInteraction: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    
    val selectedPosition = latestPositions[selectedDeviceId]
    val center = selectedPosition?.toLatLng()
        ?: latestPositions.values.firstOrNull()?.toLatLng()
        ?: Tehran

    val currentOnMapInteraction by rememberUpdatedState(onMapInteraction)
    val currentIsMapLocked by rememberUpdatedState(isMapLocked)
    
    var tailPositions by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var currentTailDeviceId by remember { mutableStateOf<Long?>(null) }
    
    val density = LocalDensity.current

    LaunchedEffect(selectedDeviceId, selectedPosition) {
        if (selectedDeviceId == null || selectedPosition == null) {
            tailPositions = emptyList()
            currentTailDeviceId = selectedDeviceId
            return@LaunchedEffect
        }
        
        val latLng = selectedPosition.toLatLng()
        
        if (selectedDeviceId != currentTailDeviceId) {
            tailPositions = listOf(latLng)
            currentTailDeviceId = selectedDeviceId
        } else {
            val newList = tailPositions.toMutableList()
            if (newList.lastOrNull() != latLng) {
                newList.add(latLng)
                if (newList.size > 20) {
                    newList.removeAt(0)
                }
            }
            tailPositions = newList
        }
    }

    val tracker = remember { object { 
        var lastDeviceId: Long? = selectedDeviceId
        var hasInitialCentered: Boolean = false
        var lastPosition: Position? = null
        var userInteracted: Boolean = false
        var lastMapStyle: String? = null
        var styleToken: Int = 0
    } }

    val lifecycleOwner = LocalLifecycleOwner.current
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var mapLibreMapRef by remember { mutableStateOf<MapLibreMap?>(null) }
    
    var symbolManager by remember { mutableStateOf<SymbolManager?>(null) }
    var lineManager by remember { mutableStateOf<LineManager?>(null) }
    var deviceSymbol by remember { mutableStateOf<Symbol?>(null) }
    var tailLine by remember { mutableStateOf<Line?>(null) }
    
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
                            lineManager = LineManager(this, map, style)
                            symbolManager = SymbolManager(this, map, style).apply {
                                iconAllowOverlap = true
                                iconIgnorePlacement = true
                            }
                            
                            // Load initial map state
                            map.cameraPosition = CameraPosition.Builder()
                                .target(center)
                                .zoom(16.0)
                                .build()
                        }
                        
                        map.addOnMoveListener(object : MapLibreMap.OnMoveListener {
                            override fun onMoveBegin(detector: org.maplibre.android.gestures.MoveGestureDetector) {
                                tracker.userInteracted = true
                                currentOnMapInteraction()
                            }
                            override fun onMove(detector: org.maplibre.android.gestures.MoveGestureDetector) {}
                            override fun onMoveEnd(detector: org.maplibre.android.gestures.MoveGestureDetector) {}
                        })
                        
                        map.addOnMapClickListener {
                            currentOnMapInteraction()
                            false
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
                    tailLine = null
                    symbolManager = null
                    lineManager = null
                    
                    map.setStyle(Style.Builder().fromJson(MapLibreStyles.getStyleJson(mapStyle, isDarkTheme))) { newStyle ->
                        if (currentToken != tracker.styleToken) return@setStyle
                        lineManager = LineManager(mapView, map, newStyle)
                        symbolManager = SymbolManager(mapView, map, newStyle).apply {
                            iconAllowOverlap = true
                            iconIgnorePlacement = true
                            iconRotationAlignment = org.maplibre.android.style.layers.Property.ICON_ROTATION_ALIGNMENT_VIEWPORT
                        }
                    }
                }

                // Adjust for bottom panel padding
                val bottomPaddingPx = with(density) { mapBottomPadding.roundToPx() }
                map.uiSettings.setFocalPoint(
                    android.graphics.PointF(
                        (mapView.width / 2).toFloat(),
                        (mapView.height / 2 - bottomPaddingPx / 2).toFloat()
                    )
                )

                // Handle gestures based on lock state
                map.uiSettings.isScrollGesturesEnabled = !isMapLocked
                map.uiSettings.isZoomGesturesEnabled = !isMapLocked
                map.uiSettings.isRotateGesturesEnabled = !isMapLocked

                val rawBearing = selectedPosition?.course ?: 0.0
                val currentBearing = map.cameraPosition.bearing
                var delta = (rawBearing - currentBearing) % 360.0
                if (delta > 180.0) delta -= 360.0
                if (delta < -180.0) delta += 360.0
                val bearing = currentBearing + delta

                if (tracker.lastDeviceId != selectedDeviceId) {
                    tracker.lastDeviceId = selectedDeviceId
                    tracker.hasInitialCentered = false
                    tracker.userInteracted = false
                }
                
                if (selectedPosition != null && selectedPosition.deviceId == selectedDeviceId) {
                    if (!tracker.hasInitialCentered) {
                        val builder = CameraPosition.Builder().target(center).zoom(16.0)
                        if (isMapLocked) builder.bearing(bearing)
                        val newCameraPos = builder.build()
                        
                        map.moveCamera(CameraUpdateFactory.newCameraPosition(newCameraPos))
                        tracker.hasInitialCentered = true
                    } else if (isMapLocked && selectedPosition == tracker.lastPosition) {
                        val cameraPosition = CameraPosition.Builder()
                            .target(center)
                            .zoom(16.0)
                            .bearing(bearing)
                            .build()
                        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 500)
                    }
                }

                // Update UI Overlays
                map.getStyle { style ->
                    val sm = symbolManager ?: return@getStyle
                    val lm = lineManager ?: return@getStyle
                    
                    if (selectedPosition != tracker.lastPosition || deviceSymbol == null) {
                        if (selectedPosition == null) {
                            deviceSymbol?.let { sm.delete(it) }
                            deviceSymbol = null
                        } else {
                            val newLatLng = selectedPosition.toLatLng()
                            val speedKmh = (selectedPosition.speed * 1.852).toInt()
                            val iconId = "marker_$speedKmh"
                            
                            // Add image to style if missing
                            if (style.getImage(iconId) == null) {
                                style.addImage(iconId, createDeviceMarkerBitmap(context, speedKmh))
                            }
                            
                            if (deviceSymbol == null || tracker.lastPosition?.deviceId != selectedPosition.deviceId) {
                                if (deviceSymbol == null) {
                                    deviceSymbol = sm.create(
                                        SymbolOptions()
                                            .withLatLng(newLatLng)
                                            .withIconImage(iconId)
                                            .withIconAnchor("center")
                                            .withIconOffset(arrayOf(0f, 0f))
                                    )
                                } else {
                                    deviceSymbol?.let {
                                        it.latLng = newLatLng
                                        it.iconImage = iconId
                                        sm.update(it)
                                    }
                                }
                            } else {
                                deviceSymbol?.let {
                                    val shouldFollow = !tracker.userInteracted
                                    val isLocked = isMapLocked
                                    
                                    MarkerAnimator.animateMarker(
                                        symbol = it,
                                        symbolManager = sm,
                                        endPosition = newLatLng,
                                        durationMs = 1000L,
                                        onUpdate = { fraction, currentLatLng ->
                                            if (shouldFollow) {
                                                val builder = CameraPosition.Builder().target(currentLatLng)
                                                if (isLocked) {
                                                    val startBearing = map.cameraPosition.bearing
                                                    var deltaRot = (bearing - startBearing) % 360.0
                                                    if (deltaRot > 180.0) deltaRot -= 360.0
                                                    if (deltaRot < -180.0) deltaRot += 360.0
                                                    builder.bearing(startBearing + deltaRot * fraction)
                                                }
                                                map.moveCamera(CameraUpdateFactory.newCameraPosition(builder.build()))
                                            }
                                            tailLine?.let { line ->
                                                val points = line.latLngs?.toMutableList() ?: return@let
                                                if (points.isNotEmpty()) {
                                                    points[points.size - 1] = currentLatLng
                                                    line.latLngs = points
                                                    lm.update(line)
                                                }
                                            }
                                        }
                                    )
                                    it.iconImage = iconId
                                }
                            }
                        }
                        tracker.lastPosition = selectedPosition
                    }
                    
                    // Update Tail
                    if (tailPositions.size > 1) {
                        if (tailLine == null) {
                            val initialPoints = tailPositions.toMutableList()
                            if (initialPoints.isNotEmpty() && deviceSymbol != null) {
                                initialPoints[initialPoints.size - 1] = deviceSymbol!!.latLng
                            }
                            tailLine = lm.create(
                                LineOptions()
                                    .withLatLngs(initialPoints)
                                    .withLineColor(if (mapStyle.lowercase().contains("google")) "#E53935" else "#2196F3")
                                    .withLineWidth(4f)
                            )
                        } else {
                            tailLine?.let {
                                val points = tailPositions.toMutableList()
                                if (points.isNotEmpty() && deviceSymbol != null) {
                                    points[points.size - 1] = deviceSymbol!!.latLng
                                }
                                it.latLngs = points
                                lm.update(it)
                            }
                        }
                    } else {
                        tailLine?.let { lm.delete(it) }
                        tailLine = null
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
