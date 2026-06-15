package com.example.uzradyab.map.tile

import com.example.uzradyab.BuildConfig
import java.net.URLEncoder

private const val MAP_IR_BASE_URL = "https://map.ir"
private val MAP_IR_ROAD_SERVERS = arrayOf(MAP_IR_BASE_URL)

/**
 * Map.ir proxied Google Road tiles.
 *
 * This mirrors the React project's approach: instead of hitting
 * `mt*.google.com` directly (which can be blocked/rate-limited in Iran),
 * we go through `map.ir/raster/xyz` which acts as a CDN proxy.
 *
 * Requires [BuildConfig.MAP_IR_API_KEY].
 */
object MapIrGoogleRoadTileSource : ResilientTileSource(
    name = "MapIrGoogleRoad",
    minZoom = 3,
    maxZoom = 20,
    tileSizePx = 256,
    imageFileExtension = ".png",
    baseUrls = MAP_IR_ROAD_SERVERS,
) {
    private val encodedApiKey: String by lazy {
        URLEncoder.encode(BuildConfig.MAP_IR_API_KEY, "UTF-8")
    }

    override fun baseUrls(): Array<String> = MAP_IR_ROAD_SERVERS

    override fun buildTileUrl(zoom: Int, x: Int, y: Int): String {
        return "$MAP_IR_BASE_URL/raster/xyz/1.0.0/google:p@EPSG:900913@png/$zoom/$x/$y.png?x-api-key=$encodedApiKey"
    }
}
