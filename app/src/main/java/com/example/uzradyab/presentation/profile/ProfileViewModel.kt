package com.example.uzradyab.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uzradyab.data.remote.api.TraccarApi
import com.example.uzradyab.data.remote.dto.SessionDto
import com.example.uzradyab.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val sessionDto: SessionDto? = null,
    val error: String? = null,
    val saveSuccess: Boolean = false,
    val signedOut: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val api: TraccarApi,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // Fetch latest session / user info
                val session = api.getSession()
                _uiState.update { it.copy(isLoading = false, sessionDto = session) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage ?: "خطا در دریافت اطلاعات") }
            }
        }
    }

    fun updateProfile(updatedSession: SessionDto) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null, saveSuccess = false) }
            try {
                val updatedUser = api.updateUser(updatedSession.id, updatedSession)
                _uiState.update { 
                    it.copy(
                        isSaving = false, 
                        saveSuccess = true, 
                        sessionDto = updatedUser 
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.localizedMessage ?: "خطا در ذخیره اطلاعات") }
            }
        }
    }
    
    fun resetSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                authRepository.logout()
            } finally {
                _uiState.update { it.copy(signedOut = true) }
            }
        }
    }
}
