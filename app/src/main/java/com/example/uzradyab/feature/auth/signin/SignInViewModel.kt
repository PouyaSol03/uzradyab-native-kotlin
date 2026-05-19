package com.example.uzradyab.feature.auth.signin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.uzradyab.core.AppContainer
import com.example.uzradyab.core.network.ApiException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SignInViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AppContainer.sessionRepository(application)

    private val _uiState = MutableStateFlow(SignInUiState(isCheckingSession = true))
    val uiState: StateFlow<SignInUiState> = _uiState

    init {
        viewModelScope.launch {
            runCatching { repository.currentSession() }
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(isCheckingSession = false, signedIn = true)
                    }
                }
                .onFailure {
                    _uiState.update { state -> state.copy(isCheckingSession = false) }
                }
        }
    }

    fun onPhoneNumberChange(value: String) {
        _uiState.update {
            it.copy(
                phoneNumber = value.filter(Char::isDigit).take(11),
                errorMessage = null,
            )
        }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null) }
    }

    fun onPasswordVisibilityChange() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun signIn() {
        val state = _uiState.value
        if (state.phoneNumber.length != 11) {
            _uiState.update { it.copy(errorMessage = "شماره تلفن باید ۱۱ رقم باشد") }
            return
        }
        if (state.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "رمز عبور را وارد کنید") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            runCatching {
                repository.signIn(
                    phoneNumber = state.phoneNumber,
                    password = state.password,
                )
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        signedIn = true,
                        password = "",
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        password = "",
                        errorMessage = when {
                            error is ApiException && error.code == 401 -> {
                                "شماره تلفن یا رمز عبور درست نیست"
                            }
                            else -> error.message?.takeIf(String::isNotBlank) ?: "ورود ناموفق بود"
                        },
                    )
                }
            }
        }
    }
}
