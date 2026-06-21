package com.example.uzradyab.presentation.replay

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.uzradyab.domain.model.Position
import com.example.uzradyab.map.OsmdroidConfig
import com.example.uzradyab.map.tile.TileSourceRegistry
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.uzradyab.R
import org.osmdroid.util.BoundingBox

private val Tehran = GeoPoint(35.6892, 51.3890)
private const val REPLAY_MARKER = "replay-marker"

@Composable
fun ReplayMap(
    positions: List<Position>,
    currentIndex: Int,
    mapStyle: String = "osm",
    activeTileSource: org.osmdroid.tileprovider.tilesource.ITileSource? = null,
    onNodeClick: (Position) -> Unit = {},
    mapBottomPadding: Dp = 0.dp,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val bottomPaddingPx = with(density) { mapBottomPadding.roundToPx() }
    
    val currentPosition = positions.getOrNull(currentIndex)
    val center = currentPosition?.toGeoPoint() ?: Tehran

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
                val tileProvider = com.example.uzradyab.map.tile.UzradyabMapTileProvider(it.applicationContext)

                MapView(it, tileProvider).apply {
                    val resolvedSource = activeTileSource ?: TileSourceRegistry.resolve(mapStyle)
                    setTileSource(resolvedSource)
                    setMultiTouchControls(true)
                    setBuiltInZoomControls(false)
                    zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                    setMinZoomLevel(3.0)
                    // Respect tile source's declared max zoom instead of hardcoded 23
                    setMaxZoomLevel(resolvedSource.maximumZoomLevel.toDouble())
                }
            },
            update = { mapView ->
                val tileSource = activeTileSource ?: TileSourceRegistry.resolve(mapStyle)
                if (mapView.tileProvider.tileSource != tileSource) {
                    mapView.setTileSource(tileSource)
                    // Update max zoom to match the new source's limit
                    mapView.setMaxZoomLevel(tileSource.maximumZoomLevel.toDouble())
                }

                // Adjust for bottom panel padding
                mapView.setMapCenterOffset(0, -bottomPaddingPx / 2)

                // First time zooming to bounds or tracking
                val isFirstLoad = mapView.tag == null
                if (isFirstLoad && positions.isNotEmpty()) {
                    mapView.tag = "loaded"
                    // Bound to route
                    val lats = positions.map { it.latitude }
                    val lons = positions.map { it.longitude }
                    val boundingBox = BoundingBox(
                        lats.maxOrNull() ?: 0.0,
                        lons.maxOrNull() ?: 0.0,
                        lats.minOrNull() ?: 0.0,
                        lons.minOrNull() ?: 0.0
                    )
                    // Post to let MapView layout first
                    mapView.post {
                        mapView.zoomToBoundingBox(boundingBox, true, 100)
                    }
                } else if (positions.isNotEmpty() && !isFirstLoad) {
                    // Smoothly animate to current position when playing
                    mapView.controller.animateTo(center)
                }

                mapView.overlays.clear()

                if (positions.isNotEmpty()) {
                    // Draw Polyline
                    val routePolyline = Polyline(mapView).apply {
                        setPoints(positions.map { it.toGeoPoint() })
                        color = AndroidColor.parseColor("#A12887")
                        width = with(density) { 4.dp.toPx() } // thickness
                        isGeodesic = true
                        infoWindow = null // Disable native infoWindow on click
                    }
                    mapView.overlays.add(routePolyline)

                    // Draw a dot for each position (Commented out to prevent lag on huge routes)
                    /* 
                    val dotDrawable = getCachedDotDrawable(mapView.context)
                    positions.forEach { pos ->
                        val nodeMarker = Marker(mapView).apply {
                            position = pos.toGeoPoint()
                            icon = dotDrawable
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                            infoWindow = null // Disable native infoWindow on click
                            setOnMarkerClickListener { _, _ ->
                                onNodeClick(pos)
                                true
                            }
                        }
                        mapView.overlays.add(nodeMarker)
                    }
                    */

                    // Draw current car marker
                    currentPosition?.let { pos ->
                        mapView.overlays.add(
                            Marker(mapView).apply {
                                this.position = pos.toGeoPoint()
                                icon = createDeviceMarkerDrawable(
                                    context = mapView.context,
                                    speedKmh = (pos.speed * 1.852).toInt()
                                )
                                setAnchor(Marker.ANCHOR_CENTER, 70f / 106f)
                                relatedObject = REPLAY_MARKER
                                infoWindow = null
                            }
                        )
                    }
                }

                mapView.invalidate()
            },
            onRelease = { mapView ->
                mapView.tileProvider.detach()
                mapView.onDetach()
            }
        )
    }
}

private var cachedDotDrawable: BitmapDrawable? = null

private fun getCachedDotDrawable(context: Context): BitmapDrawable {
    if (cachedDotDrawable == null) {
        cachedDotDrawable = createImprovedDotDrawable(context, AndroidColor.parseColor("#A12887"))
    }
    return cachedDotDrawable!!
}

private fun createImprovedDotDrawable(context: Context, color: Int): BitmapDrawable {
    val density = context.resources.displayMetrics.density
    fun dp(value: Float): Float = value * density

    val size = dp(16f) // Slightly larger for clickability
    val bitmap = Bitmap.createBitmap(size.toInt(), size.toInt(), Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    val center = size / 2f
    
    // Draw shadow and white border
    paint.color = AndroidColor.WHITE
    paint.style = Paint.Style.FILL
    paint.setShadowLayer(dp(2f), 0f, dp(1f), AndroidColor.argb(60, 0, 0, 0))
    canvas.drawCircle(center, center, dp(6f), paint)
    
    paint.clearShadowLayer()
    
    // Draw colored inner circle
    paint.color = color
    canvas.drawCircle(center, center, dp(4f), paint)
    
    // Draw tiny glossy center
    paint.color = AndroidColor.WHITE
    canvas.drawCircle(center, center, dp(1.5f), paint)

    return BitmapDrawable(context.resources, bitmap)
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
