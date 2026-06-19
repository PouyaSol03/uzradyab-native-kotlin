package com.example.uzradyab.map.tile

import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * Singleton OkHttp-based tile fetcher.
 *
 * osmdroid's default tile downloader uses [java.net.HttpURLConnection] which
 * creates a **new TCP+TLS handshake per tile request**. This object provides
 * a shared [OkHttpClient] optimised for tile fetching:
 *
 * - **Connection pooling** — 10 idle connections, 2 min keep-alive
 * - **HTTP/2 multiplexing** — multiple tiles on a single connection
 * - **Short timeouts** — 5 s connect, 10 s read (tiles are small)
 * - **No auth overhead** — no cookies, interceptors, or session handling
 *
 * Used by map composables that need to pre-warm or directly fetch tiles
 * (e.g., for custom overlay rendering). The osmdroid downloader itself
 * still uses its own internal HTTP client for the standard tile provider chain.
 */
object OkHttpTileClient {

    private const val TAG = "OkHttpTileClient"

    /**
     * Dedicated tile-fetching client — separate from the app's shared client
     * to avoid carrying session cookies / running through auth interceptors.
     */
    val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectionPool(ConnectionPool(10, 2, TimeUnit.MINUTES))
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .followRedirects(true)
            .build()
    }

    /**
     * Fetch a single tile as a [Drawable].
     *
     * @param url       Full tile URL (e.g. from [OnlineTileSourceBase.getTileURLString])
     * @param userAgent Package name or custom UA string
     * @return The tile image as a [BitmapDrawable], or `null` on failure
     */
    fun fetchTile(url: String, userAgent: String): Drawable? {
        if (url.isBlank()) return null
        return try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", userAgent)
                .header("Accept", "image/png,image/*;q=0.9,*/*;q=0.8")
                .header("Connection", "keep-alive")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                response.body?.byteStream()?.use { stream ->
                    val bitmap = BitmapFactory.decodeStream(stream)
                    if (bitmap != null) {
                        BitmapDrawable(
                            android.content.res.Resources.getSystem(),
                            bitmap,
                        )
                    } else null
                }
            } else {
                Log.d(TAG, "Tile fetch ${response.code}: $url")
                response.close()
                null
            }
        } catch (e: Exception) {
            Log.d(TAG, "Tile fetch error: ${e.message}")
            null
        }
    }
}
