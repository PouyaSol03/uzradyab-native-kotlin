package com.example.uzradyab.presentation.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uzradyab.core.debug.AppLogger
import com.example.uzradyab.core.debug.LogLevel
import com.example.uzradyab.domain.model.Device
import com.example.uzradyab.domain.model.Position
import com.example.uzradyab.domain.repository.DeviceRepository
import com.example.uzradyab.domain.repository.GeocoderRepository
import com.example.uzradyab.domain.repository.PositionRepository
import com.example.uzradyab.domain.repository.ReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

data class ReportsUiState(
    val isLoading: Boolean = false,
    val devices: List<Device> = emptyList(),
    val selectedDeviceId: Long? = null,
    val deviceStatusText: String = "نامشخص",
    val currentAddress: String = "در حال دریافت...",
    val distanceKm: String = "۰",
    val fuelLiters: String = "۰",
    val averageSpeed: String = "۰"
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val reportRepository: ReportRepository,
    private val geocoderRepository: GeocoderRepository,
    private val positionRepository: PositionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    init {
        observeDevices()
    }

    private fun observeDevices() {
        viewModelScope.launch {
            deviceRepository.observeDevices().collectLatest { list ->
                val sortedList = list.sortedBy { it.name }
                _uiState.update { state ->
                    val selectedId = state.selectedDeviceId ?: sortedList.firstOrNull()?.id

                    if (state.selectedDeviceId == null && selectedId != null) {
                        fetchDeviceData(selectedId)
                    }

                    state.copy(
                        devices = sortedList,
                        selectedDeviceId = selectedId
                    )
                }
            }
        }
    }

    fun selectDevice(deviceId: Long) {
        if (_uiState.value.selectedDeviceId == deviceId) return

        _uiState.update { it.copy(selectedDeviceId = deviceId) }
        fetchDeviceData(deviceId)
    }

    private fun fetchDeviceData(deviceId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, currentAddress = "در حال دریافت آدرس...") }

            // ۱. دریافت موقعیت جاری دستگاه
            val selectedPosition: Position? = positionRepository.getLatestPosition(deviceId)

            // ۲. وضعیت احتراق
            val statusText = calculateDeviceStatus(selectedPosition)

            // ۳. دریافت آدرس واقعی از Map.ir
            val addressText = if (selectedPosition != null) {
                AppLogger.log(LogLevel.REQUEST, "Geocoder", "Fetching address for ${selectedPosition.latitude}, ${selectedPosition.longitude}")
                val address = geocoderRepository.getAddress(selectedPosition.latitude, selectedPosition.longitude)
                AppLogger.log(LogLevel.RESPONSE, "Geocoder", "Address received: $address")
                address
            } else {
                "موقعیت نامشخص"
            }

            // ۴. دریافت خلاصه گزارش
            val (fromIso, toIso) = getTodayIsoRange()
            AppLogger.log(LogLevel.REQUEST, "Report", "Fetching summary for device $deviceId from $fromIso to $toIso")
            val summaryResult = reportRepository.getSummaryReport(
                deviceId = deviceId,
                from = fromIso,
                to = toIso
            )

            // ۵. آپدیت UI
            summaryResult.onSuccess { summaryList ->
                val summary = summaryList.firstOrNull()
                AppLogger.log(LogLevel.RESPONSE, "Report", "Summary received successfully")

                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        deviceStatusText = statusText,
                        currentAddress = addressText,
                        distanceKm = formatMetric(summary?.distance, divider = 1000.0),
                        fuelLiters = formatMetric(summary?.spentFuel),
                        averageSpeed = formatMetric(summary?.averageSpeed, multiplier = 1.852)
                    )
                }
            }.onFailure { e ->
                AppLogger.log(LogLevel.ERROR, "Report", "Failed to fetch summary: ${e.message}")
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        deviceStatusText = statusText,
                        currentAddress = addressText,
                        distanceKm = "۰",
                        fuelLiters = "۰",
                        averageSpeed = "۰"
                    )
                }
            }
        }
    }

    private fun calculateDeviceStatus(position: Position?): String {
        if (position == null) return "نامشخص"
        return try {
            val json = JSONObject(position.attributesJson)
            val ignition = json.optBoolean("ignition", false)
            val motion = json.optBoolean("motion", false)
            when {
                !ignition -> "خاموش"
                ignition && motion -> "روشن | در حال حرکت"
                ignition && !motion -> "روشن | متوقف"
                else -> "نامشخص"
            }
        } catch (e: Exception) {
            "نامشخص"
        }
    }

    private fun getTodayIsoRange(): Pair<String, String> {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val todayStart = sdf.format(cal.time)

        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val todayEnd = sdf.format(cal.time)

        return Pair(todayStart, todayEnd)
    }

    private fun formatMetric(value: Double?, multiplier: Double = 1.0, divider: Double = 1.0): String {
        if (value == null) return "۰"
        val finalValue = (value * multiplier) / divider
        return String.format(Locale.US, "%.1f", finalValue).toPersianDigits()
    }

    private fun String.toPersianDigits(): String {
        val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        return buildString(length) {
            this@toPersianDigits.forEach { char ->
                append(if (char in '0'..'9') persianDigits[char - '0'] else char)
            }
        }
    }
}
