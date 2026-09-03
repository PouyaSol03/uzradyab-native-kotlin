package com.example.uzradyab.presentation.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.uzradyab.R
import com.example.uzradyab.core.designsystem.AuthBackground
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.example.uzradyab.presentation.components.LocalSnackbarController
import com.example.uzradyab.presentation.startup.findActivity
import com.example.uzradyab.ui.theme.UzradyabTheme

@Composable
fun LoginRoute(
    onSignedIn: () -> Unit,
    onRegisterClick: () -> Unit,
    biometricHelper: com.example.uzradyab.core.biometric.BiometricHelper = androidx.compose.ui.platform.LocalContext.current.let { com.example.uzradyab.core.biometric.BiometricHelper(it.applicationContext) },
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarController = LocalSnackbarController.current

    LaunchedEffect(state.isSignedIn) {
        if (state.isSignedIn) {
            onSignedIn()
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarController.showError(it)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(state.infoMessage) {
        state.infoMessage?.let {
            snackbarController.showInfo(it)
            viewModel.clearMessages()
        }
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = remember(context) { context.findActivity() }

    val triggerBiometric = {
        if (activity != null) {
            biometricHelper.showBiometricPrompt(
                activity = activity,
                title = "ورود به برنامه",
                subtitle = "برای ورود اثر انگشت یا چهره خود را تایید کنید",
                negativeButtonText = "انصراف",
                onSuccess = { viewModel.onBiometricSuccess() },
                onError = { code, err -> 
                    val ignoredCodes = listOf(
                        androidx.biometric.BiometricPrompt.ERROR_USER_CANCELED,
                        androidx.biometric.BiometricPrompt.ERROR_CANCELED,
                        androidx.biometric.BiometricPrompt.ERROR_NEGATIVE_BUTTON
                    )
                    if (code !in ignoredCodes) {
                        snackbarController.showError(err.toString())
                    }
                },
                onFailed = { snackbarController.showError("تایید هویت ناموفق بود") }
            )
        }
    }

    LaunchedEffect(state.shouldAutoTriggerBiometric) {
        if (state.shouldAutoTriggerBiometric) {
            triggerBiometric()
        }
    }

    if (state.authFlow == AuthFlow.ForgotPassword) {
        ForgotPasswordScreen(
            state = state,
            onPhoneNumberChange = viewModel::onPhoneNumberChange,
            onPasswordChange = viewModel::onPasswordChange,
            onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
            onOtpChange = viewModel::onOtpChange,
            onSendOtpClick = viewModel::sendForgotPasswordOtpSubmit,
            onVerifyOtpClick = viewModel::verifyForgotPasswordOtpSubmit,
            onChangePasswordClick = viewModel::submitNewPassword,
            onResendOtpClick = viewModel::resendOtp,
            onChangePhoneClick = viewModel::changePhone,
            onBackToLoginClick = { viewModel.setAuthFlow(AuthFlow.Login) }
        )
    } else {
        LoginScreen(
            state = state,
            onPhoneNumberChange = viewModel::onPhoneNumberChange,
            onPasswordChange = viewModel::onPasswordChange,
            onRememberMeChange = viewModel::onRememberMeChange,
            onLoginClick = viewModel::login,
            onRegisterClick = onRegisterClick,
            onForgotPasswordClick = { viewModel.setAuthFlow(AuthFlow.ForgotPassword) },
            onBiometricClick = { viewModel.onBiometricClicked(triggerPrompt = triggerBiometric) },
        )
    }
}

@Composable
fun LoginScreen(
    state: AuthUiState,
    onPhoneNumberChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRememberMeChange: (Boolean) -> Unit,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onBiometricClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var passwordVisible by remember { mutableStateOf(false) }

    AuthBackground(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            AuthPanel(height = 625.dp) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(modifier = Modifier.height(101.dp))
                    AuthTitle(text = "ورود به حساب کاربری")
                    Spacer(modifier = Modifier.height(32.dp))
                    AuthTextField(
                        value = state.phoneNumber,
                        onValueChange = onPhoneNumberChange,
                        label = "شماره تلفن",
                        placeholder = "09",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        inputTextDirection = TextDirection.Ltr,
                        rightIcon = { PhoneFieldIcon() },
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    AuthTextField(
                        value = state.password,
                        onValueChange = onPasswordChange,
                        label = "رمز عبور",
                        placeholder = "رمز عبور را وارد کنید",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        inputTextDirection = TextDirection.Ltr,
                        leftIcon = { 
                            PasswordEyeIcon(
                                isVisible = passwordVisible,
                                onClick = { passwordVisible = !passwordVisible }
                            ) 
                        },
                        rightIcon = { PasswordKeyIcon() },
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.width(AuthControlWidth),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { onRememberMeChange(!state.rememberMe) }
                        ) {
                            Checkbox(
                                checked = state.rememberMe,
                                onCheckedChange = onRememberMeChange,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colorScheme.primary,
                                    uncheckedColor = UzradyabTheme.colors.textBody,
                                    checkmarkColor = Color.White
                                ),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.remember_me),
                                color = UzradyabTheme.colors.textBody,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                        AuthTextLink(
                            text = "فراموشی رمز عبور",
                            onClick = onForgotPasswordClick,
                            fontSize = 12,
                        )
                    }
                    if (state.canUseBiometric) {
                        androidx.compose.material3.IconButton(
                            onClick = onBiometricClick,
                            modifier = Modifier.size(48.dp)
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = "ورود با اثر انگشت",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    AuthPrimaryButton(
                        text = if (state.isSubmitting) "در حال ورود..." else "ورود",
                        onClick = {
                            android.util.Log.d("LoginPerformance", "Login button clicked at ${System.currentTimeMillis()}")
                            onLoginClick()
                        },
                        enabled = !state.isSubmitting,
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = "آیا حساب کاربری ندارید؟",
                        color = UzradyabTheme.colors.textBody,
                        fontSize = 14.sp,
                        lineHeight = 24.sp,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AuthTextLink(
                        text = "ایجاد حساب کاربری",
                        onClick = onRegisterClick,
                    )
                }
            }
        }
    }
}
