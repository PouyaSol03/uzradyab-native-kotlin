package com.example.uzradyab.map.tile

import android.graphics.drawable.Drawable
import android.util.Log
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.Request
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.MapTileRequestState
import org.osmdroid.tileprovider.modules.IFilesystemCache
import org.osmdroid.tileprovider.modules.INetworkAvailablityCheck
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase
import org.osmdroid.tileprovider.tilesource.ITileSource
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import java.io.InputStream
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/**
 * A robust tile downloader module for Osmdroid that relies on OkHttp.
 * It manually streams the downloaded bytes into Osmdroid's SQLite cache (IFilesystemCache)
 * to ensure that tiles are cached offline accurately.
 */
class OkHttpTileModuleProvider(
    private val tileSource: ITileSource,
    private val filesystemCache: IFilesystemCache?,
    private val networkAvailabilityCheck: INetworkAvailablityCheck,
    threadPoolSize: Int = Configuration.getInstance().tileDownloadThreads.toInt(),
    pendingQueueSize: Int = Configuration.getInstance().tileDownloadMaxQueueSize.toInt()
) : MapTileModuleProviderBase(threadPoolSize, pendingQueueSize) {

    private val client: OkHttpClient
        get() = MapDependencies.okHttpClient

    private val tileSourceRef = AtomicReference(tileSource)

    override fun getUsesDataConnection(): Boolean = true

    override fun getName(): String = "OkHttpTileModuleProvider"

    override fun getThreadGroupName(): String = "OkHttpTileModuleProvider"

    override fun getMinimumZoomLevel(): Int = tileSourceRef.get()?.minimumZoomLevel ?: 0

    override fun getMaximumZoomLevel(): Int = tileSourceRef.get()?.maximumZoomLevel ?: 22

    override fun getTileLoader(): TileLoader = OkHttpTileLoader()

    override fun setTileSource(newTileSource: ITileSource?) {
        if (newTileSource != null) {
            tileSourceRef.set(newTileSource)
        }
    }

    private inner class OkHttpTileLoader : TileLoader() {
        override fun loadTile(pMapTileIndex: Long): Drawable? {
            val source = tileSourceRef.get() ?: return null
            if (source !is OnlineTileSourceBase) return null

            if (!networkAvailabilityCheck.networkAvailable) {
                return null
            }

            val tileUrl = source.getTileURLString(pMapTileIndex)
            if (tileUrl.isNullOrBlank()) return null

            val userAgent = Configuration.getInstance().userAgentValue ?: "UzradyabNative/1.0"

            val request = Request.Builder()
                .url(tileUrl)
                .header("User-Agent", userAgent)
                .header("Connection", "keep-alive")
                .header("Accept", "image/png,image/*;q=0.9,*/*;q=0.8")
                .build()

            return try {
                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    Log.w("OkHttpTileModule", "Failed to load tile: $tileUrl, code: ${response.code}")
                    response.close()
                    return null
                }

                // Buffer the entire stream so we can both save it to cache and create a Drawable
                val bytes = response.body?.bytes() ?: return null

                // 1. Save directly to osmdroid's SQLite cache with a 1-year expiration
                if (filesystemCache != null) {
                    try {
                        val bais = java.io.ByteArrayInputStream(bytes)
                        val expirationTime = System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000
                        filesystemCache.saveFile(source, pMapTileIndex, bais, expirationTime)
                    } catch (e: Exception) {
                        Log.e("OkHttpTileModule", "Error saving tile to cache", e)
                    }
                }

                // 2. Create the Drawable from the same bytes to return to the map
                try {
                    val bais = java.io.ByteArrayInputStream(bytes)
                    source.getDrawable(bais)
                } catch (e: Exception) {
                    Log.e("OkHttpTileModule", "Error creating Drawable from tile bytes", e)
                    null
                }
            } catch (e: Exception) {
                Log.e("OkHttpTileModule", "Error downloading tile: $tileUrl", e)
                null
            }
        }
    }
}
