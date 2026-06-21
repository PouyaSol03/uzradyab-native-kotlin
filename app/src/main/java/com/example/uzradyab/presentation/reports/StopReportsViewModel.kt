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
import com.example.uzradyab.domain.repository.MapSettingsRepository
import com.example.uzradyab.domain.repository.GeocoderRepository
import javax.inject.Inject

data class StopReportsUiState(
    val devices: List<Device> = emptyList(),
    val selectedDeviceId: Long? = null,
    val isLoading: Boolean = false,
    val reports: List<StopReport> = emptyList(),
    val error: String? = null,
    val fromDateIso: String = "",
    val toDateIso: String = "",
    val selectedDateFilter: String = "امروز",
    val showCustomDatePicker: Boolean = false
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
    
    fun dismissCustomDatePicker() {
        _uiState.update { it.copy(showCustomDatePicker = false) }
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
                _uiState.update { it.copy(isLoading = false, reports = data) }
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
