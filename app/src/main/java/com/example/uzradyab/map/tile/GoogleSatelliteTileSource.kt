package com.example.uzradyab.map.tile

private val GOOGLE_SAT_SERVERS = arrayOf(
    "https://mt0.google.com",
    "https://mt1.google.com",
    "https://mt2.google.com",
    "https://mt3.google.com",
)

/**
 * Google Hybrid (satellite + labels) tile source using `mt0..mt3`
 * with round-robin rotation.
 *
 * - `lyrs=y` = satellite imagery with road/label overlay
 * - Persian labels (`hl=fa`)
 * - Session token (`s=Ga`)
 * - Max zoom capped at 20 (actual Google Satellite limit)
 */
object GoogleSatelliteTileSource : ResilientTileSource(
    name = "GoogleSatellite",
    minZoom = 3,
    maxZoom = 20,
    tileSizePx = 256,
    imageFileExtension = ".png",
    baseUrls = GOOGLE_SAT_SERVERS,
) {
    override fun baseUrls(): Array<String> = GOOGLE_SAT_SERVERS

    override fun buildTileUrl(zoom: Int, x: Int, y: Int): String {
        val server = nextBaseUrl()
        return "$server/vt/lyrs=y&hl=fa&x=$x&y=$y&z=$zoom&s=Ga"
    }
}
