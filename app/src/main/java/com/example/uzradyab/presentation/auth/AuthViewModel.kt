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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


import com.example.uzradyab.core.security.SecureCredentialManager

private const val RegisterOtpLength = 6
private const val RegisterOtpDurationSeconds = 90

enum class RegisterStep {
    Details,
    Otp,
    Password,
}

enum class AuthFlow {
    Login,
    Register,
    ForgotPassword
}

enum class ForgotPasswordStep {
    Phone,
    Otp,
    NewPassword
}

data class AuthUiState(
    val authFlow: AuthFlow = AuthFlow.Login,
    val forgotPasswordStep: ForgotPasswordStep = ForgotPasswordStep.Phone,
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
    val canUseBiometric: Boolean = false,
    val shouldAutoTriggerBiometric: Boolean = false,
    val rememberMe: Boolean = true,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val registrationRepository: RegistrationRepository,
    private val fcmTokenManager: FcmTokenManager,
    private val deviceRepository: DeviceRepository,
    private val positionRepository: PositionRepository,
    private val biometricHelper: com.example.uzradyab.core.biometric.BiometricHelper,
    private val credentialManager: SecureCredentialManager,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    private var otpTimerJob: Job? = null

    private var hasActiveSession = false

    init {
        viewModelScope.launch {
            val session = authRepository.currentSession.firstOrNull()
            hasActiveSession = session != null
            val biometricAvailable = biometricHelper.isBiometricAvailable()
            val biometricEnabled = biometricHelper.isBiometricEnabled()
            
            val isRememberMe = credentialManager.isRememberMeEnabled()
            val savedPhone = credentialManager.getSavedPhone()
            val savedPassword = credentialManager.getSavedPassword()
            val hasSavedCredentials = !savedPhone.isNullOrBlank() && !savedPassword.isNullOrBlank()
            
            _uiState.update { current ->
                current.copy(
                    phoneNumber = if (isRememberMe && hasSavedCredentials) savedPhone ?: current.phoneNumber else current.phoneNumber,
                    password = if (isRememberMe && hasSavedCredentials) savedPassword ?: current.password else current.password,
                    rememberMe = isRememberMe,
                    canUseBiometric = biometricAvailable,
                    shouldAutoTriggerBiometric = hasSavedCredentials && biometricAvailable && biometricEnabled
                ) 
            }
        }
    }

    fun onRememberMeChange(value: Boolean) {
        _uiState.update { it.copy(rememberMe = value) }
        credentialManager.setRememberMeEnabled(value)
    }

    fun onPhoneNumberChange(value: String) {
        if (value.length <= 11 && value.all(Char::isDigit)) {
            _uiState.update { it.copy(phoneNumber = value, errorMessage = null, infoMessage = null) }
        }
    }

    fun setAuthFlow(flow: AuthFlow) {
        otpTimerJob?.cancel()
        _uiState.update {
            it.copy(
                authFlow = flow,
                errorMessage = null,
                infoMessage = null,
                registerStep = RegisterStep.Details,
                forgotPasswordStep = ForgotPasswordStep.Phone,
                otp = "",
                remainingOtpSeconds = RegisterOtpDurationSeconds,
                canResendOtp = false
            )
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
                    if (state.rememberMe) {
                        credentialManager.saveCredentials(state.phoneNumber, state.password, rememberMe = true)
                        if (biometricHelper.isBiometricAvailable()) {
                            biometricHelper.setBiometricEnabled(true)
                        }
                    } else {
                        credentialManager.clearCredentials()
                        biometricHelper.setBiometricEnabled(false)
                    }
                    _uiState.update { current -> current.copy(isSubmitting = false, isSignedIn = true) }
                    
                    // Run slow sync tasks in the background so they don't block navigation
                    viewModelScope.launch {
                        fcmTokenManager.syncCurrentToken()
                        deviceRepository.refreshDevices()
                        positionRepository.refreshLatestPositions()
                    }
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

    fun onBiometricClicked(triggerPrompt: () -> Unit) {
        val hasSavedCredentials = biometricHelper.getSavedPhone() != null && biometricHelper.getSavedPassword() != null
        if (!hasSavedCredentials) {
            _uiState.update { it.copy(infoMessage = "برای ورود با اثر انگشت، ابتدا یکبار با رمز عبور وارد شوید.") }
            return
        }
        if (!biometricHelper.isBiometricEnabled()) {
            _uiState.update { it.copy(infoMessage = "لطفاً ابتدا اثر انگشت را در تنظیمات برنامه فعال کنید.") }
            return
        }
        triggerPrompt()
    }

    fun onBiometricSuccess() {
        if (hasActiveSession) {
            _uiState.update { it.copy(isSignedIn = true) }
        } else {
            val phone = biometricHelper.getSavedPhone()
            val pass = biometricHelper.getSavedPassword()
            if (phone != null && pass != null) {
                viewModelScope.launch {
                    _uiState.update { it.copy(isSubmitting = true, errorMessage = null, infoMessage = null) }
                    authRepository.login(phone, pass)
                        .onSuccess {
                            _uiState.update { current -> current.copy(isSubmitting = false, isSignedIn = true) }
                            
                            viewModelScope.launch {
                                fcmTokenManager.syncCurrentToken()
                                deviceRepository.refreshDevices()
                                positionRepository.refreshLatestPositions()
                            }
                        }
                        .onFailure {
                            _uiState.update { current ->
                                current.copy(
                                    isSubmitting = false,
                                    errorMessage = "ورود با اثر انگشت با خطا مواجه شد",
                                )
                            }
                        }
                }
            } else {
                _uiState.update { it.copy(errorMessage = "اطلاعات ورود یافت نشد. لطفا یکبار با رمز عبور وارد شوید.") }
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

    fun resendOtp() {
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

    fun changePhone() {
        otpTimerJob?.cancel()
        _uiState.update {
            it.copy(
                registerStep = RegisterStep.Details,
                forgotPasswordStep = ForgotPasswordStep.Phone,
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
                        credentialManager.saveCredentials(state.phoneNumber, state.password, rememberMe = true)
                        if (biometricHelper.isBiometricAvailable()) {
                            biometricHelper.setBiometricEnabled(true)
                        }
                        _uiState.update { current ->
                            current.copy(isSubmitting = false, isSignedIn = true)
                        }
                        
                        viewModelScope.launch {
                            fcmTokenManager.syncCurrentToken()
                            deviceRepository.refreshDevices()
                            positionRepository.refreshLatestPositions()
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

    fun sendForgotPasswordOtpSubmit() {
        val state = _uiState.value
        if (!isValidIranPhoneNumber(state.phoneNumber)) {
            _uiState.update { it.copy(errorMessage = "شماره تلفن باید ۱۱ رقم باشد") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null, infoMessage = null) }
            
            // 1. Check if user exists
            val existsResult = registrationRepository.checkUserExists(state.phoneNumber)
            if (existsResult.isFailure || existsResult.getOrNull() == false) {
                _uiState.update { current ->
                    current.copy(
                        isSubmitting = false,
                        errorMessage = "کاربری با این شماره تلفن یافت نشد"
                    )
                }
                return@launch
            }

            // 2. Send OTP
            registrationRepository.sendOtp(state.phoneNumber)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            forgotPasswordStep = ForgotPasswordStep.Otp,
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

    fun verifyForgotPasswordOtpSubmit() {
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
                            forgotPasswordStep = ForgotPasswordStep.NewPassword,
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

    fun submitNewPassword() {
        val state = _uiState.value
        when {
            !state.passwordRules.isValid -> {
                _uiState.update { it.copy(errorMessage = "رمز عبور باید شرایط امنیتی را داشته باشد") }
            }
            state.password != state.confirmPassword -> {
                _uiState.update { it.copy(errorMessage = "رمز عبور و تایید آن یکسان نیست") }
            }
            else -> {
                viewModelScope.launch {
                    _uiState.update { it.copy(isSubmitting = true, errorMessage = null, infoMessage = null) }
                    registrationRepository.changePassword(
                        phoneNumber = state.phoneNumber,
                        password = state.password,
                    ).onSuccess {
                        if (credentialManager.isRememberMeEnabled()) {
                            credentialManager.saveCredentials(state.phoneNumber, state.password, rememberMe = true)
                        }
                        _uiState.update { current ->
                            current.copy(
                                isSubmitting = false,
                                authFlow = AuthFlow.Login,
                                password = if (current.rememberMe) state.password else "",
                                confirmPassword = "",
                                infoMessage = "رمز عبور با موفقیت تغییر یافت"
                            )
                        }
                    }.onFailure {
                        _uiState.update { current ->
                            current.copy(
                                isSubmitting = false,
                                errorMessage = "تغییر رمز عبور با خطا مواجه شد",
                            )
                        }
                    }
                }
            }
        }
    }
}
