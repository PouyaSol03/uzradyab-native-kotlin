package com.example.uzradyab.feature.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.uzradyab.core.AppContainer
import com.example.uzradyab.core.network.ApiException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AppContainer.sessionRepository(application)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                val user = repository.currentSession()
                val devices = repository.devices()
                val positions = repository.positions().associateBy { it.deviceId }
                Triple(user, devices, positions)
            }.onSuccess { (user, devices, positions) ->
                _uiState.update { state ->
                    state.copy(
                        user = user,
                        devices = devices,
                        positions = positions,
                        selectedDeviceId = state.selectedDeviceId ?: devices.firstOrNull()?.id,
                        isLoading = false,
                        errorMessage = null,
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    if (error is ApiException && error.code == 401) {
                        HomeUiState(isLoading = false, signedOut = true)
                    } else {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message?.takeIf(String::isNotBlank)
                                ?: "خطا در دریافت اطلاعات",
                        )
                    }
                }
            }
        }
    }

    fun selectDevice(deviceId: Long) {
        _uiState.update { it.copy(selectedDeviceId = deviceId) }
    }

    fun signOut() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSigningOut = true, errorMessage = null) }
            runCatching { repository.signOut() }
                .onSuccess {
                    _uiState.update {
                        HomeUiState(isLoading = false, signedOut = true)
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSigningOut = false,
                            errorMessage = error.message?.takeIf(String::isNotBlank)
                                ?: "خروج ناموفق بود",
                        )
                    }
                }
        }
    }
}
