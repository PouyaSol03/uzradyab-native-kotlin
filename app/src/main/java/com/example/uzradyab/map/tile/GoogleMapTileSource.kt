package com.example.uzradyab.map.tile

private val GOOGLE_ROAD_SERVERS = arrayOf(
    "https://mt0.google.com",
    "https://mt1.google.com",
    "https://mt2.google.com",
    "https://mt3.google.com",
)

/**
 * Google Road tile source using `mt0..mt3` subdomains with round-robin.
 *
 * - Persian labels (`hl=fa`)
 * - Session token (`s=Ga`) for stable tile serving
 * - Max zoom capped at 20 (actual Google Road limit)
 */
object GoogleMapTileSource : ResilientTileSource(
    name = "GoogleRoad",
    minZoom = 3,
    maxZoom = 20,
    tileSizePx = 256,
    imageFileExtension = ".png",
    baseUrls = GOOGLE_ROAD_SERVERS,
) {
    override fun baseUrls(): Array<String> = GOOGLE_ROAD_SERVERS

    override fun buildTileUrl(zoom: Int, x: Int, y: Int): String {
        val server = nextBaseUrl()
        return "$server/vt/lyrs=m&hl=fa&x=$x&y=$y&z=$zoom&s=Ga"
    }
}
