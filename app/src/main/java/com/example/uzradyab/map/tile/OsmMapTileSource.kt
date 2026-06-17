package com.example.uzradyab.map.tile

private val OSM_SERVERS = arrayOf(
    "https://a.tile.openstreetmap.org",
    "https://b.tile.openstreetmap.org",
    "https://c.tile.openstreetmap.org",
)

/**
 * OpenStreetMap default tile source.
 * 
 * Uses standard a/b/c subdomains for resilience.
 */
object OsmMapTileSource : ResilientTileSource(
    name = "Mapnik",
    minZoom = 0,
    maxZoom = 19,
    tileSizePx = 256,
    imageFileExtension = ".png",
    baseUrls = OSM_SERVERS,
) {
    override fun baseUrls(): Array<String> = OSM_SERVERS

    override fun buildTileUrl(zoom: Int, x: Int, y: Int): String {
        val server = nextBaseUrl()
        return "$server/$zoom/$x/$y.png"
    }
}
