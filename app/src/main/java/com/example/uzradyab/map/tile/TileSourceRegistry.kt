package com.example.uzradyab.map.tile

import org.osmdroid.tileprovider.tilesource.ITileSource
import org.osmdroid.tileprovider.tilesource.TileSourceFactory

/**
 * Central registry that maps user-facing style IDs to tile sources
 * and provides a fallback chain when the primary source is unreachable.
 *
 * Style IDs stored in [MapSettingsRepository]:
 *   `"osm"`, `"googleRoad"`, `"googleSatellite"`, `"carto"`,
 *   `"openTopo"`, `"exir"`
 */
object TileSourceRegistry {

    /**
     * Represents a single map style entry visible in the settings dialog.
     */
    data class MapStyle(
        val id: String,
        val title: String,
        /** Primary tile source to try first. */
        val primary: ITileSource,
        /** Ordered list of fallbacks if primary fails. */
        val fallbacks: List<ITileSource> = emptyList(),
        /** A test URL that can be pinged for health checks. */
        val healthCheckUrl: String,
    )

    /**
     * All available map styles in display order.
     * The settings dialog can iterate this list.
     */
    val availableStyles: List<MapStyle> = listOf(
        MapStyle(
            id = "osm",
            title = "نقشه",
            primary = TileSourceFactory.MAPNIK,
            fallbacks = listOf(CartoTileSource),
            healthCheckUrl = "https://tile.openstreetmap.org/1/1/1.png",
        ),
        MapStyle(
            id = "googleRoad",
            title = "جاده گوگل",
            primary = MapIrGoogleRoadTileSource,
            fallbacks = listOf(GoogleMapTileSource, TileSourceFactory.MAPNIK),
            healthCheckUrl = "https://map.ir/raster/xyz/1.0.0/google:p@EPSG:900913@png/1/1/1.png",
        ),
        MapStyle(
            id = "googleSatellite",
            title = "ماهواره‌ای گوگل",
            primary = MapIrSatelliteTileSource,
            fallbacks = listOf(GoogleSatelliteTileSource, TileSourceFactory.MAPNIK),
            healthCheckUrl = "https://map.ir/raster/xyz/1.0.0/satellite:p@EPSG:900913@png/1/1/1.png",
        ),
        MapStyle(
            id = "carto",
            title = "اکسیر",
            primary = ExirFirmTileSource,
            fallbacks = listOf(CartoTileSource, TileSourceFactory.MAPNIK),
            healthCheckUrl = com.example.uzradyab.BuildConfig.EXIR_TILE_BASE_URL.trimEnd('/') + "/1/1/1.png",
        ),
    )

    /** Fast ID → MapStyle lookup. */
    private val styleMap: Map<String, MapStyle> = availableStyles.associateBy { it.id }

    /**
     * Resolve a style ID to its primary tile source.
     * Falls back to OSM (MAPNIK) if the ID is unknown.
     */
    fun resolve(styleId: String): ITileSource {
        return styleMap[styleId]?.primary ?: TileSourceFactory.MAPNIK
    }

    /**
     * Get the full [MapStyle] entry for a style ID.
     * Returns the OSM entry if the ID is unknown.
     */
    fun getStyle(styleId: String): MapStyle {
        return styleMap[styleId] ?: availableStyles.first()
    }

    /**
     * Given a style ID, return the ordered fallback chain
     * (primary first, then fallbacks).
     */
    fun fallbackChain(styleId: String): List<ITileSource> {
        val style = getStyle(styleId)
        return listOf(style.primary) + style.fallbacks
    }

    /**
     * Get the health-check URL for a given style ID.
     */
    fun healthCheckUrl(styleId: String): String {
        return getStyle(styleId).healthCheckUrl
    }
}
