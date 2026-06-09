package com.example.uzradyab.presentation.alerts

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uzradyab.domain.repository.AuthRepository
import com.example.uzradyab.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlertsSettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private var currentUserId: Long? = null

    // We'll hold the state of each preference here.
    val notificationStates = mutableStateMapOf<String, Boolean>()

    init {
        viewModelScope.launch {
            authRepository.currentSession.collectLatest { session ->
                session?.let {
                    currentUserId = it.id
                    fetchPreferences(it.id)
                }
            }
        }
    }

    private fun fetchPreferences(userId: Long) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            
            val result = notificationRepository.getPreferences(userId)
            if (result.isSuccess) {
                val prefs = result.getOrDefault(emptyMap())
                
                // Initialize default values for the expected keys
                val expectedKeys = listOf(
                    "device_movement", "ignition_on", "ignition_off", "online_status",
                    "geofence_enter", "geofence_exit", "maintenance_required",
                    "high_speed", "accident_sos", "towing", "power_cut", "sms_received"
                )
                
                expectedKeys.forEach { key ->
                    notificationStates[key] = prefs[key] ?: true
                }
            } else {
                errorMessage = result.exceptionOrNull()?.localizedMessage ?: "خطا در دریافت تنظیمات"
            }
            
            isLoading = false
        }
    }

    fun togglePreference(key: String) {
        val userId = currentUserId ?: return
        val currentState = notificationStates[key] ?: return
        val newState = !currentState

        // Optimistic UI update
        notificationStates[key] = newState

        viewModelScope.launch {
            val result = notificationRepository.togglePreference(userId, key)
            if (result.isFailure) {
                // Revert on failure
                notificationStates[key] = currentState
                errorMessage = result.exceptionOrNull()?.localizedMessage ?: "خطا در بروزرسانی تنظیمات"
            }
        }
    }
}
