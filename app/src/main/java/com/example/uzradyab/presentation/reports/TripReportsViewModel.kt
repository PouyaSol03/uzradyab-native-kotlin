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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.uzradyab.core.utils.FormatUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import com.example.uzradyab.core.utils.ImmutableListWrapper
import com.example.uzradyab.core.utils.ImmutableSetWrapper
import com.example.uzradyab.core.utils.emptyImmutableList
import com.example.uzradyab.core.utils.toImmutable

data class TripReportsUiState(
    val devices: ImmutableListWrapper<Device> = emptyImmutableList(),
    val selectedDeviceId: Long? = null,
    val isLoading: Boolean = false,
    val reports: ImmutableListWrapper<TripReportUiModel> = emptyImmutableList(),
    val error: String? = null,
    val fromDateIso: String = "",
    val toDateIso: String = "",
    val selectedDateFilter: String = "امروز",
    val showCustomDatePicker: Boolean = false,
    val showColumnSelector: Boolean = false,
    val selectedColumns: ImmutableSetWrapper<String> = setOf("startTime", "endTime", "distance", "averageSpeed").toImmutable()
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
                val previousSelectedId = _uiState.value.selectedDeviceId
                _uiState.update { current ->
                    val newSelectedId = current.selectedDeviceId ?: lastId ?: devicesList.firstOrNull()?.id
                    current.copy(
                        devices = devicesList.toImmutable(),
                        selectedDeviceId = newSelectedId
                    )
                }
                val newSelectedId = _uiState.value.selectedDeviceId
                if (newSelectedId != null && previousSelectedId != newSelectedId) {
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
            state.copy(selectedColumns = newColumns.toImmutable())
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
                val uiModels = withContext(Dispatchers.Default) {
                    reportsList.map { report ->
                        TripReportUiModel(
                            startPositionId = report.startPositionId,
                            startTime = formatIsoTimeWithFormatUtils(report.startTime),
                            endTime = formatIsoTimeWithFormatUtils(report.endTime),
                            distance = FormatUtils.formatDoublePersian(report.distance / 1000.0, 2),
                            averageSpeed = FormatUtils.formatDoublePersian(report.averageSpeed * 1.852, 0),
                            maxSpeed = FormatUtils.formatDoublePersian(report.maxSpeed * 1.852, 0),
                            duration = formatDuration(report.duration),
                            spentFuel = FormatUtils.formatDoublePersian(report.spentFuel, 1),
                            startAddress = report.startAddress,
                            endAddress = report.endAddress,
                            startLat = report.startLat,
                            startLon = report.startLon,
                            endLat = report.endLat,
                            endLon = report.endLon
                        )
                    }
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        reports = uiModels.toImmutable(),
                        error = if (uiModels.isEmpty()) "هیچ گزارشی برای این بازه زمانی یافت نشد." else null
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

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    suspend fun resolveAddress(lat: Double, lon: Double): String {
        return try {
            val address = geocoderRepository.getAddress(lat, lon)
            if (address.isNullOrBlank()) "نامشخص" else address
        } catch (e: Exception) {
            "نامشخص"
        }
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
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tehran")).apply { time = date }
        val h = cal.get(Calendar.HOUR_OF_DAY).toString().padStart(2, '0')
        val min = cal.get(Calendar.MINUTE).toString().padStart(2, '0')
        val gY = cal.get(Calendar.YEAR)
        val gM = cal.get(Calendar.MONTH) + 1
        val gD = cal.get(Calendar.DAY_OF_MONTH)
        val jDate = JalaliUtils.gregorianToJalali(gY, gM, gD)
        val y = jDate[0]
        val m = jDate[1].toString().padStart(2, '0')
        val d = jDate[2].toString().padStart(2, '0')
        
        return "\u200E$y/$m/$d - $h:$min\u200E".map { char ->
            if (char in '0'..'9') {
                charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')[char - '0']
            } else char
        }.joinToString("")
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
