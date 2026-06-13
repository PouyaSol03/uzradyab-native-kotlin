package com.example.uzradyab.presentation.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uzradyab.domain.model.Device
import com.example.uzradyab.domain.model.Event
import com.example.uzradyab.domain.repository.DeviceRepository
import com.example.uzradyab.domain.repository.EventRepository
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

enum class EventDateFilter {
    Today, Yesterday, CurrentWeek, CurrentMonth, Custom
}

data class EventsReportUiState(
    val isLoading: Boolean = false,
    val devices: List<Device> = emptyList(),
    val selectedDeviceId: Long? = null,
    val events: List<Event> = emptyList(),
    val dateFilter: EventDateFilter = EventDateFilter.Today,
    val customFromDate: Long? = null,
    val customToDate: Long? = null,
    val filterText: String = ""
)

@HiltViewModel
class EventsReportViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val eventRepository: EventRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventsReportUiState())
    val uiState: StateFlow<EventsReportUiState> = _uiState.asStateFlow()

    private val initialDeviceId = savedStateHandle.get<String>("deviceId")?.toLongOrNull()

    init {
        observeDevices()
    }

    private fun observeDevices() {
        viewModelScope.launch {
            deviceRepository.observeDevices().collectLatest { list ->
                _uiState.update { state ->
                    val selectedId = state.selectedDeviceId ?: initialDeviceId ?: list.firstOrNull()?.id
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
        fetchEvents(deviceId, _uiState.value.dateFilter)
    }

    fun setDateFilter(filter: EventDateFilter) {
        _uiState.update { it.copy(dateFilter = filter) }
        val deviceId = _uiState.value.selectedDeviceId ?: return
        fetchEvents(deviceId, filter)
    }

    private fun fetchEvents(deviceId: Long, filter: EventDateFilter) {
        val (from, to) = getRange(filter)
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, filterText = formatFilterText(filter, from)) }
            val result = eventRepository.getEventsReport(deviceId, from, to)
            result.onSuccess { events ->
                _uiState.update { it.copy(isLoading = false, events = events.sortedByDescending { it.eventTime }) }
            }.onFailure {
                _uiState.update { it.copy(isLoading = false, events = emptyList()) }
            }
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
                // Implement custom logic if needed
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
        val jDateStr = com.example.uzradyab.core.utils.JalaliUtils.getTodayJalaliString().substringAfter("| ").trim()
        return "$label | $jDateStr"
    }
}
