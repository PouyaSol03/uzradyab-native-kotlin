package com.example.uzradyab.map

import android.content.Context
import org.osmdroid.config.Configuration

/**
 * Centralised osmdroid configuration.
 * Call [configure] once from the composable factory lambda instead of
 * repeating the same block in every map screen.
 */
object OsmdroidConfig {

    private const val OSMDROID_PREFS = "osmdroid"

    /** Max on-disk tile cache in bytes (500 MB). */
    private const val CACHE_MAX_BYTES = 500L * 1024 * 1024

    /** Trim target when cache exceeds max (400 MB). */
    private const val CACHE_TRIM_BYTES = 400L * 1024 * 1024


    /** Parallel tile-download threads (default is 2). */
    private const val TILE_DOWNLOAD_THREADS = 4

    /** Max queued tiles awaiting download (default is 40). */
    private const val TILE_DOWNLOAD_MAX_QUEUE = 80

    /** Max queued tiles awaiting filesystem I/O (default is 40). */
    private const val TILE_FS_MAX_QUEUE = 80

    /**
     * Apply all configuration values.
     * Safe to call multiple times — idempotent.
     */
    fun configure(context: Context) {
        val prefs = context.getSharedPreferences(OSMDROID_PREFS, Context.MODE_PRIVATE)
        Configuration.getInstance().apply {
            load(context, prefs)
            
            // Fix caching on modern Android (Android 10+) by explicitly setting cache paths to internal storage
            val basePath = java.io.File(context.cacheDir, "osmdroid").apply { mkdirs() }
            val tileCache = java.io.File(basePath, "tiles").apply { mkdirs() }
            osmdroidBasePath = basePath
            osmdroidTileCache = tileCache
            
            // Enable debug logging to see tile fetch delay in Logcat (tag: OsmDroid)
            isDebugMode = false
            isDebugMapTileDownloader = true
            isDebugTileProviders = false

            userAgentValue = context.packageName
            tileFileSystemCacheMaxBytes = CACHE_MAX_BYTES
            tileFileSystemCacheTrimBytes = CACHE_TRIM_BYTES
            

            
            tileDownloadThreads = TILE_DOWNLOAD_THREADS.toShort()
            tileDownloadMaxQueueSize = TILE_DOWNLOAD_MAX_QUEUE.toShort()
            tileFileSystemMaxQueueSize = TILE_FS_MAX_QUEUE.toShort()

            // Global HTTP headers applied to every tile request
            additionalHttpRequestProperties["User-Agent"] = context.packageName
            additionalHttpRequestProperties["Accept"] = "image/png,image/*;q=0.9,*/*;q=0.8"
            additionalHttpRequestProperties["Connection"] = "keep-alive"
        }
    }
}
