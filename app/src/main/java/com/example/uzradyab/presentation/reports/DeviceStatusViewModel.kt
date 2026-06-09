package com.example.uzradyab.presentation.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uzradyab.domain.model.Device
import com.example.uzradyab.domain.model.Position
import com.example.uzradyab.domain.repository.DeviceRepository
import com.example.uzradyab.domain.repository.GeocoderRepository
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

data class DeviceStatusUiState(
    val isLoading: Boolean = false,
    val devices: List<Device> = emptyList(),
    val selectedDeviceId: Long? = null,
    val deviceStatusText: String = "نامشخص",
    val startAddress: String = "در حال دریافت...",
    val currentAddress: String = "در حال دریافت...",
    val firstIgnitionTime: String = "- : -",
    val ignitionDuration: String = "- ساعت و - دقیقه",
    val averageSpeed: String = "۰",
    val spentFuel: String = "۰",
    val startOdometer: String = "۰",
    val endOdometer: String = "۰"
)

@HiltViewModel
class DeviceStatusViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val reportRepository: ReportRepository,
    private val geocoderRepository: GeocoderRepository // تزریق سیستم Geocoder (تبدیل مختصات به آدرس)
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeviceStatusUiState())
    val uiState: StateFlow<DeviceStatusUiState> = _uiState.asStateFlow()

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
                        fetchDeviceStatusData(selectedId)
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
        fetchDeviceStatusData(deviceId)
    }

    private fun fetchDeviceStatusData(deviceId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // ۱. دریافت موقعیت جاری دستگاه
            val currentPosition: Position? = getLatestPositionForDevice(deviceId)

            // TODO: برای موقعیت مبدأ باید اولین پوزیشن امروز یا پوزیشن شروع حرکت را دریافت کنید
            val startPosition: Position? = currentPosition

            // ۲. وضعیت فعلی دستگاه (روشن/خاموش)
            val statusText = calculateDeviceStatus(currentPosition)

            // ۳. دریافت آدرس‌ها (استفاده از کش و API نقشه)
            val currentAddressText = currentPosition?.let { getAddressFromCoordinates(it.latitude, it.longitude) } ?: "موقعیت نامشخص"
            val startAddressText = startPosition?.let { getAddressFromCoordinates(it.latitude, it.longitude) } ?: "موقعیت نامشخص"

            // ۴. فراخوانی API خلاصه گزارش برای آمارها
            val (fromIso, toIso) = getTodayIsoRange()
            val summaryResult = reportRepository.getSummaryReport(
                deviceId = deviceId,
                from = fromIso,
                to = toIso
            )

            // ۵. پردازش و آپدیت UI
            summaryResult.onSuccess { summaryList ->
                val summary = summaryList.firstOrNull()

                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        deviceStatusText = statusText,
                        currentAddress = currentAddressText,
                        startAddress = startAddressText,
                        // تبدیل کیلومترشمار از متر به کیلومتر (معمولاً Traccar به متر برمی‌گرداند)
                        startOdometer = formatMetric(summary?.startOdometer, divider = 1000.0),
                        endOdometer = formatMetric(summary?.endOdometer, divider = 1000.0),
                        spentFuel = formatMetric(summary?.spentFuel),
                        averageSpeed = formatMetric(summary?.averageSpeed, multiplier = 1.852), // نات به کیلومتر بر ساعت

                        // TODO: برای محاسبه دقیق "اولین زمان روشن شدن" و "مدت روشن بودن"
                        // باید از API /reports/trips یا /reports/stops ترکار استفاده کنید.
                        firstIgnitionTime = "۱۰:۲۲".toPersianDigits(),
                        ignitionDuration = "۳ ساعت و ۳۰ دقیقه".toPersianDigits()
                    )
                }
            }.onFailure {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        deviceStatusText = statusText,
                        currentAddress = currentAddressText,
                        startAddress = startAddressText
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

    private suspend fun getAddressFromCoordinates(lat: Double, lon: Double): String {
        // اتصال کامل به لایه Repository که داده را از Map.ir گرفته و کش می‌کند
        return geocoderRepository.getAddress(lat, lon)
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

    private suspend fun getLatestPositionForDevice(deviceId: Long): Position? {
        return null // در آینده به کش پوزیشن‌ها متصل می‌شود
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