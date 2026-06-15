package com.example.uzradyab.map.tile

private const val OPEN_TOPO_BASE_URL = "https://tile.opentopomap.org"
private val OPEN_TOPO_SERVERS = arrayOf(OPEN_TOPO_BASE_URL)

/**
 * OpenTopoMap tile source.
 *
 * - Max zoom capped at 17 (server-side limit for OpenTopoMap)
 * - Single server, no round-robin needed
 */
object OpenTopoMapTileSource : ResilientTileSource(
    name = "OpenTopoMap",
    minZoom = 3,
    maxZoom = 17,
    tileSizePx = 256,
    imageFileExtension = ".png",
    baseUrls = OPEN_TOPO_SERVERS,
) {
    override fun baseUrls(): Array<String> = OPEN_TOPO_SERVERS

    override fun buildTileUrl(zoom: Int, x: Int, y: Int): String {
        return "$OPEN_TOPO_BASE_URL/$zoom/$x/$y.png"
    }
}
