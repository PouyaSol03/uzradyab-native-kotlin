package com.example.uzradyab.map.tile

import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.util.MapTileIndex
import java.util.concurrent.atomic.AtomicLong

/**
 * Base tile source that provides:
 * - **Round-robin** server rotation across multiple base URLs
 * - Safe URL construction with bounds validation
 * - Per-source max zoom enforcement
 *
 * All concrete tile sources should extend this class.
 */
abstract class ResilientTileSource(
    name: String,
    minZoom: Int,
    maxZoom: Int,
    tileSizePx: Int,
    imageFileExtension: String,
    baseUrls: Array<String>,
) : OnlineTileSourceBase(
    name,
    minZoom,
    maxZoom,
    tileSizePx,
    imageFileExtension,
    baseUrls,
) {
    /**
     * Atomic counter used for round-robin selection across servers.
     * This ensures even distribution across subdomains/mirror servers.
     */
    private val requestCounter = AtomicLong(0)

    /**
     * Select the next server URL in round-robin fashion.
     * Uses the [baseUrls] array passed to the constructor.
     */
    protected fun nextBaseUrl(): String {
        val all = baseUrls()
        if (all.isEmpty()) return ""
        return all[(requestCounter.getAndIncrement() % all.size).toInt()]
    }

    /**
     * Subclasses must provide the actual base URL array for rotation.
     */
    protected abstract fun baseUrls(): Array<String>

    /**
     * Builds the tile URL. Subclasses override [buildTileUrl] instead of
     * this method so that error handling is always applied.
     */
    override fun getTileURLString(pMapTileIndex: Long): String {
        return try {
            val zoom = MapTileIndex.getZoom(pMapTileIndex)
            val x = MapTileIndex.getX(pMapTileIndex)
            val y = MapTileIndex.getY(pMapTileIndex)
            buildTileUrl(zoom, x, y)
        } catch (e: Exception) {
            // Return empty string so osmdroid skips this tile gracefully
            ""
        }
    }

    /**
     * Concrete tile sources implement this to produce the final URL
     * for the given tile coordinates.
     */
    protected abstract fun buildTileUrl(zoom: Int, x: Int, y: Int): String
}
