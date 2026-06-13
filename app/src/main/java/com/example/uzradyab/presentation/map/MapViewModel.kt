package com.example.uzradyab.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uzradyab.domain.model.Device
import com.example.uzradyab.domain.model.Event
import com.example.uzradyab.domain.model.LatestNotificationEvent
import com.example.uzradyab.domain.model.Position
import com.example.uzradyab.domain.model.TrackingConnectionState
import com.example.uzradyab.domain.repository.AuthRepository
import com.example.uzradyab.domain.repository.EventRepository
import com.example.uzradyab.domain.repository.MapSettingsRepository
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
import kotlinx.coroutines.delay
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
    val latestEvent: MapLatestEventItem? = null,
    val mapSettingsOpen: Boolean = false,
    val mapStyle: String = "osm",
)

data class MapLatestEventItem(
    val text: String,
    val timeText: String?,
)

@HiltViewModel
class MapViewModel @Inject constructor(
    observeHomeSnapshot: ObserveHomeSnapshotUseCase,
    private val authRepository: AuthRepository,
    private val eventRepository: EventRepository,
    private val reportRepository: ReportRepository,
    private val trackingRepository: TrackingRepository,
    private val mapSettingsRepository: MapSettingsRepository,
) : ViewModel() {
    private val localState = MutableStateFlow(HomeMapUiState())

    val uiState: StateFlow<HomeMapUiState> = combine(
        observeHomeSnapshot(),
        trackingRepository.connectionState,
        localState,
        eventRepository.observeRecentEvents(limit = 8),
        mapSettingsRepository.observeMapStyle(),
    ) { snapshot, connection, local, recentEvents, mapStyle ->
        val selected = local.selectedDeviceId ?: snapshot.devices.firstOrNull()?.id
        local.copy(
            devices = snapshot.devices,
            latestPositions = snapshot.latestPositions,
            selectedDeviceId = selected,
            connectionState = connection,
            latestEvent = local.latestEvent ?: latestEventForDevice(recentEvents, selected),
            mapStyle = mapStyle,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeMapUiState())

    init {
        trackingRepository.start()
        observeSelectedDeviceDistance()
        observeSelectedDeviceLatestEvent()
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

    fun openMapSettings() {
        localState.update { it.copy(mapSettingsOpen = true, devicesOpen = false) }
    }

    fun closeMapSettings() {
        localState.update { it.copy(mapSettingsOpen = false) }
    }

    fun setMapStyle(style: String) {
        viewModelScope.launch {
            mapSettingsRepository.setMapStyle(style)
        }
        closeMapSettings()
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

    private fun observeSelectedDeviceLatestEvent() {
        viewModelScope.launch {
            uiState
                .map { it.selectedDeviceId }
                .distinctUntilChanged()
                .collectLatest { deviceId ->
                    if (deviceId == null) {
                        localState.update { it.copy(latestEvent = null) }
                        return@collectLatest
                    }
                    localState.update { it.copy(latestEvent = null) }
                    while (true) {
                        eventRepository.fetchLatestDeviceEvents(deviceId)
                            .onSuccess { events ->
                                localState.update { state ->
                                    state.copy(latestEvent = events.firstOrNull()?.toTickerItem())
                                }
                            }
                        delay(120_000L)
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
        "${distanceMeters.roundToInt()} متر".toPersianDigits()
    } else {
        String.format(Locale.US, "%.1f کیلومتر", distanceMeters / 1_000.0).toPersianDigits()
    }
}

private fun latestEventForDevice(events: List<Event>, deviceId: Long?): MapLatestEventItem? {
    val event = events.firstOrNull { it.deviceId == deviceId } ?: events.firstOrNull()
    return event?.let {
        MapLatestEventItem(
            text = formatEventText(it),
            timeText = formatEventTime(it.eventTime),
        )
    }
}

private fun LatestNotificationEvent.toTickerItem(): MapLatestEventItem {
    return MapLatestEventItem(
        text = text,
        timeText = formatEventTime(time),
    )
}

private fun formatEventText(event: Event): String {
    return when (event.type) {
        "deviceOnline" -> "دستگاه آنلاین شد"
        "deviceOffline" -> "دستگاه آفلاین شد"
        "deviceUnknown" -> "وضعیت دستگاه نامشخص شد"
        "ignitionOn" -> "روشن شدن خودرو"
        "ignitionOff" -> "خاموش شدن خودرو"
        "geofenceEnter" -> "ورود به محدوده"
        "geofenceExit" -> "خروج از محدوده"
        "alarm" -> "هشدار دستگاه"
        else -> if (event.type.isBlank()) "رویداد جدید" else "رویداد ${event.type}"
    }
}

fun daysUntilExpiration(value: String?): Int? {
    if (value == null) return null
    val parsed = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSX",
        "yyyy-MM-dd'T'HH:mm:ssX",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
    ).firstNotNullOfOrNull { pattern ->
        runCatching {
            SimpleDateFormat(pattern, Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.parse(value)
        }.getOrNull()
    } ?: return null

    val diff = parsed.time - System.currentTimeMillis()
    return (diff / (1000 * 60 * 60 * 24)).toInt()
}

private fun formatEventTime(value: String?): String? {
    if (value.isNullOrBlank()) return null
    val parsed = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSX",
        "yyyy-MM-dd'T'HH:mm:ssX",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
    ).firstNotNullOfOrNull { pattern ->
        runCatching {
            SimpleDateFormat(pattern, Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.parse(value)
        }.getOrNull()
    } ?: return null

    return SimpleDateFormat("yyyy/MM/dd - HH:mm", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("Asia/Tehran")
    }.format(parsed).toPersianDigits()
}

private fun String.toPersianDigits(): String {
    val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    return buildString(length) {
        this@toPersianDigits.forEach { char ->
            append(if (char in '0'..'9') persianDigits[char - '0'] else char)
        }
    }
}
