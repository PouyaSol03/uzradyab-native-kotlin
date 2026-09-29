package com.example.uzradyab.map

import com.example.uzradyab.BuildConfig
import org.maplibre.android.maps.Style

object MapLibreStyles {

    // Official Neshan Vector Tile Style URLs
    const val NESHAN_STANDARD_LIGHT = "https://static.neshan.org/sdk/maplibre/styles/light.json"
    const val NESHAN_STANDARD_DARK = "https://static.neshan.org/sdk/maplibre/styles/dark.json"
    const val NESHAN_MONOCHROME_LIGHT = "https://static.neshan.org/sdk/maplibre/styles/monochrome_light.json"
    const val NESHAN_MONOCHROME_DARK = "https://static.neshan.org/sdk/maplibre/styles/monochrome_dark.json"
    const val NESHAN_PASTEL = "https://static.neshan.org/sdk/maplibre/styles/pastel.json"

    /**
     * Builds and returns a [Style.Builder] appropriate for the given [styleId].
     * For Neshan vector styles (or any remote URI), it configures [Style.Builder.fromUri].
     * For custom raster styles, it configures [Style.Builder.fromJson].
     */
    fun getStyleBuilder(styleId: String, isDarkTheme: Boolean = false): Style.Builder {
        val uri = getStyleUri(styleId, isDarkTheme)
        return if (uri != null) {
            Style.Builder().fromUri(uri)
        } else {
            Style.Builder().fromJson(getStyleJson(styleId, isDarkTheme))
        }
    }

    /**
     * Resolves the remote vector style URI for Neshan vector styles.
     * Returns null if the style should be rendered via custom raster JSON.
     */
    fun getStyleUri(styleId: String, isDarkTheme: Boolean = false): String? {
        return when (styleId) {
            "neshan", "neshanVector", "osm" -> if (isDarkTheme) NESHAN_STANDARD_DARK else NESHAN_STANDARD_LIGHT
            "neshanDark" -> NESHAN_STANDARD_DARK
            "neshanLight" -> NESHAN_STANDARD_LIGHT
            "neshanPastel" -> NESHAN_PASTEL
            "neshanMonochrome" -> if (isDarkTheme) NESHAN_MONOCHROME_DARK else NESHAN_MONOCHROME_LIGHT
            else -> {
                if (styleId.startsWith("http://") || styleId.startsWith("https://")) {
                    styleId
                } else {
                    null
                }
            }
        }
    }

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
