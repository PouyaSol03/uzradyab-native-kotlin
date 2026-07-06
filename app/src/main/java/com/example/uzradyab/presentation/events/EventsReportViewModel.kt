package com.example.uzradyab.presentation.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uzradyab.domain.model.Device
import com.example.uzradyab.domain.model.Event
import com.example.uzradyab.domain.repository.DeviceRepository
import com.example.uzradyab.domain.repository.EventRepository
import com.example.uzradyab.domain.repository.MapSettingsRepository
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import com.example.uzradyab.core.utils.FormatUtils

enum class EventDateFilter {
    Today, Yesterday, CurrentWeek, CurrentMonth, Custom
}

data class EventsReportUiState(
    val isLoading: Boolean = false,
    val devices: List<Device> = emptyList(),
    val selectedDeviceId: Long? = null,
    val events: List<EventUiModel> = emptyList(),
    val hasMore: Boolean = false,
    val dateFilter: EventDateFilter = EventDateFilter.Today,
    val customFromDate: Long? = null,
    val customToDate: Long? = null,
    val filterText: String = "",
    val error: String? = null
)

@HiltViewModel
class EventsReportViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val eventRepository: EventRepository,
    private val mapSettingsRepository: MapSettingsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventsReportUiState())
    val uiState: StateFlow<EventsReportUiState> = _uiState.asStateFlow()

    private var fullEvents: List<EventUiModel> = emptyList()
    private val pageSize = 50

    private val initialDeviceId = savedStateHandle.get<String>("deviceId")?.toLongOrNull()

    init {
        observeDevices()
    }

    private fun observeDevices() {
        viewModelScope.launch {
            kotlinx.coroutines.flow.combine(
                deviceRepository.observeDevices(),
                mapSettingsRepository.observeLastSelectedDeviceId()
            ) { list, lastSelectedId ->
                list to lastSelectedId
            }.collectLatest { (list, lastSelectedId) ->
                _uiState.update { state ->
                    val selectedId = state.selectedDeviceId ?: lastSelectedId ?: initialDeviceId ?: list.firstOrNull()?.id
                    if (state.selectedDeviceId == null && selectedId != null) {
                        fetchEvents(selectedId, state.dateFilter)
                    }
                    state.copy(
                        devices = list.sortedBy { it.name },
                        selectedDeviceId = selectedId
                    )
                }
            }
        }
    }

    fun selectDevice(deviceId: Long) {
        if (_uiState.value.selectedDeviceId == deviceId) return
        _uiState.update { it.copy(selectedDeviceId = deviceId) }
        viewModelScope.launch {
            mapSettingsRepository.setLastSelectedDeviceId(deviceId)
        }
        fetchEvents(deviceId, _uiState.value.dateFilter)
    }

    fun setDateFilter(filter: EventDateFilter) {
        _uiState.update { it.copy(dateFilter = filter) }
        val deviceId = _uiState.value.selectedDeviceId ?: return
        fetchEvents(deviceId, filter)
    }

    fun setCustomDateFilter(from: Long, to: Long) {
        _uiState.update { it.copy(dateFilter = EventDateFilter.Custom, customFromDate = from, customToDate = to) }
        val deviceId = _uiState.value.selectedDeviceId ?: return
        fetchEvents(deviceId, EventDateFilter.Custom)
    }

    private fun fetchEvents(deviceId: Long, filter: EventDateFilter) {
        val (from, to) = getRange(filter)
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, filterText = formatFilterText(filter, from)) }
            val result = eventRepository.getEventsReport(deviceId, from, to)
            result.onSuccess { events ->
                val uiModels = withContext(Dispatchers.Default) {
                    events.sortedByDescending { it.eventTime }.map { eventToItem(it) }
                }
                fullEvents = uiModels
                val initialPage = uiModels.take(pageSize)
                
                _uiState.update { it.copy(
                    isLoading = false, 
                    events = initialPage,
                    hasMore = uiModels.size > pageSize,
                    error = if (uiModels.isEmpty()) "هیچ رویدادی در این بازه زمانی یافت نشد." else null
                ) }
            }.onFailure { err ->
                _uiState.update { it.copy(isLoading = false, events = emptyList(), hasMore = false, error = err.message ?: "خطا در دریافت رویدادها") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun loadNextPage() {
        val currentSize = _uiState.value.events.size
        if (currentSize >= fullEvents.size) return
        
        val nextSize = (currentSize + pageSize).coerceAtMost(fullEvents.size)
        val nextChunk = fullEvents.take(nextSize)
        
        _uiState.update { it.copy(
            events = nextChunk,
            hasMore = nextSize < fullEvents.size
        ) }
    }

    private fun eventToItem(event: Event): EventUiModel {
        val title = eventTitle(event.type)
        return EventUiModel(
            id = event.id,
            title = title,
            description = eventTitle(event.type),
            time = FormatUtils.formatEventTimeJalali(event.eventTime),
        )
    }

    private fun eventTitle(type: String): String {
        return when (type) {
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
            else -> if (type.isBlank()) "رویداد جدید" else type
        }
    }

    private fun getRange(filter: EventDateFilter): Pair<String, String> {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tehran"))
        val to = sdf.format(cal.time)

        when (filter) {
            EventDateFilter.Today -> {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
            }
            EventDateFilter.Yesterday -> {
                cal.add(Calendar.DAY_OF_YEAR, -1)
                val from = cal.clone() as Calendar
                from.set(Calendar.HOUR_OF_DAY, 0)
                from.set(Calendar.MINUTE, 0)
                from.set(Calendar.SECOND, 0)
                
                val toCal = cal.clone() as Calendar
                toCal.set(Calendar.HOUR_OF_DAY, 23)
                toCal.set(Calendar.MINUTE, 59)
                toCal.set(Calendar.SECOND, 59)
                
                return Pair(sdf.format(from.time), sdf.format(toCal.time))
            }
            EventDateFilter.CurrentWeek -> {
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.set(Calendar.HOUR_OF_DAY, 0)
            }
            EventDateFilter.CurrentMonth -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
            }
            EventDateFilter.Custom -> {
                val state = _uiState.value
                if (state.customFromDate != null && state.customToDate != null) {
                    return Pair(sdf.format(Date(state.customFromDate)), sdf.format(Date(state.customToDate)))
                }
            }
        }
        return Pair(sdf.format(cal.time), to)
    }

    private fun formatFilterText(filter: EventDateFilter, fromIso: String): String {
        val label = when(filter) {
            EventDateFilter.Today -> "امروز"
            EventDateFilter.Yesterday -> "دیروز"
            EventDateFilter.CurrentWeek -> "هفته جاری"
            EventDateFilter.CurrentMonth -> "ماه جاری"
            EventDateFilter.Custom -> "بازه انتخابی"
        }
        if (filter == EventDateFilter.Custom) {
            val state = _uiState.value
            if (state.customFromDate != null && state.customToDate != null) {
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }
                return "$label | از ${FormatUtils.formatEventTimeJalali(sdf.format(Date(state.customFromDate)))} تا ${FormatUtils.formatEventTimeJalali(sdf.format(Date(state.customToDate)))}"
            }
        }
        val jDateStr = com.example.uzradyab.core.utils.JalaliUtils.getTodayJalaliString().substringAfter("| ").trim()
        return "$label | $jDateStr"
    }
}
