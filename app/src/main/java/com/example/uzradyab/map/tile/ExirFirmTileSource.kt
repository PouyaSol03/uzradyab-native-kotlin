package com.example.uzradyab.map.tile

import com.example.uzradyab.BuildConfig
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.util.MapTileIndex

private const val TILE_SIZE_PX = 256

class ExirFirmTileSource(
    private val baseUrl: String = BuildConfig.EXIR_TILE_BASE_URL,
) : OnlineTileSourceBase(
    "ExirFirmOSM",
    3,
    19,
    TILE_SIZE_PX,
    ".png",
    arrayOf(baseUrl),
) {
    override fun getTileURLString(pMapTileIndex: Long): String {
        val normalizedBase = baseUrl.trimEnd('/') + "/"
        val zoom = MapTileIndex.getZoom(pMapTileIndex)
        val x = MapTileIndex.getX(pMapTileIndex)
        val y = MapTileIndex.getY(pMapTileIndex)
        return "$normalizedBase$zoom/$x/$y.png"
    }
}
