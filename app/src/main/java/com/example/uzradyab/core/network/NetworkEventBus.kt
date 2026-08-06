package com.example.uzradyab.core.network

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkEventBus @Inject constructor() {
    private val _networkErrorEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val networkErrorEvent = _networkErrorEvent.asSharedFlow()

    private val _maintenanceEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val maintenanceEvent = _maintenanceEvent.asSharedFlow()

    fun emitError() {
        _networkErrorEvent.tryEmit(Unit)
    }

    fun emitMaintenance() {
        _maintenanceEvent.tryEmit(Unit)
    }
}
