package com.example.uzradyab.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uzradyab.domain.model.Device
import com.example.uzradyab.domain.model.Event
import com.example.uzradyab.domain.model.LatestNotificationEvent
import com.example.uzradyab.domain.model.Position
import com.example.uzradyab.domain.model.TrackingConnectionState
import com.example.uzradyab.domain.repository.AuthRepository
import com.example.uzradyab.domain.repository.AppConfigRepository
import com.example.uzradyab.domain.repository.DeviceRepository
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
import android.util.Log
import com.example.uzradyab.core.utils.ImmutableListWrapper
import com.example.uzradyab.core.utils.ImmutableMapWrapper
import com.example.uzradyab.core.utils.emptyImmutableList
import com.example.uzradyab.core.utils.emptyImmutableMap
import com.example.uzradyab.core.utils.toImmutable
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
import kotlin.math.roundToInt

enum class ConnectionErrorType {
    NONE,
    NETWORK_UNREACHABLE,
    SERVER_DOWN
}

data class HomeMapUiState(
    val devices: ImmutableListWrapper<Device> = emptyImmutableList(),
    val latestPositions: ImmutableMapWrapper<Long, Position> = emptyImmutableMap(),
    val selectedDeviceId: Long? = null,
    val devicesOpen: Boolean = false,
    val deviceCardExpanded: Boolean = false,
    val deviceManagementOpen: Boolean = false,
    val connectionState: TrackingConnectionState = TrackingConnectionState.Idle,
    val signedOut: Boolean = false,
    val todayDistanceText: String = "در حال دریافت",
    val latestEvent: MapLatestEventItem? = null,
    val latestEventsMap: ImmutableMapWrapper<Long, MapLatestEventItem> = emptyImmutableMap(),
    val mapSettingsOpen: Boolean = false,
    val mapStyle: String = "osm",
    val isAlternativeMapForced: Boolean = false,
    val isMapLocked: Boolean = false,
    val infoMessage: String? = null,
    val connectionError: ConnectionErrorType = ConnectionErrorType.NONE,
    val isCheckingServer: Boolean = false,
)

data class MapLatestEventItem(
    val text: String,
    val timeText: String?,
)

@HiltViewModel
class MapViewModel @Inject constructor(
    observeHomeSnapshot: ObserveHomeSnapshotUseCase,
    private val authRepository: AuthRepository,
    private val appConfigRepository: AppConfigRepository,
    private val eventRepository: EventRepository,
    private val reportRepository: ReportRepository,
    private val trackingRepository: TrackingRepository,
    private val mapSettingsRepository: MapSettingsRepository,
    private val deviceRepository: DeviceRepository,
) : ViewModel() {
    private val localState = MutableStateFlow(HomeMapUiState())

    // Removed Osmdroid alternative source tracking

    val uiState: StateFlow<HomeMapUiState> = combine(
        observeHomeSnapshot(),
        trackingRepository.connectionState,
        localState,
        eventRepository.observeRecentEvents(limit = 8),
        combine(
            mapSettingsRepository.observeMapStyle(),
            mapSettingsRepository.observeLastSelectedDeviceId(),
            appConfigRepository.currentConfig
        ) { style, lastId, config -> Triple(style, lastId, config) }
    ) { snapshot, connection, local, recentEvents, settings ->
        val mapStyle = settings.first
        val lastDeviceId = settings.second
        val config = settings.third
        
        val selected = local.selectedDeviceId 
            ?: lastDeviceId?.takeIf { id -> snapshot.devices.any { it.id == id } }
            ?: snapshot.devices.firstOrNull()?.id
            
        val isAlternativeForced = config?.alternativeMap == true && !config.alternativeMapUrl.isNullOrEmpty()
        
        Log.d("AlternativeMap", "AppConfig -> alternativeMap: ${config?.alternativeMap}, alternativeMapUrl: ${config?.alternativeMapUrl}, isForced: $isAlternativeForced")

        val finalMapStyle = if (isAlternativeForced) {
            "alternative_forced"
        } else {
            mapStyle
        }

        local.copy(
            devices = snapshot.devices.toImmutable(),
            latestPositions = snapshot.latestPositions.toImmutable(),
            selectedDeviceId = selected,
            connectionState = connection,
            mapStyle = finalMapStyle,
            isAlternativeMapForced = isAlternativeForced,
            latestEvent = selected?.let { id -> local.latestEventsMap[id] ?: latestEventForDevice(recentEvents, id) },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeMapUiState())

    init {
        trackingRepository.start()
        observeSelectedDeviceDistance()
        observeSelectedDeviceLatestEvent()
        startHealthCheckLoop()
        
        viewModelScope.launch {
            deviceRepository.refreshDevices()
        }
        
        viewModelScope.launch {
            uiState.map { it.selectedDeviceId }.distinctUntilChanged().collectLatest { deviceId ->
                if (deviceId != null) {
                    mapSettingsRepository.addTrackedDeviceId(deviceId)
                }
            }
        }

        viewModelScope.launch {
            trackingRepository.connectionState
                .collect { state ->
                    if (state == TrackingConnectionState.Disconnected) {
                        checkServerHealth()
                    }
                }
        }
    }

    fun selectDevice(deviceId: Long) {
        viewModelScope.launch {
            mapSettingsRepository.setLastSelectedDeviceId(deviceId)
        }
        val hasPosition = localState.value.latestPositions[deviceId] != null
        localState.update {
            it.copy(
                selectedDeviceId = deviceId,
                devicesOpen = false,
                deviceManagementOpen = false,
                infoMessage = if (!hasPosition) "هنوز موقعیتی برای این دستگاه ثبت نشده است." else null
            )
        }
    }

    fun clearInfoMessage() {
        localState.update { it.copy(infoMessage = null) }
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
        // MapLibre style updates automatically based on the string.
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

    fun toggleMapLock() {
        localState.update { it.copy(isMapLocked = !it.isMapLocked) }
        Log.d("MapViewModel", "Map lock toggled: ${!localState.value.isMapLocked}")
    }

    fun unlockMap() {
        if (localState.value.isMapLocked) {
            localState.update { it.copy(isMapLocked = false) }
            Log.d("MapViewModel", "Map unlocked by user action")
        }
    }

    fun consumeTileHealthError() {
        // No-op for MapLibre
    }

    private var lastServerDownTime = 0L

    fun dismissServerDown() {
        localState.update { it.copy(connectionError = ConnectionErrorType.NONE) }
        lastServerDownTime = System.currentTimeMillis()
    }

    private fun startHealthCheckLoop() {
        viewModelScope.launch {
            while (true) {
                checkServerHealth()
                delay(30L * 60L * 1000L) // 30 minutes
            }
        }
    }

    private fun checkServerHealth() {
        viewModelScope.launch {
            localState.update { it.copy(isCheckingServer = true, connectionError = ConnectionErrorType.NONE) }
            val result = authRepository.checkServerHealth()
            
            result.onSuccess { statusCode ->
                val currentTime = System.currentTimeMillis()
                val oneHourMs = 60L * 60L * 1000L
                val shouldShowError = (lastServerDownTime == 0L || currentTime - lastServerDownTime > oneHourMs)

                if (statusCode == 401 || statusCode == 404) {
                    logout()
                } else if (statusCode in 200..299) {
                    localState.update { it.copy(isCheckingServer = false, connectionError = ConnectionErrorType.NONE) }
                } else if (statusCode in 400..499) {
                    if (shouldShowError) {
                        localState.update { it.copy(isCheckingServer = false, connectionError = ConnectionErrorType.NETWORK_UNREACHABLE) }
                    } else {
                        localState.update { it.copy(isCheckingServer = false) }
                    }
                } else if (statusCode >= 500) {
                    if (shouldShowError) {
                        localState.update { it.copy(isCheckingServer = false, connectionError = ConnectionErrorType.SERVER_DOWN) }
                    } else {
                        localState.update { it.copy(isCheckingServer = false) }
                    }
                } else {
                    localState.update { it.copy(isCheckingServer = false) }
                }
            }.onFailure { exception ->
                if (exception is retrofit2.HttpException && (exception.code() == 401 || exception.code() == 404)) {
                    logout()
                } else {
                    val currentTime = System.currentTimeMillis()
                    val oneHourMs = 60L * 60L * 1000L
                    if (lastServerDownTime == 0L || currentTime - lastServerDownTime > oneHourMs) {
                        val errorType = if (exception is java.net.UnknownHostException || exception is java.net.ConnectException || exception is java.net.SocketException || exception is java.net.SocketTimeoutException) {
                            ConnectionErrorType.NETWORK_UNREACHABLE
                        } else {
                            ConnectionErrorType.SERVER_DOWN
                        }
                        localState.update { it.copy(isCheckingServer = false, connectionError = errorType) }
                    } else {
                        localState.update { it.copy(isCheckingServer = false) }
                    }
                }
            }
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
                        return@collectLatest
                    }
                    while (true) {
                        eventRepository.fetchLatestDeviceEvents(deviceId)
                            .onSuccess { events ->
                                val item = events.firstOrNull()?.toTickerItem()
                                if (item != null) {
                                    localState.update { state ->
                                        val newMap = state.latestEventsMap.toMutableMap()
                                        newMap[deviceId] = item
                                        state.copy(latestEventsMap = newMap.toImmutable())
                                    }
                                }
                            }
                        delay(120_000L)
                    }
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
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
    if (deviceId == null) return null
    val event = events.firstOrNull { it.deviceId == deviceId }
    return event?.let {
        val formattedTime = formatEventTime(it.eventTime)
        val text = formatEventText(it)
        MapLatestEventItem(
            text = if (formattedTime != null) "$text - $formattedTime" else text,
            timeText = null,
        )
    }
}

private fun com.example.uzradyab.domain.model.LatestNotificationEvent.toTickerItem(): MapLatestEventItem {
    val formattedTime = formatEventTime(time)
    return MapLatestEventItem(
        text = if (formattedTime != null) "$text - $formattedTime" else text,
        timeText = null,
    )
}

private fun formatEventText(event: Event): String {
    return when (event.type) {
        "all" -> "همه رویدادها"
        "deviceOnline" -> "وضعیت آنلاین"
        "deviceUnknown" -> "وضعیت نامعلوم"
        "deviceOffline" -> "وضعیت آفلاین"
        "deviceInactive" -> "دستگاه غیرفعال"
        "queuedCommandSent" -> "Queued command sent"
        "deviceMoving" -> "حرکت دستگاه"
        "deviceStopped" -> "دستگاه متوقف شد"
        "deviceOverspeed" -> "سرعت از حد مجاز فراتر رفت"
        "deviceFuelDrop" -> "افت سوخت"
        "deviceFuelIncrease" -> "افزایش سوخت"
        "commandResult" -> "نتیجه ارسال دستور"
        "geofenceEnter" -> "ورود محدوده جغرافیایی"
        "geofenceExit" -> "خروج محدوده جغرافیایی"
        "alarm" -> "هشدار"
        "ignitionOn" -> "سویچ روشن"
        "ignitionOff" -> "سوئیچ خاموش"
        "maintenance" -> "نیاز به تعمیر"
        "textMessage" -> "پیامک دریافت شد"
        "driverChanged" -> "تعویض راننده"
        "media" -> "مدیا"
        else -> if (event.type.isBlank()) "رویداد جدید" else event.type
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

    val cal = java.util.Calendar.getInstance(TimeZone.getTimeZone("Asia/Tehran")).apply { time = parsed }
    val gY = cal.get(java.util.Calendar.YEAR)
    val gM = cal.get(java.util.Calendar.MONTH) + 1
    val gD = cal.get(java.util.Calendar.DAY_OF_MONTH)

    val jDate = com.example.uzradyab.core.utils.JalaliUtils.gregorianToJalali(gY, gM, gD)
    val timeStr = SimpleDateFormat("HH:mm", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("Asia/Tehran")
    }.format(parsed)

    return "${jDate[0]}/${String.format(Locale.US, "%02d", jDate[1])}/${String.format(Locale.US, "%02d", jDate[2])} - $timeStr".toPersianDigits()
}

private fun String.toPersianDigits(): String {
    val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    return buildString(length) {
        this@toPersianDigits.forEach { char ->
            append(if (char in '0'..'9') persianDigits[char - '0'] else char)
        }
    }
}
