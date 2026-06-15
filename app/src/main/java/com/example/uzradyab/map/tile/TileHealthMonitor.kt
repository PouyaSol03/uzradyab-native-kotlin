package com.example.uzradyab.map.tile

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.osmdroid.tileprovider.tilesource.ITileSource
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Health state emitted by [TileHealthMonitor].
 */
enum class TileHealthState {
    /** No check has run yet. */
    Unknown,

    /** Last probe succeeded. */
    Healthy,

    /** Last probe failed but retrying. */
    Degraded,

    /** Multiple consecutive failures. */
    Unreachable,
}

/**
 * Lifecycle-aware health monitor that periodically pings the current
 * tile source's health-check URL and exposes the result as a [StateFlow].
 *
 * - Base interval: 30 s
 * - After failure: exponential back-off up to 2 min
 * - After [MAX_CONSECUTIVE_FAILURES] consecutive failures, emits [TileHealthState.Unreachable]
 *   and provides the next fallback tile source via [suggestedFallback].
 *
 * The monitor uses a dedicated lightweight [OkHttpClient] with short
 * timeouts so it doesn't block the app's shared client pool.
 */
@Singleton
class TileHealthMonitor @Inject constructor() {

    private companion object {
        const val TAG = "TileHealthMonitor"
        const val BASE_INTERVAL_MS = 30_000L
        const val MAX_INTERVAL_MS = 120_000L
        const val MAX_CONSECUTIVE_FAILURES = 3
    }

    private val _state = MutableStateFlow(TileHealthState.Unknown)
    val state: StateFlow<TileHealthState> = _state.asStateFlow()

    private val _suggestedFallback = MutableStateFlow<ITileSource?>(null)
    val suggestedFallback: StateFlow<ITileSource?> = _suggestedFallback.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var monitorJob: Job? = null
    private var consecutiveFailures = 0

    private val probeClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(8, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)
            .build()
    }

    /**
     * Start monitoring for the given [styleId].
     * Cancels any previous monitoring session.
     */
    fun startMonitoring(styleId: String) {
        monitorJob?.cancel()
        consecutiveFailures = 0
        _state.value = TileHealthState.Unknown
        _suggestedFallback.value = null

        monitorJob = scope.launch {
            val healthUrl = TileSourceRegistry.healthCheckUrl(styleId)
            val chain = TileSourceRegistry.fallbackChain(styleId)
            var interval = BASE_INTERVAL_MS

            // Initial delay before first probe
            delay(BASE_INTERVAL_MS)

            while (true) {
                val healthy = probe(healthUrl)
                if (healthy) {
                    consecutiveFailures = 0
                    interval = BASE_INTERVAL_MS
                    _state.value = TileHealthState.Healthy
                    _suggestedFallback.value = null
                } else {
                    consecutiveFailures++
                    // Exponential back-off: 30s → 60s → 120s
                    interval = (interval * 2).coerceAtMost(MAX_INTERVAL_MS)

                    if (consecutiveFailures >= MAX_CONSECUTIVE_FAILURES) {
                        _state.value = TileHealthState.Unreachable
                        // Suggest the first fallback that isn't the primary
                        _suggestedFallback.value = chain.getOrNull(1)
                        Log.w(TAG, "Tile source unreachable after $consecutiveFailures failures")
                    } else {
                        _state.value = TileHealthState.Degraded
                    }
                }

                delay(interval)
            }
        }
    }

    /**
     * Stop monitoring. Call when leaving the map screen.
     */
    fun stopMonitoring() {
        monitorJob?.cancel()
        monitorJob = null
        _state.value = TileHealthState.Unknown
        _suggestedFallback.value = null
    }

    private fun probe(url: String): Boolean {
        return try {
            val request = Request.Builder()
                .url(url)
                .head() // HEAD request — only check reachability, don't download body
                .build()
            val response = probeClient.newCall(request).execute()
            val success = response.isSuccessful
            response.close()
            success
        } catch (e: Exception) {
            Log.d(TAG, "Health probe failed: ${e.message}")
            false
        }
    }
}
