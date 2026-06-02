package com.example.uzradyab.domain.repository

import com.example.uzradyab.domain.model.TrackingConnectionState
import kotlinx.coroutines.flow.StateFlow

interface TrackingRepository {
    val connectionState: StateFlow<TrackingConnectionState>
    fun start()
    fun stop()
    fun startFallbackPolling()
    fun stopFallbackPolling()
}
