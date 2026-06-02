package com.example.uzradyab.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uzradyab.domain.model.Device
import com.example.uzradyab.domain.model.Position
import com.example.uzradyab.domain.model.TrackingConnectionState
import com.example.uzradyab.domain.repository.AuthRepository
import com.example.uzradyab.domain.repository.ReportRepository
import com.example.uzradyab.domain.repository.TrackingRepository
import com.example.uzradyab.domain.usecase.ObserveHomeSnapshotUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeMapUiState(
    val devices: List<Device> = emptyList(),
    val latestPositions: Map<Long, Position> = emptyMap(),
    val selectedDeviceId: Long? = null,
    val devicesOpen: Boolean = false,
    val deviceCardExpanded: Boolean = false,
    val deviceManagementOpen: Boolean = false,
    val connectionState: TrackingConnectionState = TrackingConnectionState.Idle,
    val signedOut: Boolean = false,
    val todayDistanceText: String = "در حال دریافت",
)

@HiltViewModel
class MapViewModel @Inject constructor(
    observeHomeSnapshot: ObserveHomeSnapshotUseCase,
    private val authRepository: AuthRepository,
    private val reportRepository: ReportRepository,
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
        observeSelectedDeviceDistance()
    }

    fun selectDevice(deviceId: Long) {
        localState.update {
            it.copy(
                selectedDeviceId = deviceId,
                devicesOpen = false,
                deviceManagementOpen = false,
            )
        }
    }

    fun toggleDevices() {
        localState.update { it.copy(devicesOpen = !it.devicesOpen, deviceManagementOpen = false) }
    }

    fun toggleDeviceCard() {
        localState.update { it.copy(deviceCardExpanded = !it.deviceCardExpanded) }
    }

    fun openDeviceManagement() {
        localState.update {
            it.copy(
                deviceCardExpanded = true,
                deviceManagementOpen = true,
                devicesOpen = false,
            )
        }
    }

    fun closeDeviceManagement() {
        localState.update { it.copy(deviceManagementOpen = false) }
    }

    fun logout() {
        viewModelScope.launch {
            trackingRepository.stop()
            authRepository.logout()
            localState.update { it.copy(signedOut = true) }
        }
    }

    private fun observeSelectedDeviceDistance() {
        viewModelScope.launch {
            uiState
                .map { it.selectedDeviceId }
                .distinctUntilChanged()
                .collectLatest { deviceId ->
                    if (deviceId == null) {
                        localState.update { it.copy(todayDistanceText = "نامشخص") }
                        return@collectLatest
                    }

                    val range = utcTodayRange()
                    localState.update { it.copy(todayDistanceText = "در حال دریافت") }
                    coroutineScope {
                        launch {
                            reportRepository.refreshDailyDistance(
                                deviceId = deviceId,
                                date = range.date,
                                from = range.from,
                                to = range.to,
                            ).onFailure {
                                localState.update { state ->
                                    if (state.todayDistanceText == "در حال دریافت") {
                                        state.copy(todayDistanceText = "داده موجود نیست")
                                    } else {
                                        state
                                    }
                                }
                            }
                        }
                        reportRepository.observeDailyDistance(deviceId, range.date)
                            .collect { dailyDistance ->
                                localState.update {
                                    it.copy(
                                        todayDistanceText = dailyDistance?.distanceMeters
                                            ?.let(::formatDistance)
                                            ?: "در حال دریافت",
                                    )
                                }
                            }
                    }
                }
        }
    }
}

private data class UtcDayRange(
    val date: String,
    val from: String,
    val to: String,
)

private fun utcTodayRange(now: Date = Date()): UtcDayRange {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    val date = formatter.format(now)
    return UtcDayRange(
        date = date,
        from = "${date}T00:00:00.000Z",
        to = "${date}T23:59:59.999Z",
    )
}

private fun formatDistance(distanceMeters: Double): String {
    return if (distanceMeters < 1_000.0) {
        "${distanceMeters.roundToInt()} متر"
    } else {
        String.format(Locale.US, "%.1f کیلومتر", distanceMeters / 1_000.0)
    }
}
