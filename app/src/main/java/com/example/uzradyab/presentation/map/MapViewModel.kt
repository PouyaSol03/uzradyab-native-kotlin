package com.example.uzradyab.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uzradyab.domain.model.Device
import com.example.uzradyab.domain.model.Position
import com.example.uzradyab.domain.model.TrackingConnectionState
import com.example.uzradyab.domain.repository.AuthRepository
import com.example.uzradyab.domain.repository.TrackingRepository
import com.example.uzradyab.domain.usecase.ObserveHomeSnapshotUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeMapUiState(
    val devices: List<Device> = emptyList(),
    val latestPositions: Map<Long, Position> = emptyMap(),
    val selectedDeviceId: Long? = null,
    val devicesOpen: Boolean = true,
    val connectionState: TrackingConnectionState = TrackingConnectionState.Idle,
    val signedOut: Boolean = false,
    val todayDistanceText: String = "در حال دریافت",
)

@HiltViewModel
class MapViewModel @Inject constructor(
    observeHomeSnapshot: ObserveHomeSnapshotUseCase,
    private val authRepository: AuthRepository,
    private val trackingRepository: TrackingRepository,
) : ViewModel() {
    private val localState = MutableStateFlow(HomeMapUiState())

    val uiState: StateFlow<HomeMapUiState> = combine(
        observeHomeSnapshot(),
        trackingRepository.connectionState,
        localState,
    ) { snapshot, connection, local ->
        val selected = local.selectedDeviceId ?: snapshot.devices.firstOrNull()?.id
        local.copy(
            devices = snapshot.devices,
            latestPositions = snapshot.latestPositions,
            selectedDeviceId = selected,
            connectionState = connection,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeMapUiState())

    init {
        trackingRepository.start()
    }

    fun selectDevice(deviceId: Long) {
        localState.update { it.copy(selectedDeviceId = deviceId, devicesOpen = false) }
    }

    fun toggleDevices() {
        localState.update { it.copy(devicesOpen = !it.devicesOpen) }
    }

    fun logout() {
        viewModelScope.launch {
            trackingRepository.stop()
            authRepository.logout()
            localState.update { it.copy(signedOut = true) }
        }
    }
}
