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

data class ReplayUiState(
    val isLoading: Boolean = false,
    val positions: List<Position> = emptyList(),
    val currentIndex: Int = 0,
    val isPlaying: Boolean = false,
    val playSpeed: Int = 1, // 1x or 2x
    val error: String? = null,
    val totalDistanceText: String = "۰ کیلومتر"
)

@HiltViewModel
class ReplayViewModel @Inject constructor(
    private val repository: PositionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val deviceId: Long = savedStateHandle.get<String>("deviceId")?.toLongOrNull() ?: -1L

    private val _state = MutableStateFlow(ReplayUiState())
    val state: StateFlow<ReplayUiState> = _state.asStateFlow()

    private var playbackJob: Job? = null

    init {
        if (deviceId != -1L) {
            fetchRoute()
        } else {
            _state.update { it.copy(error = "دستگاه نامعتبر است") }
        }
    }

    private fun fetchRoute() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            // For now, mock a "Today" filter from 00:00 to 23:59
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

            repository.getPositionsHistory(deviceId, fromTime, toTime)
                .onSuccess { positions ->
                    _state.update { it.copy(
                        positions = positions, 
                        isLoading = false,
                        // Initially show 150 km mocked, or map it properly later
                        totalDistanceText = "۱۵۰ کیلومتر" 
                    ) }
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.localizedMessage) }
                }
        }
    }

    fun togglePlayback() {
        val currentState = _state.value
        if (currentState.positions.isEmpty()) return
        
        if (currentState.isPlaying) {
            pausePlayback()
        } else {
            // If we're at the end, restart
            if (currentState.currentIndex >= currentState.positions.lastIndex) {
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
                
                if (currentState.currentIndex < currentState.positions.lastIndex) {
                    _state.update { it.copy(currentIndex = it.currentIndex + 1) }
                    val delayMs = if (currentState.playSpeed == 1) 500L else 250L
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
        val maxIndex = _state.value.positions.lastIndex
        val safeIndex = index.coerceIn(0, maxIndex.coerceAtLeast(0))
        _state.update { it.copy(currentIndex = safeIndex) }
    }
}
