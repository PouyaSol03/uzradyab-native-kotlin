package com.example.uzradyab.map.tile

import com.example.uzradyab.BuildConfig

/**
 * Exir Firm self-hosted OSM tile source.
 *
 * - Reads base URL from [BuildConfig.EXIR_TILE_BASE_URL]
 * - Normalises trailing slash
 * - Max zoom capped at 19 (server-side limit)
 */
object ExirFirmTileSource : ResilientTileSource(
    name = "ExirFirmOSM",
    minZoom = 3,
    maxZoom = 19,
    tileSizePx = 256,
    imageFileExtension = ".png",
    baseUrls = arrayOf(BuildConfig.EXIR_TILE_BASE_URL),
) {
    private val normalizedBase: String by lazy {
        BuildConfig.EXIR_TILE_BASE_URL.trimEnd('/') + "/"
    }

    override fun baseUrls(): Array<String> =
        arrayOf(BuildConfig.EXIR_TILE_BASE_URL)

    override fun buildTileUrl(zoom: Int, x: Int, y: Int): String {
        return "$normalizedBase$zoom/$x/$y.png"
    }
}
