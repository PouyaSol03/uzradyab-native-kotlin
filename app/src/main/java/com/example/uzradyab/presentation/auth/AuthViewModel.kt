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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

private const val RegisterOtpLength = 6
private const val RegisterOtpDurationSeconds = 90

enum class RegisterStep {
    Details,
    Otp,
    Password,
}

sealed interface AuthUiEffect {
    object NavigateToHome : AuthUiEffect
    data class ShowError(val message: String) : AuthUiEffect
    data class ShowInfo(val message: String) : AuthUiEffect
    object TriggerBiometric : AuthUiEffect
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
    val isPrivacyPolicyAccepted: Boolean = false,
    val canUseBiometric: Boolean = false,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val registrationRepository: RegistrationRepository,
    private val fcmTokenManager: FcmTokenManager,
    private val deviceRepository: DeviceRepository,
    private val positionRepository: PositionRepository,
    private val biometricHelper: com.example.uzradyab.core.biometric.BiometricHelper,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    
    private val _effect = Channel<AuthUiEffect>()
    val effect = _effect.receiveAsFlow()
    
    private var otpTimerJob: Job? = null
    private var hasActiveSession = false

    init {
        viewModelScope.launch {
            val session = authRepository.currentSession.firstOrNull()
            hasActiveSession = session != null
            val biometricAvailable = biometricHelper.isBiometricAvailable()
            val biometricEnabled = biometricHelper.isBiometricEnabled()
            val hasSavedCredentials = biometricHelper.getSavedPhone() != null && biometricHelper.getSavedPassword() != null
            
            _uiState.update { 
                it.copy(canUseBiometric = biometricAvailable) 
            }
            
            if (hasSavedCredentials && biometricAvailable && biometricEnabled) {
                _effect.send(AuthUiEffect.TriggerBiometric)
            }
        }
    }

    fun onPhoneNumberChange(value: String) {
        if (value.length <= 11 && value.all(Char::isDigit)) {
            _uiState.update { it.copy(phoneNumber = value) }
        }
    }

    fun onPrivacyPolicyAcceptChange(isAccepted: Boolean) {
        _uiState.update { it.copy(isPrivacyPolicyAccepted = isAccepted) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update {
            it.copy(
                password = value,
                passwordRules = passwordRuleState(value),
            )
        }
    }

    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value) }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update { it.copy(confirmPassword = value) }
    }

    fun onOtpChange(value: String) {
        if (value.length <= RegisterOtpLength && value.all(Char::isDigit)) {
            _uiState.update { it.copy(otp = value) }
        }
    }

    fun login() {
        val state = _uiState.value
        if (state.phoneNumber.length != 11 || state.password.isBlank()) {
            viewModelScope.launch { _effect.send(AuthUiEffect.ShowError("شماره تلفن یا رمز عبور صحیح نیست")) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            authRepository.login(state.phoneNumber, state.password)
                .onSuccess {
                    biometricHelper.saveCredentials(state.phoneNumber, state.password)
                    _uiState.update { it.copy(isSubmitting = false) }
                    _effect.send(AuthUiEffect.NavigateToHome)
                    
                    CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                        fcmTokenManager.syncCurrentToken()
                        deviceRepository.refreshDevices()
                        positionRepository.refreshLatestPositions()
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(isSubmitting = false) }
                    _effect.send(AuthUiEffect.ShowError("ورود به برنامه با خطا مواجه شد"))
                }
        }
    }

    fun onBiometricClicked() {
        val hasSavedCredentials = biometricHelper.getSavedPhone() != null && biometricHelper.getSavedPassword() != null
        if (!hasSavedCredentials) {
            viewModelScope.launch { _effect.send(AuthUiEffect.ShowInfo("برای ورود با اثر انگشت، ابتدا یکبار با رمز عبور وارد شوید.")) }
            return
        }
        if (!biometricHelper.isBiometricEnabled()) {
            viewModelScope.launch { _effect.send(AuthUiEffect.ShowInfo("لطفاً ابتدا اثر انگشت را در تنظیمات برنامه فعال کنید.")) }
            return
        }
        viewModelScope.launch { _effect.send(AuthUiEffect.TriggerBiometric) }
    }

    fun onBiometricSuccess() {
        if (hasActiveSession) {
            viewModelScope.launch { _effect.send(AuthUiEffect.NavigateToHome) }
        } else {
            val phone = biometricHelper.getSavedPhone()
            val pass = biometricHelper.getSavedPassword()
            if (phone != null && pass != null) {
                viewModelScope.launch {
                    _uiState.update { it.copy(isSubmitting = true) }
                    authRepository.login(phone, pass)
                        .onSuccess {
                            _uiState.update { it.copy(isSubmitting = false) }
                            _effect.send(AuthUiEffect.NavigateToHome)
                            
                            CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                                fcmTokenManager.syncCurrentToken()
                                deviceRepository.refreshDevices()
                                positionRepository.refreshLatestPositions()
                            }
                        }
                        .onFailure {
                            _uiState.update { it.copy(isSubmitting = false) }
                            _effect.send(AuthUiEffect.ShowError("ورود با اثر انگشت با خطا مواجه شد"))
                        }
                }
            } else {
                viewModelScope.launch { _effect.send(AuthUiEffect.ShowError("اطلاعات ورود یافت نشد. لطفا یکبار با رمز عبور وارد شوید.")) }
            }
        }
    }

    fun registerVisualSubmit() {
        val state = _uiState.value
        when {
            state.name.isBlank() -> {
                viewModelScope.launch { _effect.send(AuthUiEffect.ShowError("نام و نام خانوادگی را وارد کنید")) }
            }
            !isValidIranPhoneNumber(state.phoneNumber) -> {
                viewModelScope.launch { _effect.send(AuthUiEffect.ShowError("شماره تلفن باید ۱۱ رقم باشد")) }
            }
            else -> {
                viewModelScope.launch {
                    _uiState.update { it.copy(isSubmitting = true) }
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
                            _uiState.update { it.copy(isSubmitting = false) }
                            _effect.send(AuthUiEffect.ShowError("ارسال کد تایید با خطا مواجه شد"))
                        }
                }
            }
        }
    }

    fun verifyRegisterOtp() {
        val state = _uiState.value
        if (state.otp.length != RegisterOtpLength) {
            viewModelScope.launch { _effect.send(AuthUiEffect.ShowError("کد تایید باید ۶ رقم باشد")) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
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
                    _uiState.update { it.copy(isSubmitting = false) }
                    _effect.send(AuthUiEffect.ShowError("کد تایید نادرست است"))
                }
        }
    }

    fun resendRegisterOtp() {
        val state = _uiState.value
        if (!state.canResendOtp || state.isSubmitting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
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
                    _uiState.update { it.copy(isSubmitting = false) }
                    _effect.send(AuthUiEffect.ShowError("ارسال مجدد کد تایید با خطا مواجه شد"))
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
            )
        }
    }

    fun completeRegistration() {
        val state = _uiState.value
        when {
            !state.isPrivacyPolicyAccepted -> {
                viewModelScope.launch { _effect.send(AuthUiEffect.ShowError("لطفا قوانین و مقررات را تایید کنید")) }
            }
            !state.passwordRules.isValid -> {
                viewModelScope.launch { _effect.send(AuthUiEffect.ShowError("رمز عبور باید شرایط امنیتی را داشته باشد")) }
            }
            state.password != state.confirmPassword -> {
                viewModelScope.launch { _effect.send(AuthUiEffect.ShowError("رمز عبور و تایید آن یکسان نیست")) }
            }
            else -> {
                viewModelScope.launch {
                    _uiState.update { it.copy(isSubmitting = true) }
                    registrationRepository.createUserAndLogin(
                        name = state.name,
                        phoneNumber = state.phoneNumber,
                        password = state.password,
                    ).onSuccess {
                        _uiState.update { it.copy(isSubmitting = false) }
                        _effect.send(AuthUiEffect.NavigateToHome)
                        
                        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                            fcmTokenManager.syncCurrentToken()
                            deviceRepository.refreshDevices()
                            positionRepository.refreshLatestPositions()
                        }
                    }.onFailure {
                        _uiState.update { it.copy(isSubmitting = false) }
                        _effect.send(AuthUiEffect.ShowError("تکمیل عضویت با خطا مواجه شد"))
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
