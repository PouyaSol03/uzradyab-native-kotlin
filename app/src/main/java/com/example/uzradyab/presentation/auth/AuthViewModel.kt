package com.example.uzradyab.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uzradyab.domain.repository.AuthRepository
import com.example.uzradyab.domain.repository.DeviceRepository
import com.example.uzradyab.domain.repository.PositionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val phoneNumber: String = "",
    val password: String = "",
    val name: String = "",
    val confirmPassword: String = "",
    val isSubmitting: Boolean = false,
    val isSignedIn: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val deviceRepository: DeviceRepository,
    private val positionRepository: PositionRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onPhoneNumberChange(value: String) {
        if (value.length <= 11 && value.all(Char::isDigit)) {
            _uiState.update { it.copy(phoneNumber = value, errorMessage = null, infoMessage = null) }
        }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null, infoMessage = null) }
    }

    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value, errorMessage = null, infoMessage = null) }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update { it.copy(confirmPassword = value, errorMessage = null, infoMessage = null) }
    }

    fun login() {
        val state = _uiState.value
        if (state.phoneNumber.length != 11 || state.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "شماره تلفن یا رمز عبور صحیح نیست") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null, infoMessage = null) }
            authRepository.login(state.phoneNumber, state.password)
                .onSuccess {
                    deviceRepository.refreshDevices()
                    positionRepository.refreshLatestPositions()
                    _uiState.update { current -> current.copy(isSubmitting = false, isSignedIn = true) }
                }
                .onFailure {
                    _uiState.update { current ->
                        current.copy(
                            isSubmitting = false,
                            errorMessage = "ورود به برنامه با خطا مواجه شد",
                        )
                    }
                }
        }
    }

    fun registerVisualSubmit() {
        val state = _uiState.value
        when {
            state.name.isBlank() -> {
                _uiState.update {
                    it.copy(
                        errorMessage = "نام و نام خانوادگی را وارد کنید",
                        infoMessage = null,
                    )
                }
            }
            state.phoneNumber.length != 11 -> {
                _uiState.update {
                    it.copy(
                        errorMessage = "شماره تلفن باید ۱۱ رقم باشد",
                        infoMessage = null,
                    )
                }
            }
            else -> {
                _uiState.update {
                    it.copy(
                        errorMessage = null,
                        infoMessage = "کد تایید در مرحله بعد دریافت می‌شود",
                    )
                }
            }
        }
    }
}
