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
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import android.util.Log
import com.example.uzradyab.core.utils.ImmutableListWrapper
import com.example.uzradyab.core.utils.ImmutableMapWrapper
import com.example.uzradyab.domain.model.Device
import com.example.uzradyab.domain.model.Position
import com.example.uzradyab.map.OsmdroidConfig
import com.example.uzradyab.map.tile.TileSourceRegistry
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.MapEventsOverlay
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.uzradyab.R
import android.view.MotionEvent

private val Tehran = GeoPoint(35.6892, 51.3890)
private const val SELECTED_DEVICE_MARKER = "selected-device-marker"

@Composable
fun TrackingMap(
    devices: ImmutableListWrapper<Device>,
    latestPositions: ImmutableMapWrapper<Long, Position>,
    selectedDeviceId: Long?,
    mapStyle: String = "carto",
    activeTileSource: org.osmdroid.tileprovider.tilesource.ITileSource? = null,
    isMapLocked: Boolean,
    mapBottomPadding: Dp = 0.dp,
    onMapInteraction: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val bottomPaddingPx = with(density) { mapBottomPadding.roundToPx() }
    
    val selectedPosition = latestPositions[selectedDeviceId]
    val center = selectedPosition?.toGeoPoint()
        ?: latestPositions.values.firstOrNull()?.toGeoPoint()
        ?: Tehran
    val centerKey = "${selectedDeviceId}:${center.latitude}:${center.longitude}"

    val currentOnMapInteraction by rememberUpdatedState(onMapInteraction)
    val currentIsMapLocked by rememberUpdatedState(isMapLocked)
    var wasLocked by remember { mutableStateOf(isMapLocked) }
    val tracker = remember { object { 
        var lastDeviceId: Long? = selectedDeviceId
        var hasInitialCentered: Boolean = false
        var lastPosition: Position? = null
        var eventsReceiverAdded: Boolean = false
    } }

    val lifecycleOwner = LocalLifecycleOwner.current
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START || event == Lifecycle.Event.ON_RESUME) {
                mapViewRef?.onResume()
            } else if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                mapViewRef?.onPause()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFE8F0F6)),
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                // Use centralised configuration instead of inline duplication
                OsmdroidConfig.configure(it.applicationContext)
                val tileProvider = TileProviderManager.getProvider(it.applicationContext)

                MapView(it, tileProvider).apply {
                    mapViewRef = this
                    val resolvedSource = activeTileSource ?: TileSourceRegistry.resolve(mapStyle)
                    setTileSource(resolvedSource)
                    setMultiTouchControls(true)
                    setBuiltInZoomControls(false)
                    zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                    setMinZoomLevel(3.0)
                    setMaxZoomLevel(resolvedSource.maximumZoomLevel.toDouble())
                    controller.setZoom(18.0)
                    controller.setCenter(center)
                    tag = centerKey
                    
                    // Force start rendering immediately (fixes blank map during enter animations)
                    onResume()

                    val rotationGestureOverlay = RotationGestureOverlay(this)
                    rotationGestureOverlay.isEnabled = true
                    overlays.add(rotationGestureOverlay)

                    setOnTouchListener { _, event ->
                        if (event.action == android.view.MotionEvent.ACTION_DOWN) {
                            currentOnMapInteraction()
                        }
                        
                        if (currentIsMapLocked) {
                            // Block any panning (ACTION_MOVE) or zooming (multi-touch)
                            if (event.action == android.view.MotionEvent.ACTION_MOVE || event.pointerCount > 1) {
                                currentOnMapInteraction()
                                return@setOnTouchListener true
                            }
                        } else {
                            if (event.action == android.view.MotionEvent.ACTION_MOVE) {
                                currentOnMapInteraction()
                            }
                        }
                        false
                    }
                }
            },
            update = { mapView ->
                val tileSource = activeTileSource ?: TileSourceRegistry.resolve(mapStyle)
                if (mapView.tileProvider.tileSource != tileSource) {
                    mapView.tileProvider.clearTileCache()
                    mapView.setTileSource(tileSource)
                    // Update max zoom to match the new source's limit
                    mapView.setMaxZoomLevel(tileSource.maximumZoomLevel.toDouble())
                    // Force a full redraw so old tiles are immediately dropped from screen
                    mapView.invalidate()
                }

                // Keep map interactive: always allow multi‑touch gestures (pinch, rotate, pan)
                mapView.setMultiTouchControls(true)
                
                // Toggle map rotation gesture based on map lock state
                mapView.overlays.filterIsInstance<RotationGestureOverlay>().firstOrNull()?.let {
                    it.isEnabled = !isMapLocked
                }

                // Center map only when needed; we no longer disable interaction
                if (isMapLocked) {
                    // When locked, strictly center the map without disabling gestures
                    if (mapView.width > 0 && mapView.height > 0) {
                        mapView.controller.animateTo(center, 20.0, 500L)
                    } else {
                        mapView.controller.setZoom(20.0)
                        mapView.controller.setCenter(center)
                    }
                    mapView.tag = centerKey
                }
                wasLocked = isMapLocked
                
                if (tracker.lastDeviceId != selectedDeviceId) {
                    tracker.lastDeviceId = selectedDeviceId
                    tracker.hasInitialCentered = false
                }
                
                if (selectedPosition != null && !tracker.hasInitialCentered) {
                    if (mapView.width > 0 && mapView.height > 0) {
                        mapView.controller.animateTo(center, 18.0, 1000L)
                    } else {
                        mapView.controller.setZoom(18.0)
                        mapView.controller.setCenter(center)
                    }
                    tracker.hasInitialCentered = true
                }

                // Offset map center feature removed by user request

                // Handle map clicks
                if (!tracker.eventsReceiverAdded) {
                    val eventsReceiver = object : MapEventsReceiver {
                        override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                            currentOnMapInteraction()
                            return true
                        }
                        override fun longPressHelper(p: GeoPoint?): Boolean {
                            currentOnMapInteraction()
                            return true
                        }
                    }
                    mapView.overlays.add(0, MapEventsOverlay(eventsReceiver))
                    tracker.eventsReceiverAdded = true
                }

                if (selectedPosition != tracker.lastPosition) {
                    val existingMarker = mapView.overlays.find { 
                        it is Marker && it.relatedObject == SELECTED_DEVICE_MARKER 
                    } as? Marker

                    if (selectedPosition == null) {
                        existingMarker?.let { mapView.overlays.remove(it) }
                    } else {
                        val newGeoPoint = selectedPosition.toGeoPoint()
                        val newRotation = selectedPosition.course.toFloat()
                        val speedKmh = (selectedPosition.speed * 1.852).toInt()
                        val newIcon = MarkerCache.getOrCreate(mapView.context, speedKmh)

                        if (existingMarker == null) {
                            mapView.overlays.add(
                                Marker(mapView).apply {
                                    this.position = newGeoPoint
                                    // Marker should always be vertical (0f)
                                    this.rotation = 0f
                                    this.icon = newIcon
                                    setAnchor(Marker.ANCHOR_CENTER, 70f / 106f)
                                    relatedObject = SELECTED_DEVICE_MARKER
                                    infoWindow = null
                                }
                            )
                            if (isMapLocked) {
                                // Rotate map so that the device marker stays upright based on its course
                                mapView.mapOrientation = (360f - newRotation) % 360f
                            }
                        } else {
                            existingMarker.icon = newIcon
                            MarkerAnimator.animateMarker(
                                marker = existingMarker,
                                mapView = mapView,
                                endPosition = newGeoPoint,
                                endCourse = newRotation,
                                rotateMap = isMapLocked,
                                durationMs = 300L
                            )
                        }
                    }
                    tracker.lastPosition = selectedPosition
                    mapView.invalidate()
                }
            },
            onRelease = { mapView ->
                // Clean up map resources to prevent memory leaks and database locks
                mapView.tileProvider.detach()
                mapView.onDetach()
            }
        )
    }
}

private fun Position.toGeoPoint(): GeoPoint = GeoPoint(latitude, longitude)

private fun createDeviceMarkerDrawable(context: Context, speedKmh: Int): BitmapDrawable {
    val density = context.resources.displayMetrics.density
    fun dp(value: Float): Float = value * density

    val bitmap = Bitmap.createBitmap(dp(72f).toInt(), dp(106f).toInt(), Bitmap.Config.ARGB_8888)
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

    return BitmapDrawable(context.resources, bitmap)
}

private fun String.toPersianDigits(): String {
    val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    return buildString(length) {
        this@toPersianDigits.forEach { char ->
            append(if (char in '0'..'9') persianDigits[char - '0'] else char)
        }
    }
}

// Cache to prevent allocating Bitmaps 1x per second on the UI thread
object MarkerCache {
    private val cache = android.util.LruCache<Int, BitmapDrawable>(60)

    fun getOrCreate(context: Context, speedKmh: Int): BitmapDrawable {
        return cache.get(speedKmh) ?: run {
            val drawable = createDeviceMarkerDrawable(context, speedKmh)
            cache.put(speedKmh, drawable)
            drawable
        }
    }
}

// Singleton manager to prevent leaking TileProvider thread pools and SQLite locks
// during rapid Compose navigation (where old MapViews are destroyed after new ones are created).
object TileProviderManager {
    private var provider: com.example.uzradyab.map.tile.UzradyabMapTileProvider? = null

    fun getProvider(context: android.content.Context): com.example.uzradyab.map.tile.UzradyabMapTileProvider {
        if (provider == null) {
            provider = com.example.uzradyab.map.tile.UzradyabMapTileProvider(context.applicationContext)
        }
        return provider!!
    }
}
