package com.example.uzradyab.map

import com.example.uzradyab.BuildConfig

object MapLibreStyles {

    fun getStyleJson(styleId: String): String {
        return when (styleId) {
            "googleRoad" -> buildRasterStyle(
                tiles = listOf(
                    "https://mt0.google.com/vt/lyrs=m&hl=fa&x={x}&y={y}&z={z}&scale=2&s=Ga",
                    "https://mt1.google.com/vt/lyrs=m&hl=fa&x={x}&y={y}&z={z}&scale=2&s=Ga",
                    "https://mt2.google.com/vt/lyrs=m&hl=fa&x={x}&y={y}&z={z}&scale=2&s=Ga",
                    "https://mt3.google.com/vt/lyrs=m&hl=fa&x={x}&y={y}&z={z}&scale=2&s=Ga"
                )
            )
            "googleSatellite" -> buildRasterStyle(
                tiles = listOf(
                    "https://mt0.google.com/vt/lyrs=y&hl=fa&x={x}&y={y}&z={z}&scale=2&s=Ga",
                    "https://mt1.google.com/vt/lyrs=y&hl=fa&x={x}&y={y}&z={z}&scale=2&s=Ga",
                    "https://mt2.google.com/vt/lyrs=y&hl=fa&x={x}&y={y}&z={z}&scale=2&s=Ga",
                    "https://mt3.google.com/vt/lyrs=y&hl=fa&x={x}&y={y}&z={z}&scale=2&s=Ga"
                )
            )
            "carto" -> buildRasterStyle(
                tiles = listOf(
                    BuildConfig.EXIR_TILE_BASE_URL.trimEnd('/') + "/{z}/{x}/{y}.png"
                )
            )
            "osm" -> buildRasterStyle(
                tiles = listOf(
                    "https://a.tile.openstreetmap.org/{z}/{x}/{y}.png",
                    "https://b.tile.openstreetmap.org/{z}/{x}/{y}.png",
                    "https://c.tile.openstreetmap.org/{z}/{x}/{y}.png"
                )
            )
            else -> buildRasterStyle(
                tiles = listOf("https://a.tile.openstreetmap.org/{z}/{x}/{y}.png")
            )
        }
    }

    private fun buildRasterStyle(tiles: List<String>): String {
        val tilesJsonArray = tiles.joinToString(", ") { "\"$it\"" }
        return """
            {
              "version": 8,
              "sources": {
                "raster-tiles": {
                  "type": "raster",
                  "tiles": [$tilesJsonArray],
                  "tileSize": 256
                }
              },
              "layers": [
                {
                  "id": "simple-tiles",
                  "type": "raster",
                  "source": "raster-tiles",
                  "minzoom": 0,
                  "maxzoom": 22
                }
              ]
            }
        """.trimIndent()
    }
}
