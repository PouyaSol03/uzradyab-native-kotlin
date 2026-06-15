package com.example.uzradyab.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uzradyab.domain.manager.FcmTokenManager
import com.example.uzradyab.domain.repository.AuthRepository
import com.example.uzradyab.domain.repository.DeviceRepository
import com.example.uzradyab.domain.repository.PositionRepository
import com.example.uzradyab.domain.repository.RegistrationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val RegisterOtpLength = 6
private const val RegisterOtpDurationSeconds = 90

enum class RegisterStep {
    Details,
    Otp,
    Password,
}

data class AuthUiState(
    val phoneNumber: String = "",
    val password: String = "",
    val name: String = "",
    val confirmPassword: String = "",
    val otp: String = "",
    val registerStep: RegisterStep = RegisterStep.Details,
    val remainingOtpSeconds: Int = RegisterOtpDurationSeconds,
    val canResendOtp: Boolean = false,
    val passwordRules: PasswordRuleState = PasswordRuleState(),
    val isSubmitting: Boolean = false,
    val isSignedIn: Boolean = false,
    val isPrivacyPolicyAccepted: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val registrationRepository: RegistrationRepository,
    private val fcmTokenManager: FcmTokenManager,
    private val deviceRepository: DeviceRepository,
    private val positionRepository: PositionRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    private var otpTimerJob: Job? = null

    fun onPhoneNumberChange(value: String) {
        if (value.length <= 11 && value.all(Char::isDigit)) {
            _uiState.update { it.copy(phoneNumber = value, errorMessage = null, infoMessage = null) }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, infoMessage = null) }
    }

    fun onPrivacyPolicyAcceptChange(isAccepted: Boolean) {
        _uiState.update { it.copy(isPrivacyPolicyAccepted = isAccepted, errorMessage = null, infoMessage = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update {
            it.copy(
                password = value,
                passwordRules = passwordRuleState(value),
                errorMessage = null,
                infoMessage = null,
            )
        }
    }

    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value, errorMessage = null, infoMessage = null) }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update { it.copy(confirmPassword = value, errorMessage = null, infoMessage = null) }
    }

    fun onOtpChange(value: String) {
        if (value.length <= RegisterOtpLength && value.all(Char::isDigit)) {
            _uiState.update { it.copy(otp = value, errorMessage = null, infoMessage = null) }
        }
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
                    fcmTokenManager.syncCurrentToken()
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
            !isValidIranPhoneNumber(state.phoneNumber) -> {
                _uiState.update {
                    it.copy(
                        errorMessage = "شماره تلفن باید ۱۱ رقم باشد",
                        infoMessage = null,
                    )
                }
            }
            else -> {
                viewModelScope.launch {
                    _uiState.update { it.copy(isSubmitting = true, errorMessage = null, infoMessage = null) }
                    registrationRepository.sendOtp(state.phoneNumber)
                        .onSuccess {
                            _uiState.update {
                                it.copy(
                                    isSubmitting = false,
                                    registerStep = RegisterStep.Otp,
                                    otp = "",
                                    remainingOtpSeconds = RegisterOtpDurationSeconds,
                                    canResendOtp = false,
                                )
                            }
                            startOtpTimer()
                        }
                        .onFailure {
                            _uiState.update { current ->
                                current.copy(
                                    isSubmitting = false,
                                    errorMessage = "ارسال کد تایید با خطا مواجه شد",
                                )
                            }
                        }
                }
            }
        }
    }

    fun verifyRegisterOtp() {
        val state = _uiState.value
        if (state.otp.length != RegisterOtpLength) {
            _uiState.update { it.copy(errorMessage = "کد تایید باید ۶ رقم باشد") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null, infoMessage = null) }
            registrationRepository.verifyOtp(state.phoneNumber, state.otp)
                .onSuccess {
                    otpTimerJob?.cancel()
                    _uiState.update { current ->
                        current.copy(
                            isSubmitting = false,
                            registerStep = RegisterStep.Password,
                            password = "",
                            confirmPassword = "",
                            passwordRules = PasswordRuleState(),
                        )
                    }
                }
                .onFailure {
                    _uiState.update { current ->
                        current.copy(
                            isSubmitting = false,
                            errorMessage = "کد تایید نادرست است",
                        )
                    }
                }
        }
    }

    fun resendRegisterOtp() {
        val state = _uiState.value
        if (!state.canResendOtp || state.isSubmitting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null, infoMessage = null) }
            registrationRepository.sendOtp(state.phoneNumber)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            otp = "",
                            remainingOtpSeconds = RegisterOtpDurationSeconds,
                            canResendOtp = false,
                        )
                    }
                    startOtpTimer()
                }
                .onFailure {
                    _uiState.update { current ->
                        current.copy(
                            isSubmitting = false,
                            errorMessage = "ارسال مجدد کد تایید با خطا مواجه شد",
                        )
                    }
                }
        }
    }

    fun changeRegisterPhone() {
        otpTimerJob?.cancel()
        _uiState.update {
            it.copy(
                registerStep = RegisterStep.Details,
                otp = "",
                remainingOtpSeconds = RegisterOtpDurationSeconds,
                canResendOtp = false,
                errorMessage = null,
                infoMessage = null,
            )
        }
    }

    fun completeRegistration() {
        val state = _uiState.value
        when {
            !state.isPrivacyPolicyAccepted -> {
                _uiState.update { it.copy(errorMessage = "لطفا قوانین و مقررات را تایید کنید") }
            }
            !state.passwordRules.isValid -> {
                _uiState.update { it.copy(errorMessage = "رمز عبور باید شرایط امنیتی را داشته باشد") }
            }
            state.password != state.confirmPassword -> {
                _uiState.update { it.copy(errorMessage = "رمز عبور و تایید آن یکسان نیست") }
            }
            else -> {
                viewModelScope.launch {
                    _uiState.update { it.copy(isSubmitting = true, errorMessage = null, infoMessage = null) }
                    registrationRepository.createUserAndLogin(
                        name = state.name,
                        phoneNumber = state.phoneNumber,
                        password = state.password,
                    ).onSuccess {
                        fcmTokenManager.syncCurrentToken()
                        deviceRepository.refreshDevices()
                        positionRepository.refreshLatestPositions()
                        _uiState.update { current ->
                            current.copy(isSubmitting = false, isSignedIn = true)
                        }
                    }.onFailure {
                        _uiState.update { current ->
                            current.copy(
                                isSubmitting = false,
                                errorMessage = "تکمیل عضویت با خطا مواجه شد",
                            )
                        }
                    }
                }
            }
        }
    }

    private fun startOtpTimer() {
        otpTimerJob?.cancel()
        otpTimerJob = viewModelScope.launch {
            while (_uiState.value.remainingOtpSeconds > 0) {
                delay(1_000)
                _uiState.update {
                    val next = (it.remainingOtpSeconds - 1).coerceAtLeast(0)
                    it.copy(
                        remainingOtpSeconds = next,
                        canResendOtp = next == 0,
                    )
                }
            }
        }
    }
}
