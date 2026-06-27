package com.example.uzradyab.presentation.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uzradyab.core.utils.JalaliUtils
import com.example.uzradyab.domain.model.Device
import com.example.uzradyab.domain.model.StopReport
import com.example.uzradyab.domain.repository.DeviceRepository
import com.example.uzradyab.domain.repository.ReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.uzradyab.core.utils.FormatUtils
import com.example.uzradyab.domain.repository.MapSettingsRepository
import com.example.uzradyab.domain.repository.GeocoderRepository
import com.example.uzradyab.presentation.components.JalaliDateTime
import javax.inject.Inject

data class StopReportsUiState(
    val devices: List<Device> = emptyList(),
    val selectedDeviceId: Long? = null,
    val isLoading: Boolean = false,
    val reports: List<StopReportUiModel> = emptyList(),
    val error: String? = null,
    val fromDateIso: String = "",
    val toDateIso: String = "",
    val selectedDateFilter: String = "امروز",
    val showCustomDatePicker: Boolean = false,
    val showColumnSelector: Boolean = false,
    val selectedColumns: Set<String> = setOf("startTime", "endTime", "address") // Default columns based on React
)

val STOP_REPORT_COLUMNS = listOf(
    ColumnOption(id = "startTime", name = "زمان شروع", isRequired = true),
    ColumnOption(id = "endTime", name = "زمان پایان", isRequired = true),
    ColumnOption(id = "address", name = "آدرس", isRequired = true),
    ColumnOption(id = "duration", name = "مدت زمان", isRequired = false),
    ColumnOption(id = "engineHours", name = "ساعات کارکرد موتور", isRequired = false),
    ColumnOption(id = "spentFuel", name = "سوخت مصرفی", isRequired = false)
)

@HiltViewModel
class StopReportsViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val reportRepository: ReportRepository,
    private val mapSettingsRepository: MapSettingsRepository,
    private val geocoderRepository: GeocoderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StopReportsUiState())
    val uiState: StateFlow<StopReportsUiState> = _uiState.asStateFlow()

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
        viewModelScope.launch {
            mapSettingsRepository.setLastSelectedDeviceId(deviceId)
        }
        _uiState.update { it.copy(selectedDeviceId = deviceId) }
        fetchReports()
    }

    suspend fun resolveAddress(lat: Double, lon: Double): String {
        return geocoderRepository.getAddress(lat, lon)
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

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            reportRepository.getStopsReport(
                deviceId = deviceId,
                from = currentState.fromDateIso,
                to = currentState.toDateIso
            ).onSuccess { data ->
                val uiModels = withContext(Dispatchers.Default) {
                    data.map { report ->
                        StopReportUiModel(
                            deviceId = report.deviceId,
                            positionId = report.positionId,
                            latitude = report.latitude,
                            longitude = report.longitude,
                            startTime = formatIsoTimeWithFormatUtils(report.startTime),
                            endTime = formatIsoTimeWithFormatUtils(report.endTime),
                            address = report.address,
                            duration = formatDuration(report.duration),
                            engineHours = formatDuration(report.engineHours),
                            spentFuel = FormatUtils.formatDoublePersian(report.spentFuel, 1)
                        )
                    }
                }
                _uiState.update { it.copy(isLoading = false, reports = uiModels, error = if (uiModels.isEmpty()) "هیچ گزارشی برای این بازه زمانی یافت نشد." else null) }
            }.onFailure { err ->
                _uiState.update { it.copy(isLoading = false, error = err.message ?: "خطا در دریافت گزارش") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun formatDuration(durationMs: Long): String {
        val totalMinutes = durationMs / 60000
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        
        val hStr = if (hours > 0) "$hours ساعت " else ""
        val mStr = if (minutes > 0 || hours == 0L) "$minutes دقیقه" else ""
        val andStr = if (hours > 0 && minutes > 0) "و " else ""
        
        return ("$hStr$andStr$mStr").let { FormatUtils.formatDoublePersian(0.0).replace("۰.۰", "").let { _ -> it } }
            .map { char ->
                if (char in '0'..'9') {
                    charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')[char - '0']
                } else char
            }.joinToString("")
    }

    private fun formatIsoTimeWithFormatUtils(isoString: String?): String {
        if (isoString.isNullOrBlank()) return "نامشخص"
        val date = FormatUtils.parseIsoDate(isoString) ?: return isoString
        val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Tehran")).apply { time = date }
        val h = cal.get(java.util.Calendar.HOUR_OF_DAY).toString().padStart(2, '0')
        val min = cal.get(java.util.Calendar.MINUTE).toString().padStart(2, '0')
        val gY = cal.get(java.util.Calendar.YEAR)
        val gM = cal.get(java.util.Calendar.MONTH) + 1
        val gD = cal.get(java.util.Calendar.DAY_OF_MONTH)
        val jDate = JalaliUtils.gregorianToJalali(gY, gM, gD)
        val y = jDate[0]
        val m = jDate[1].toString().padStart(2, '0')
        val d = jDate[2].toString().padStart(2, '0')
        
        return "$h:$min | $y/$m/$d".map { char ->
            if (char in '0'..'9') {
                charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')[char - '0']
            } else char
        }.joinToString("")
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
