package com.example.uzradyab.presentation.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uzradyab.core.utils.JalaliUtils
import com.example.uzradyab.domain.model.Device
import com.example.uzradyab.domain.model.TripReport
import com.example.uzradyab.domain.repository.DeviceRepository
import com.example.uzradyab.domain.repository.MapSettingsRepository
import com.example.uzradyab.domain.repository.ReportRepository
import com.example.uzradyab.domain.repository.GeocoderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import com.example.uzradyab.presentation.components.JalaliDateTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

data class TripReportsUiState(
    val devices: List<Device> = emptyList(),
    val selectedDeviceId: Long? = null,
    val isLoading: Boolean = false,
    val reports: List<TripReport> = emptyList(),
    val error: String? = null,
    val fromDateIso: String = "",
    val toDateIso: String = "",
    val selectedDateFilter: String = "امروز",
    val showCustomDatePicker: Boolean = false,
    val showColumnSelector: Boolean = false,
    val selectedColumns: Set<String> = setOf("startTime", "endTime", "distance", "averageSpeed")
)

val TRIP_REPORT_COLUMNS = listOf(
    ColumnOption(id = "startTime", name = "زمان شروع", isRequired = true),
    ColumnOption(id = "endTime", name = "زمان پایان", isRequired = true),
    ColumnOption(id = "distance", name = "مسافت", isRequired = true),
    ColumnOption(id = "averageSpeed", name = "میانگین سرعت", isRequired = false),
    ColumnOption(id = "maxSpeed", name = "حداکثر سرعت", isRequired = false),
    ColumnOption(id = "duration", name = "مدت زمان", isRequired = false),
    ColumnOption(id = "spentFuel", name = "سوخت مصرفی", isRequired = false),
    ColumnOption(id = "startAddress", name = "آدرس مبدأ", isRequired = false),
    ColumnOption(id = "endAddress", name = "آدرس مقصد", isRequired = false)
)

@HiltViewModel
class TripReportsViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val reportRepository: ReportRepository,
    private val mapSettingsRepository: MapSettingsRepository,
    private val geocoderRepository: GeocoderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TripReportsUiState())
    val uiState: StateFlow<TripReportsUiState> = _uiState.asStateFlow()

    init {
        val (initialFrom, initialTo) = getIsoRangeForFilter("امروز")
        _uiState.update { it.copy(fromDateIso = initialFrom, toDateIso = initialTo) }
        
        viewModelScope.launch {
            val lastId = mapSettingsRepository.observeLastSelectedDeviceId().firstOrNull()
            deviceRepository.observeDevices().collect { devicesList ->
                _uiState.update { current ->
                    val newSelectedId = current.selectedDeviceId ?: lastId ?: devicesList.firstOrNull()?.id
                    current.copy(
                        devices = devicesList,
                        selectedDeviceId = newSelectedId
                    )
                }
                if (_uiState.value.selectedDeviceId != null && _uiState.value.reports.isEmpty()) {
                    fetchReports()
                }
            }
        }
    }

    fun onDeviceSelected(deviceId: Long) {
        _uiState.update { it.copy(selectedDeviceId = deviceId) }
        viewModelScope.launch { mapSettingsRepository.setLastSelectedDeviceId(deviceId) }
        fetchReports()
    }

    fun onDateFilterSelected(filter: String) {
        if (filter == "تاریخ سفارشی") {
            _uiState.update { it.copy(selectedDateFilter = filter, showCustomDatePicker = true) }
            return
        }
        val (from, to) = getIsoRangeForFilter(filter)
        _uiState.update { it.copy(
            selectedDateFilter = filter,
            fromDateIso = from,
            toDateIso = to,
            showCustomDatePicker = false
        ) }
        fetchReports()
    }

    fun applyCustomDateRange(start: JalaliDateTime?, end: JalaliDateTime?) {
        if (start == null || end == null) return
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }
        
        val startG = com.example.uzradyab.core.utils.JalaliUtils.jalaliToGregorian(start.year, start.month, start.day)
        val fromCal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
            set(startG[0], startG[1] - 1, startG[2], start.hour, start.minute, 0)
        }
        
        val endG = com.example.uzradyab.core.utils.JalaliUtils.jalaliToGregorian(end.year, end.month, end.day)
        val toCal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
            set(endG[0], endG[1] - 1, endG[2], end.hour, end.minute, 59)
        }
        
        val fromIso = sdf.format(fromCal.time)
        val toIso = sdf.format(toCal.time)
        
        _uiState.update {
            it.copy(
                fromDateIso = fromIso,
                toDateIso = toIso,
                showCustomDatePicker = false
            )
        }
        fetchReports()
    }

    fun dismissCustomDatePicker() {
        _uiState.update { it.copy(showCustomDatePicker = false) }
    }

    fun openColumnSelector() {
        _uiState.update { it.copy(showColumnSelector = true) }
    }

    fun dismissColumnSelector() {
        _uiState.update { it.copy(showColumnSelector = false) }
    }

    fun toggleColumn(columnId: String) {
        _uiState.update { state ->
            val current = state.selectedColumns
            val newColumns = if (current.contains(columnId)) {
                current - columnId
            } else {
                current + columnId
            }
            state.copy(selectedColumns = newColumns)
        }
    }

    fun fetchReports() {
        val currentState = _uiState.value
        val deviceId = currentState.selectedDeviceId ?: return
        val from = currentState.fromDateIso
        val to = currentState.toDateIso

        if (from.isEmpty() || to.isEmpty()) return

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val result = reportRepository.getTripsReport(deviceId, from, to)
            result.onSuccess { reportsList ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        reports = reportsList
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = err.message ?: "خطا در دریافت گزارش مسافت‌ها"
                    )
                }
            }
        }
    }

    suspend fun resolveAddress(lat: Double, lon: Double): String {
        return try {
            val address = geocoderRepository.getAddress(lat, lon)
            if (address.isNullOrBlank()) "نامشخص" else address
        } catch (e: Exception) {
            "نامشخص"
        }
    }

    private fun getIsoRangeForFilter(filter: String): Pair<String, String> {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tehran"))
        return when (filter) {
            "امروز" -> {
                jalaliToIso(cal, false) to jalaliToIso(cal, true)
            }
            "دیروز" -> {
                cal.add(Calendar.DAY_OF_MONTH, -1)
                jalaliToIso(cal, false) to jalaliToIso(cal, true)
            }
            else -> "" to ""
        }
    }

    private fun jalaliToIso(gCalInfo: Calendar, isEndOfDay: Boolean): String {
        val gCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        gCal.set(
            gCalInfo.get(Calendar.YEAR),
            gCalInfo.get(Calendar.MONTH),
            gCalInfo.get(Calendar.DAY_OF_MONTH)
        )
        if (isEndOfDay) {
            gCal.set(Calendar.HOUR_OF_DAY, 23)
            gCal.set(Calendar.MINUTE, 59)
            gCal.set(Calendar.SECOND, 59)
        } else {
            gCal.set(Calendar.HOUR_OF_DAY, 0)
            gCal.set(Calendar.MINUTE, 0)
            gCal.set(Calendar.SECOND, 0)
        }
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(gCal.time)
    }
}
