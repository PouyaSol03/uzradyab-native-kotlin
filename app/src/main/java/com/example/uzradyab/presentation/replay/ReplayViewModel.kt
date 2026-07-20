package com.example.uzradyab.presentation.replay

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uzradyab.domain.model.Position
import com.example.uzradyab.domain.repository.PositionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

import com.example.uzradyab.core.utils.ImmutableListWrapper
import com.example.uzradyab.core.utils.emptyImmutableList
import com.example.uzradyab.core.utils.toImmutable

data class ReplayUiState(
    val isLoading: Boolean = false,
    val positions: ImmutableListWrapper<Position> = emptyImmutableList(),
    val currentIndex: Int = 0,
    val isPlaying: Boolean = false,
    val playSpeed: Int = 1, // 1x or 2x
    val error: String? = null,
    val totalDistanceText: String = "۰ کیلومتر",
    val dateFilterText: String = "",
    val mapStyle: String = "osm",
    val startAddress: String? = null,
    val endAddress: String? = null,
)

@HiltViewModel
class ReplayViewModel @Inject constructor(
    private val repository: PositionRepository,
    private val reportRepository: com.example.uzradyab.domain.repository.ReportRepository,
    private val mapSettingsRepository: com.example.uzradyab.domain.repository.MapSettingsRepository,
    private val geocoderRepository: com.example.uzradyab.domain.repository.GeocoderRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val deviceId: Long = savedStateHandle.get<String>("deviceId")?.toLongOrNull() ?: -1L

    private val _state = MutableStateFlow(ReplayUiState())
    val state: StateFlow<ReplayUiState> = _state.asStateFlow()

    private var playbackJob: Job? = null

    init {
        _state.update { it.copy(dateFilterText = com.example.uzradyab.core.utils.JalaliUtils.getTodayJalaliString()) }
        if (deviceId != -1L) {
            fetchRoute()
        } else {
            _state.update { it.copy(error = "دستگاه نامعتبر است") }
        }
        
        viewModelScope.launch {
            mapSettingsRepository.observeMapStyle().collect { style ->
                _state.update { it.copy(mapStyle = style) }
            }
        }
    }

    private fun fetchRoute() {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        val fromTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply { 
            timeZone = TimeZone.getTimeZone("UTC") 
        }.format(cal.time)
        
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        val toTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply { 
            timeZone = TimeZone.getTimeZone("UTC") 
        }.format(cal.time)

        fetchRouteForDateRange(fromTime, toTime, com.example.uzradyab.core.utils.JalaliUtils.getTodayJalaliString())
    }

    fun fetchRouteForDateRange(fromTime: String, toTime: String, filterLabel: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, dateFilterText = filterLabel) }

            // Fetch summary for distance calculation
            val summaryResult = reportRepository.getSummaryReport(deviceId, fromTime, toTime)

            repository.getPositionsHistory(deviceId, fromTime, toTime)
                .onSuccess { allPositions ->
                    val positions = allPositions.takeLast(200)
                    val distanceMeters = summaryResult.getOrNull()?.firstOrNull()?.distance ?: 0.0
                    val distanceKm = distanceMeters / 1000.0
                    
                    val decimalFormat = java.text.DecimalFormat("#.##", java.text.DecimalFormatSymbols(Locale.US))
                    val formattedDistance = decimalFormat.format(distanceKm) + " کیلومتر"
                    val persianDistance = com.example.uzradyab.core.utils.JalaliUtils.run { formattedDistance.toPersianDigits() }

                    _state.update { it.copy(
                        positions = positions.toImmutable(), 
                        currentIndex = 0,
                        isPlaying = false,
                        isLoading = false,
                        totalDistanceText = persianDistance,
                        error = if (positions.isEmpty()) "در بازه زمانی انتخاب شده، هیچ مسیر پیموده‌ای برای این دستگاه یافت نشد." else null,
                        startAddress = null,
                        endAddress = null
                    ) }
                    
                    if (positions.isNotEmpty()) {
                        viewModelScope.launch {
                            try {
                                val first = positions.first()
                                val last = positions.last()
                                val startAddr = geocoderRepository.getAddress(first.latitude, first.longitude)
                                val endAddr = geocoderRepository.getAddress(last.latitude, last.longitude)
                                _state.update { it.copy(startAddress = startAddr, endAddress = endAddr) }
                            } catch (e: Exception) {
                                // ignore
                            }
                        }
                    }
                    playbackJob?.cancel()
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.localizedMessage) }
                }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun togglePlayback() {
        val currentState = _state.value
        if (currentState.positions.items.isEmpty()) return
        
        if (currentState.isPlaying) {
            pausePlayback()
        } else {
            // If we're at the end, restart
            if (currentState.currentIndex >= currentState.positions.items.lastIndex) {
                _state.update { it.copy(currentIndex = 0) }
            }
            startPlayback()
        }
    }

    private fun startPlayback() {
        _state.update { it.copy(isPlaying = true) }
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            while (isActive) {
                val currentState = _state.value
                if (!currentState.isPlaying) break
                
                if (currentState.currentIndex < currentState.positions.items.lastIndex) {
                    _state.update { it.copy(currentIndex = it.currentIndex + 1) }
                    val delayMs = if (currentState.playSpeed == 1) 1500L else 750L
                    delay(delayMs)
                } else {
                    _state.update { it.copy(isPlaying = false) }
                    break
                }
            }
        }
    }

    private fun pausePlayback() {
        _state.update { it.copy(isPlaying = false) }
        playbackJob?.cancel()
    }

    fun stopPlayback() {
        _state.update { it.copy(isPlaying = false, currentIndex = 0) }
        playbackJob?.cancel()
    }

    fun toggleSpeed() {
        _state.update { it.copy(playSpeed = if (it.playSpeed == 1) 2 else 1) }
    }

    fun setIndex(index: Int) {
        val maxIndex = _state.value.positions.items.lastIndex
        val safeIndex = index.coerceIn(0, maxIndex.coerceAtLeast(0))
        _state.update { it.copy(currentIndex = safeIndex) }
    }

    fun applyQuickRange(range: String) {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }
        val now = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Tehran"))
        
        val currentGYear = now.get(java.util.Calendar.YEAR)
        val currentGMonth = now.get(java.util.Calendar.MONTH) + 1
        val currentGDay = now.get(java.util.Calendar.DAY_OF_MONTH)
        val jDate = com.example.uzradyab.core.utils.JalaliUtils.gregorianToJalali(currentGYear, currentGMonth, currentGDay)
        
        val fromCal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
        val toCal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
        
        val label = when (range) {
            "امروز" -> com.example.uzradyab.core.utils.JalaliUtils.run { "امروز | ${jDate[2]} ${com.example.uzradyab.core.utils.JalaliUtils.getMonthName(jDate[1])} ${jDate[0]}".toPersianDigits() }
            "دیروز" -> {
                val yCal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Tehran"))
                yCal.add(java.util.Calendar.DAY_OF_MONTH, -1)
                val yJDate = com.example.uzradyab.core.utils.JalaliUtils.gregorianToJalali(yCal.get(java.util.Calendar.YEAR), yCal.get(java.util.Calendar.MONTH) + 1, yCal.get(java.util.Calendar.DAY_OF_MONTH))
                com.example.uzradyab.core.utils.JalaliUtils.run { "دیروز | ${yJDate[2]} ${com.example.uzradyab.core.utils.JalaliUtils.getMonthName(yJDate[1])} ${yJDate[0]}".toPersianDigits() }
            }
            else -> range // For this week and this month, keep it simple or expand if needed
        }
        
        when (range) {
            "امروز" -> {
                fromCal.set(java.util.Calendar.HOUR_OF_DAY, 0); fromCal.set(java.util.Calendar.MINUTE, 0); fromCal.set(java.util.Calendar.SECOND, 0)
                toCal.set(java.util.Calendar.HOUR_OF_DAY, 23); toCal.set(java.util.Calendar.MINUTE, 59); toCal.set(java.util.Calendar.SECOND, 59)
            }
            "دیروز" -> {
                fromCal.add(java.util.Calendar.DAY_OF_MONTH, -1)
                fromCal.set(java.util.Calendar.HOUR_OF_DAY, 0); fromCal.set(java.util.Calendar.MINUTE, 0); fromCal.set(java.util.Calendar.SECOND, 0)
                toCal.add(java.util.Calendar.DAY_OF_MONTH, -1)
                toCal.set(java.util.Calendar.HOUR_OF_DAY, 23); toCal.set(java.util.Calendar.MINUTE, 59); toCal.set(java.util.Calendar.SECOND, 59)
            }
            "هفته جاری" -> {
                val dow = com.example.uzradyab.core.utils.JalaliUtils.getDayOfWeekJalali(jDate[0], jDate[1], jDate[2]) // 0=Sat
                fromCal.add(java.util.Calendar.DAY_OF_MONTH, -dow)
                fromCal.set(java.util.Calendar.HOUR_OF_DAY, 0); fromCal.set(java.util.Calendar.MINUTE, 0); fromCal.set(java.util.Calendar.SECOND, 0)
            }
            "ماه جاری" -> {
                val gStartOfMonth = com.example.uzradyab.core.utils.JalaliUtils.jalaliToGregorian(jDate[0], jDate[1], 1)
                fromCal.set(gStartOfMonth[0], gStartOfMonth[1] - 1, gStartOfMonth[2], 0, 0, 0)
            }
        }
        
        val fromStr = sdf.format(fromCal.time)
        val toStr = sdf.format(toCal.time)
        fetchRouteForDateRange(fromStr, toStr, label)
    }

    fun applyCustomRange(
        start: com.example.uzradyab.presentation.components.JalaliDateTime?, 
        end: com.example.uzradyab.presentation.components.JalaliDateTime?
    ) {
        if (start == null) return
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }
        
        val startG = com.example.uzradyab.core.utils.JalaliUtils.jalaliToGregorian(start.year, start.month, start.day)
        val fromCal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
            set(startG[0], startG[1] - 1, startG[2], start.hour, start.minute, 0)
        }
        
        val toCal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
        if (end != null) {
            val endG = com.example.uzradyab.core.utils.JalaliUtils.jalaliToGregorian(end.year, end.month, end.day)
            toCal.set(endG[0], endG[1] - 1, endG[2], end.hour, end.minute, 59)
        }
        
        val fromStr = sdf.format(fromCal.time)
        val toStr = sdf.format(toCal.time)
        
        val endLabel = if (end != null) " تا ${end.day} ${com.example.uzradyab.core.utils.JalaliUtils.getMonthName(end.month)}" else ""
        val label = com.example.uzradyab.core.utils.JalaliUtils.run { "${start.day} ${com.example.uzradyab.core.utils.JalaliUtils.getMonthName(start.month)}$endLabel".toPersianDigits() }
        fetchRouteForDateRange(fromStr, toStr, label)
    }
}
