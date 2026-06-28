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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
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

    private val _effect = Channel<StartupNavigationTarget>()
    val effect = _effect.receiveAsFlow()

    init {
        checkStatus()
    }

    fun checkStatus() {
        viewModelScope.launch {
            _uiState.value = StartupUiState.Checking
            
            // 1. Check onboarding completion
            val sharedPrefs = context.getSharedPreferences("uzradyab_prefs", Context.MODE_PRIVATE)
            val isOnboardingCompleted = sharedPrefs.getBoolean("onboarding_completed", false)
            if (!isOnboardingCompleted) {
                _effect.send(StartupNavigationTarget.Onboarding)
                return@launch
            }

            // 2. Check session expiry (which clears database session internally if older than 30 days)
            val isExpired = authRepository.isSessionExpired()
            if (isExpired) {
                _effect.send(StartupNavigationTarget.SignIn)
                return@launch
            }

            // 3. Check if session exists in DB
            val session = authRepository.currentSession.first()
            if (session == null) {
                _effect.send(StartupNavigationTarget.SignIn)
                return@launch
            }

            // Go straight to Home if session is active
            _effect.send(StartupNavigationTarget.Home)
        }
    }

    fun onBiometricSuccess() {
        viewModelScope.launch {
            _effect.send(StartupNavigationTarget.Home)
        }
    }

    fun onBiometricFailure(message: String) {
        _uiState.value = StartupUiState.BiometricFailed(message)
    }

    fun logoutAndGoToSignIn() {
        viewModelScope.launch {
            authRepository.logout()
            _effect.send(StartupNavigationTarget.SignIn)
        }
    }
}
