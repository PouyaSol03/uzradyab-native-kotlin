package com.example.uzradyab.presentation.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uzradyab.domain.model.Device
import com.example.uzradyab.domain.model.SummaryReport
import com.example.uzradyab.domain.repository.DeviceRepository
import com.example.uzradyab.domain.repository.ReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.firstOrNull
import com.example.uzradyab.domain.repository.MapSettingsRepository
import com.example.uzradyab.domain.repository.GeocoderRepository
import com.example.uzradyab.presentation.components.JalaliDateTime
import javax.inject.Inject
import com.example.uzradyab.core.utils.FormatUtils

data class DailyReportUiState(
    val devices: List<Device> = emptyList(),
    val selectedDeviceId: Long? = null,
    val isLoading: Boolean = false,
    val summaryReport: SummaryReport? = null,
    val error: String? = null,
    val fromDateIso: String = "",
    val toDateIso: String = "",
    val selectedDateFilter: String = "امروز",
    val showCustomDatePicker: Boolean = false,
    val startAddressResolved: String = "—",
    val endAddressResolved: String = "—",
    val ignitionDuration: String = "۰.۰ ساعت",
    val jalaliStartTime: String = "- : -",
    val averageSpeed: String = "۰.۰",
    val maxSpeed: String = "۰.۰",
    val distance: String = "۰.۰",
    val spentFuel: String = "۰.۰",
    val startOdometer: String = "۰.۰",
    val endOdometer: String = "۰.۰"
)

@HiltViewModel
class DailyReportViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val reportRepository: ReportRepository,
    private val mapSettingsRepository: MapSettingsRepository,
    private val geocoderRepository: GeocoderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DailyReportUiState())
    val uiState: StateFlow<DailyReportUiState> = _uiState.asStateFlow()

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
                if (_uiState.value.selectedDeviceId != null && _uiState.value.summaryReport == null) {
                    fetchReports()
                }
            }
        }
    }

    fun onDeviceSelected(deviceId: Long) {
        viewModelScope.launch {
            mapSettingsRepository.setLastSelectedDeviceId(deviceId)
        }
        _uiState.update { it.copy(selectedDeviceId = deviceId) }
        fetchReports()
    }

    fun onDateFilterSelected(filter: String) {
        val (from, to) = getIsoRangeForFilter(filter)
        
        _uiState.update { 
            it.copy(
                selectedDateFilter = filter,
                fromDateIso = from,
                toDateIso = to,
                showCustomDatePicker = filter == "تاریخ سفارشی"
            ) 
        }

        if (filter != "تاریخ سفارشی") {
            fetchReports()
        }
    }

    fun onCustomDateSelected(fromIso: String, toIso: String) {
        _uiState.update {
            it.copy(
                fromDateIso = fromIso,
                toDateIso = toIso,
                showCustomDatePicker = false
            )
        }
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
        
        onCustomDateSelected(fromIso, toIso)
    }

    fun dismissCustomDatePicker() {
        _uiState.update { it.copy(showCustomDatePicker = false) }
    }

    fun fetchReports() {
        val currentState = _uiState.value
        val deviceId = currentState.selectedDeviceId ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, startAddressResolved = "—", endAddressResolved = "—", summaryReport = null) }

            reportRepository.getSummaryReport(
                deviceId = deviceId,
                from = currentState.fromDateIso,
                to = currentState.toDateIso
            ).onSuccess { data ->
                val summary = data.firstOrNull()
                val startAddr = summary?.startAddress ?: "—"
                val endAddr = summary?.endAddress ?: "—"
                
                val engineHoursMs = summary?.engineHours ?: 0L
                val ignitionDur = FormatUtils.formatDoublePersian(engineHoursMs / 3600000.0) + " ساعت"
                val jStart = summary?.startTime?.let { FormatUtils.formatEventTimeJalali(it) } ?: "- : -"
                
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        summaryReport = summary, 
                        startAddressResolved = startAddr, 
                        endAddressResolved = endAddr,
                        ignitionDuration = ignitionDur,
                        jalaliStartTime = jStart,
                        averageSpeed = FormatUtils.formatDoublePersian((summary?.averageSpeed ?: 0.0) * 1.852),
                        maxSpeed = FormatUtils.formatDoublePersian((summary?.maxSpeed ?: 0.0) * 1.852),
                        distance = FormatUtils.formatDoublePersian((summary?.distance ?: 0.0) / 1000.0),
                        spentFuel = FormatUtils.formatDoublePersian(summary?.spentFuel ?: 0.0),
                        startOdometer = FormatUtils.formatDoublePersian((summary?.startOdometer ?: 0.0) / 1000.0),
                        endOdometer = FormatUtils.formatDoublePersian((summary?.endOdometer ?: 0.0) / 1000.0),
                        error = if (data.isEmpty()) "هیچ گزارشی برای این بازه زمانی یافت نشد." else null
                    ) 
                }
            }.onFailure { err ->
                _uiState.update { it.copy(isLoading = false, error = err.message ?: "خطا در دریافت گزارش") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun getIsoRangeForFilter(filter: String): Pair<String, String> {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
        }
        val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Tehran"))
        
        when (filter) {
            "دیروز" -> cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
            "هفته گذشته" -> cal.add(java.util.Calendar.DAY_OF_YEAR, -7)
            "امروز" -> {}
            else -> return Pair(_uiState.value.fromDateIso, _uiState.value.toDateIso)
        }

        if (filter == "هفته گذشته") {
            cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
            cal.set(java.util.Calendar.MINUTE, 0)
            cal.set(java.util.Calendar.SECOND, 0)
            cal.set(java.util.Calendar.MILLISECOND, 0)
            val fromTime = sdf.format(cal.time)
            
            val calToday = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Tehran"))
            calToday.set(java.util.Calendar.HOUR_OF_DAY, 23)
            calToday.set(java.util.Calendar.MINUTE, 59)
            calToday.set(java.util.Calendar.SECOND, 59)
            calToday.set(java.util.Calendar.MILLISECOND, 999)
            val toTime = sdf.format(calToday.time)
            
            return Pair(fromTime, toTime)
        }

        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        val fromTime = sdf.format(cal.time)

        cal.set(java.util.Calendar.HOUR_OF_DAY, 23)
        cal.set(java.util.Calendar.MINUTE, 59)
        cal.set(java.util.Calendar.SECOND, 59)
        cal.set(java.util.Calendar.MILLISECOND, 999)
        val toTime = sdf.format(cal.time)

        return Pair(fromTime, toTime)
    }
}
