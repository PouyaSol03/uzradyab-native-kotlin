package com.example.uzradyab.data.repository

import com.example.uzradyab.data.local.dao.DeviceDao
import com.example.uzradyab.data.local.dao.EventDao
import com.example.uzradyab.data.local.dao.PositionDao
import com.example.uzradyab.data.mapper.toEntity
import com.example.uzradyab.data.remote.api.TraccarApi
import com.example.uzradyab.data.remote.websocket.SocketEvent
import com.example.uzradyab.data.remote.websocket.TraccarSocketClient
import com.example.uzradyab.domain.model.TrackingConnectionState
import com.example.uzradyab.domain.repository.TrackingRepository
import com.example.uzradyab.domain.repository.MapSettingsRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

@Singleton
class TrackingRepositoryImpl @Inject constructor(
    private val socketClient: TraccarSocketClient,
    private val api: TraccarApi,
    private val deviceDao: DeviceDao,
    private val positionDao: PositionDao,
    private val eventDao: EventDao,
    private val mapSettingsRepository: MapSettingsRepository,
) : TrackingRepository {
    private val _connectionState = MutableStateFlow(TrackingConnectionState.Idle)
    override val connectionState: StateFlow<TrackingConnectionState> = _connectionState.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var socketJob: Job? = null
    private var fallbackJob: Job? = null
    private var watchdogJob: Job? = null
    private var settingsJob: Job? = null
    private var stopped = false

    @Volatile
    private var trackedDeviceIds: Set<Long> = emptySet()

    /** Timestamp of the last received WebSocket message, for the watchdog. */
    @Volatile
    private var lastMessageTimeMs: Long = 0L

    /** If no message arrives within this window, treat socket as dead. */
    private companion object {
        const val WATCHDOG_INTERVAL_MS = 30_000L
        const val STALE_THRESHOLD_MS = 90_000L
    }

    override fun start() {
        if (socketJob?.isActive == true) return
        stopped = false
        
        settingsJob = scope.launch {
            mapSettingsRepository.observeTrackedDeviceIds().collect { ids ->
                trackedDeviceIds = ids
            }
        }
        
        socketJob = scope.launch {
            var backoffMs = 2_000L
            while (!stopped) {
                _connectionState.value = TrackingConnectionState.Connecting
                socketClient.connect()
                    .catch {
                        handleSocketDisconnected()
                    }
                    .collect { event ->
                        when (event) {
                            SocketEvent.Opened -> {
                                backoffMs = 2_000L
                                lastMessageTimeMs = System.currentTimeMillis()
                                _connectionState.value = TrackingConnectionState.Connected
                                stopFallbackPolling()
                                startWatchdog()
                            }
                            is SocketEvent.Message -> {
                                lastMessageTimeMs = System.currentTimeMillis()
                                persistSocketMessage(event)
                            }
                            is SocketEvent.Closed -> {
                                if (event.code != 4000) handleSocketDisconnected()
                            }
                            is SocketEvent.Failed -> handleSocketDisconnected()
                        }
                    }
                if (!stopped) {
                    delay(backoffMs)
                    backoffMs = (backoffMs * 2).coerceAtMost(60_000L)
                }
            }
        }
    }

    override fun stop() {
        stopped = true
        socketJob?.cancel()
        fallbackJob?.cancel()
        watchdogJob?.cancel()
        settingsJob?.cancel()
        _connectionState.value = TrackingConnectionState.Idle
    }

    override fun startFallbackPolling() {
        if (fallbackJob?.isActive == true) return
        fallbackJob = scope.launch {
            _connectionState.value = TrackingConnectionState.PollingFallback
            while (!stopped && _connectionState.value == TrackingConnectionState.PollingFallback) {
                runCatching {
                    positionDao.upsertLatest(api.getPositions().map { it.toEntity(isLatest = true) })
                }
                delay(45_000L)
            }
        }
    }

    override fun stopFallbackPolling() {
        fallbackJob?.cancel()
        fallbackJob = null
    }

    private suspend fun persistSocketMessage(event: SocketEvent.Message) {
        event.data.devices?.takeIf { it.isNotEmpty() }?.let { devices ->
            deviceDao.upsertAll(devices.map { it.toEntity() })
        }

        val allowedIds = trackedDeviceIds
        if (allowedIds.isNotEmpty()) {
            event.data.positions?.filter { allowedIds.contains(it.deviceId) }?.takeIf { it.isNotEmpty() }?.let { positions ->
                positionDao.upsertLatest(positions.map { it.toEntity(isLatest = true) })
            }
            event.data.events?.filter { allowedIds.contains(it.deviceId) }?.takeIf { it.isNotEmpty() }?.let { events ->
                eventDao.upsertAll(events.map { it.toEntity() })
            }
        }
    }

    private fun handleSocketDisconnected() {
        if (stopped) return
        watchdogJob?.cancel()
        _connectionState.value = TrackingConnectionState.Disconnected
        startFallbackPolling()
    }

    /**
     * Watchdog coroutine: every [WATCHDOG_INTERVAL_MS] checks if the last
     * WebSocket message is older than [STALE_THRESHOLD_MS]. If so, treats
     * the connection as dead and triggers reconnection — much faster than
     * waiting for TCP keepalive timeout (2+ hours).
     */
    private fun startWatchdog() {
        watchdogJob?.cancel()
        watchdogJob = scope.launch {
            while (!stopped && _connectionState.value == TrackingConnectionState.Connected) {
                delay(WATCHDOG_INTERVAL_MS)
                val elapsed = System.currentTimeMillis() - lastMessageTimeMs
                if (elapsed > STALE_THRESHOLD_MS) {
                    android.util.Log.w("TrackingRepo", "WebSocket stale (${elapsed}ms), reconnecting")
                    socketJob?.cancel()
                    handleSocketDisconnected()
                    break
                }
            }
        }
    }
}
