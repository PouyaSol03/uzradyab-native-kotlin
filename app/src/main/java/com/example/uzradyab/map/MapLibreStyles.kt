package com.example.uzradyab.map

import com.example.uzradyab.BuildConfig

object MapLibreStyles {

    fun getStyleJson(styleId: String, isDarkTheme: Boolean = false): String {
        return when (styleId) {
            "googleRoad" -> buildRasterStyle(
                tiles = listOf(
                    "https://mt0.google.com/vt/lyrs=m&hl=fa&x={x}&y={y}&z={z}&scale=2&s=Ga",
                    "https://mt1.google.com/vt/lyrs=m&hl=fa&x={x}&y={y}&z={z}&scale=2&s=Ga",
                    "https://mt2.google.com/vt/lyrs=m&hl=fa&x={x}&y={y}&z={z}&scale=2&s=Ga",
                    "https://mt3.google.com/vt/lyrs=m&hl=fa&x={x}&y={y}&z={z}&scale=2&s=Ga"
                ),
                isDarkTheme = isDarkTheme
            )
            "googleSatellite" -> buildRasterStyle(
                tiles = listOf(
                    "https://mt0.google.com/vt/lyrs=y&hl=fa&x={x}&y={y}&z={z}&scale=2&s=Ga",
                    "https://mt1.google.com/vt/lyrs=y&hl=fa&x={x}&y={y}&z={z}&scale=2&s=Ga",
                    "https://mt2.google.com/vt/lyrs=y&hl=fa&x={x}&y={y}&z={z}&scale=2&s=Ga",
                    "https://mt3.google.com/vt/lyrs=y&hl=fa&x={x}&y={y}&z={z}&scale=2&s=Ga"
                ),
                isDarkTheme = isDarkTheme
            )
            "carto" -> buildRasterStyle(
                tiles = listOf(
                    BuildConfig.EXIR_TILE_BASE_URL.trimEnd('/') + "/{z}/{x}/{y}.png"
                ),
                isDarkTheme = isDarkTheme
            )
            "osm" -> buildRasterStyle(
                tiles = listOf(
                    "https://a.tile.openstreetmap.org/{z}/{x}/{y}.png",
                    "https://b.tile.openstreetmap.org/{z}/{x}/{y}.png",
                    "https://c.tile.openstreetmap.org/{z}/{x}/{y}.png"
                ),
                isDarkTheme = isDarkTheme
            )
            else -> buildRasterStyle(
                tiles = listOf("https://a.tile.openstreetmap.org/{z}/{x}/{y}.png"),
                isDarkTheme = isDarkTheme
            )
        }
    }

    private fun buildRasterStyle(tiles: List<String>, isDarkTheme: Boolean): String {
        val tilesJsonArray = tiles.joinToString(", ") { "\"$it\"" }
        val bgColor = if (isDarkTheme) "#11212C" else "#E8F0F6"
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
                  "id": "background",
                  "type": "background",
                  "paint": {
                    "background-color": "$bgColor"
                  }
                },
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
