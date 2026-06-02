package com.example.uzradyab.data.repository

import com.example.uzradyab.domain.model.TrackingConnectionState
import com.example.uzradyab.domain.repository.TrackingRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class TrackingRepositoryImpl @Inject constructor() : TrackingRepository {
    private val _connectionState = MutableStateFlow(TrackingConnectionState.Idle)
    override val connectionState: StateFlow<TrackingConnectionState> = _connectionState.asStateFlow()

    override fun start() {
        if (_connectionState.value == TrackingConnectionState.Idle) {
            _connectionState.value = TrackingConnectionState.Connecting
        }
    }

    override fun stop() {
        _connectionState.value = TrackingConnectionState.Idle
    }

    override fun startFallbackPolling() {
        _connectionState.value = TrackingConnectionState.PollingFallback
    }

    override fun stopFallbackPolling() {
        if (_connectionState.value == TrackingConnectionState.PollingFallback) {
            _connectionState.value = TrackingConnectionState.Disconnected
        }
    }
}
