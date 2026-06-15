package com.example.uzradyab.map.tile

import com.example.uzradyab.BuildConfig
import java.net.URLEncoder

private const val MAP_IR_SAT_BASE_URL = "https://map.ir"
private val MAP_IR_SAT_SERVERS = arrayOf(MAP_IR_SAT_BASE_URL)

/**
 * Map.ir proxied satellite tiles.
 *
 * Same CDN-proxy pattern as [MapIrGoogleRoadTileSource] but for
 * satellite imagery. Avoids direct Google access in Iran.
 *
 * Requires [BuildConfig.MAP_IR_API_KEY].
 */
object MapIrSatelliteTileSource : ResilientTileSource(
    name = "MapIrSatellite",
    minZoom = 3,
    maxZoom = 20,
    tileSizePx = 256,
    imageFileExtension = ".png",
    baseUrls = MAP_IR_SAT_SERVERS,
) {
    private val encodedApiKey: String by lazy {
        URLEncoder.encode(BuildConfig.MAP_IR_API_KEY, "UTF-8")
    }

    override fun baseUrls(): Array<String> = MAP_IR_SAT_SERVERS

    override fun buildTileUrl(zoom: Int, x: Int, y: Int): String {
        return "$MAP_IR_SAT_BASE_URL/raster/xyz/1.0.0/satellite:p@EPSG:900913@png/$zoom/$x/$y.png?x-api-key=$encodedApiKey"
    }
}
