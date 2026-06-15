package com.example.uzradyab.map.tile

private val CARTO_SERVERS = arrayOf(
    "https://a.basemaps.cartocdn.com",
    "https://b.basemaps.cartocdn.com",
    "https://c.basemaps.cartocdn.com",
    "https://d.basemaps.cartocdn.com",
)

/**
 * CARTO Voyager raster tile source.
 *
 * - Uses `@2x` retina tiles (512 px served as 256 dp)
 * - Round-robin across 4 subdomains: `a`, `b`, `c`, `d`
 * - Max zoom 22 (CARTO's actual limit)
 * - Matches the React project's active "carto" configuration
 */
object CartoTileSource : ResilientTileSource(
    name = "CartoVoyager",
    minZoom = 3,
    maxZoom = 22,
    tileSizePx = 256,
    imageFileExtension = ".png",
    baseUrls = CARTO_SERVERS,
) {
    override fun baseUrls(): Array<String> = CARTO_SERVERS

    override fun buildTileUrl(zoom: Int, x: Int, y: Int): String {
        val server = nextBaseUrl()
        return "$server/rastertiles/voyager/$zoom/$x/$y@2x.png"
    }
}
