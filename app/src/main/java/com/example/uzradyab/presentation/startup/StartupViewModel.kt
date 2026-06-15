package com.example.uzradyab.presentation.startup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uzradyab.core.biometric.BiometricHelper
import com.example.uzradyab.domain.repository.AuthRepository
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface StartupNavigationTarget {
    object Onboarding : StartupNavigationTarget
    object SignIn : StartupNavigationTarget
    object Home : StartupNavigationTarget
}

sealed interface StartupUiState {
    object Checking : StartupUiState
    object BiometricRequired : StartupUiState
    data class BiometricFailed(val message: String) : StartupUiState
}

@HiltViewModel
class StartupViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context,
    private val biometricHelper: BiometricHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow<StartupUiState>(StartupUiState.Checking)
    val uiState: StateFlow<StartupUiState> = _uiState.asStateFlow()

    private val _navigationTarget = MutableStateFlow<StartupNavigationTarget?>(null)
    val navigationTarget: StateFlow<StartupNavigationTarget?> = _navigationTarget.asStateFlow()

    init {
        checkStatus()
    }

    fun checkStatus() {
        viewModelScope.launch {
            _uiState.value = StartupUiState.Checking
            _navigationTarget.value = null
            
            // 1. Check onboarding completion
            val sharedPrefs = context.getSharedPreferences("uzradyab_prefs", Context.MODE_PRIVATE)
            val isOnboardingCompleted = sharedPrefs.getBoolean("onboarding_completed", false)
            if (!isOnboardingCompleted) {
                _navigationTarget.value = StartupNavigationTarget.Onboarding
                return@launch
            }

            // 2. Check session expiry (which clears database session internally if older than 30 days)
            val isExpired = authRepository.isSessionExpired()
            if (isExpired) {
                _navigationTarget.value = StartupNavigationTarget.SignIn
                return@launch
            }

            // 3. Check if session exists in DB
            val session = authRepository.currentSession.first()
            if (session == null) {
                _navigationTarget.value = StartupNavigationTarget.SignIn
                return@launch
            }

            // 4. Check biometric availability and user preference
            if (biometricHelper.isBiometricAvailable() && biometricHelper.isBiometricEnabled()) {
                _uiState.value = StartupUiState.BiometricRequired
            } else {
                // Device does not support or have biometrics set up, bypass and go straight to Home
                _navigationTarget.value = StartupNavigationTarget.Home
            }
        }
    }

    fun onBiometricSuccess() {
        _navigationTarget.value = StartupNavigationTarget.Home
    }

    fun onBiometricFailure(message: String) {
        _uiState.value = StartupUiState.BiometricFailed(message)
    }

    fun logoutAndGoToSignIn() {
        viewModelScope.launch {
            authRepository.logout()
            _navigationTarget.value = StartupNavigationTarget.SignIn
        }
    }
}
