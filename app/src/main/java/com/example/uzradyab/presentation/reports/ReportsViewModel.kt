package com.example.uzradyab.presentation.reports

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uzradyab.domain.model.Device
import com.example.uzradyab.domain.model.CombinedReportItem
import com.example.uzradyab.domain.repository.DeviceRepository
import com.example.uzradyab.domain.repository.ReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val reportRepository: ReportRepository,
) : ViewModel() {

    var devices by mutableStateOf<List<Device>>(emptyList())
        private set

    var selectedDeviceIds by mutableStateOf<Set<Long>>(emptySet())
        private set

    var period by mutableStateOf("today") // today, yesterday, thisWeek, thisMonth, custom
        private set

    var customFromDate by mutableStateOf<Date?>(null)
    var customToDate by mutableStateOf<Date?>(null)

    var isLoading by mutableStateOf(false)
        private set

    var reportItems by mutableStateOf<List<CombinedReportItem>>(emptyList())
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        viewModelScope.launch {
            deviceRepository.observeDevices().collectLatest { list ->
                devices = list.sortedBy { it.name }
                if (selectedDeviceIds.isEmpty() && list.isNotEmpty()) {
                    selectedDeviceIds = setOf(list.first().id)
                }
            }
        }
    }

    fun toggleDeviceSelection(deviceId: Long) {
        selectedDeviceIds = if (selectedDeviceIds.contains(deviceId)) {
            selectedDeviceIds - deviceId
        } else {
            selectedDeviceIds + deviceId
        }
    }

    fun selectAllDevices() {
        selectedDeviceIds = devices.map { it.id }.toSet()
    }

    fun clearDeviceSelection() {
        selectedDeviceIds = emptySet()
    }

    fun onPeriodChange(newPeriod: String) {
        period = newPeriod
    }

    fun loadReport() {
        if (selectedDeviceIds.isEmpty()) {
            errorMessage = "لطفاً حداقل یک دستگاه را انتخاب کنید."
            return
        }

        if (period == "custom" && (customFromDate == null || customToDate == null)) {
            errorMessage = "لطفاً بازه زمانی شروع و پایان را انتخاب کنید."
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            val dates = calculateRange(period)
            val fromStr = dates.first
            val toStr = dates.second

            android.util.Log.d("ReportsViewModel", "Loading combined report from $fromStr to $toStr for devices: $selectedDeviceIds")

            val result = reportRepository.getCombinedReport(
                deviceIds = selectedDeviceIds.toList(),
                from = fromStr,
                to = toStr
            )

            result.onSuccess {
                android.util.Log.d("ReportsViewModel", "Combined report loaded successfully. Items count: ${it.size}")
                reportItems = it
                if (it.isEmpty() || it.flatMap { item -> item.events }.isEmpty()) {
                    errorMessage = "هیچ رویدادی برای بازه زمانی انتخاب شده یافت نشد."
                }
            }.onFailure { exception ->
                android.util.Log.e("ReportsViewModel", "Failed to load combined report from $fromStr to $toStr", exception)
                reportItems = emptyList()
                errorMessage = exception.localizedMessage ?: "خطایی در دریافت گزارش رخ داد."
            }
            isLoading = false
        }
    }

    private fun calculateRange(period: String): Pair<String, String> {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val cal = Calendar.getInstance()

        val from: Date
        val to: Date

        when (period) {
            "today" -> {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                from = cal.time
                
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                to = cal.time
            }
            "yesterday" -> {
                cal.add(Calendar.DAY_OF_YEAR, -1)
                
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                from = cal.time
                
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                to = cal.time
            }
            "thisWeek" -> {
                // Adjust to Saturday as week start in Iran
                val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
                val diff = if (dayOfWeek >= Calendar.SATURDAY) {
                    Calendar.SATURDAY - dayOfWeek
                } else {
                    -dayOfWeek - 1
                }
                cal.add(Calendar.DAY_OF_WEEK, diff)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                from = cal.time

                cal.add(Calendar.DAY_OF_WEEK, 6)
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                to = cal.time
            }
            "thisMonth" -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                from = cal.time

                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                to = cal.time
            }
            "custom" -> {
                from = customFromDate ?: Date()
                to = customToDate ?: Date()
            }
            else -> {
                from = Date()
                to = Date()
            }
        }

        return Pair(sdf.format(from), sdf.format(to))
    }
}
