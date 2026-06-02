package com.example.uzradyab.presentation.auth

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.uzradyab.core.designsystem.AuthBackground
import com.example.uzradyab.ui.theme.AppTextBody

@Composable
fun RegisterRoute(
    onSignedIn: () -> Unit,
    onLoginClick: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.isSignedIn) {
        if (state.isSignedIn) {
            onSignedIn()
        }
    }

    RegisterScreen(
        state = state,
        onNameChange = viewModel::onNameChange,
        onPhoneNumberChange = viewModel::onPhoneNumberChange,
        onRequestOtpClick = viewModel::registerVisualSubmit,
        onOtpChange = viewModel::onOtpChange,
        onVerifyOtpClick = viewModel::verifyRegisterOtp,
        onResendOtpClick = viewModel::resendRegisterOtp,
        onChangePhoneClick = viewModel::changeRegisterPhone,
        onPasswordChange = viewModel::onPasswordChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onCompleteRegistrationClick = viewModel::completeRegistration,
        onLoginClick = onLoginClick,
    )
}

@Composable
fun RegisterScreen(
    state: AuthUiState,
    onNameChange: (String) -> Unit,
    onPhoneNumberChange: (String) -> Unit,
    onRequestOtpClick: () -> Unit,
    onOtpChange: (String) -> Unit,
    onVerifyOtpClick: () -> Unit,
    onResendOtpClick: () -> Unit,
    onChangePhoneClick: () -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onCompleteRegistrationClick: () -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AuthBackground(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            AuthPanel(height = 643.dp) {
                when (state.registerStep) {
                    RegisterStep.Details -> RegisterDetailsStep(
                        state = state,
                        onNameChange = onNameChange,
                        onPhoneNumberChange = onPhoneNumberChange,
                        onRequestOtpClick = onRequestOtpClick,
                        onLoginClick = onLoginClick,
                    )

                    RegisterStep.Otp -> RegisterOtpStep(
                        state = state,
                        onOtpChange = onOtpChange,
                        onVerifyOtpClick = onVerifyOtpClick,
                        onResendOtpClick = onResendOtpClick,
                        onChangePhoneClick = onChangePhoneClick,
                        onLoginClick = onLoginClick,
                    )

                    RegisterStep.Password -> RegisterPasswordStep(
                        state = state,
                        onPasswordChange = onPasswordChange,
                        onConfirmPasswordChange = onConfirmPasswordChange,
                        onCompleteRegistrationClick = onCompleteRegistrationClick,
                        onLoginClick = onLoginClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun RegisterDetailsStep(
    state: AuthUiState,
    onNameChange: (String) -> Unit,
    onPhoneNumberChange: (String) -> Unit,
    onRequestOtpClick: () -> Unit,
    onLoginClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(101.dp))
        AuthTitle(text = "ایجاد حساب کاربری جدید")
        Spacer(modifier = Modifier.height(32.dp))
        AuthTextField(
            value = state.name,
            onValueChange = onNameChange,
            label = "نام و نام خانوادگی",
            placeholder = "نام و نام خانوادگی را وارد کنید",
            rightIcon = { UserFieldIcon() },
        )
        Spacer(modifier = Modifier.height(16.dp))
        AuthTextField(
            value = state.phoneNumber,
            onValueChange = onPhoneNumberChange,
            label = "شماره تلفن",
            placeholder = "09",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            inputTextDirection = TextDirection.Ltr,
            rightIcon = { PhoneFieldIcon() },
        )
        Spacer(modifier = Modifier.height(16.dp))
        AuthLanguageField()
        RegisterMessage(state = state)
        Spacer(
            modifier = Modifier.height(
                if (state.errorMessage == null && state.infoMessage == null) 48.dp else 18.dp,
            ),
        )
        AuthPrimaryButton(
            text = if (state.isSubmitting) "در حال ارسال..." else "دریافت کد تایید",
            onClick = onRequestOtpClick,
            enabled = !state.isSubmitting,
        )
        Spacer(modifier = Modifier.height(24.dp))
        RegisterFooter(onLoginClick = onLoginClick)
    }
}

@Composable
private fun RegisterOtpStep(
    state: AuthUiState,
    onOtpChange: (String) -> Unit,
    onVerifyOtpClick: () -> Unit,
    onResendOtpClick: () -> Unit,
    onChangePhoneClick: () -> Unit,
    onLoginClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(101.dp))
        AuthTitle(text = "ایجاد حساب کاربری جدید")
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "کد تایید ارسال شده به شماره ${maskPhoneNumber(state.phoneNumber)} را وارد کنید.",
            color = AppTextBody,
            fontSize = 14.sp,
            lineHeight = 22.sp,
            textAlign = TextAlign.Right,
            modifier = Modifier.width(AuthControlWidth),
        )
        Spacer(modifier = Modifier.height(16.dp))
        AuthTextField(
            value = state.otp,
            onValueChange = onOtpChange,
            label = "کد تایید",
            placeholder = "x x x x x x",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            inputTextDirection = TextDirection.Ltr,
            rightIcon = { UserFieldIcon() },
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.width(AuthControlWidth),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AuthTextLink(
                text = "تغییر شماره تلفن",
                onClick = onChangePhoneClick,
                fontSize = 12,
            )
            if (state.canResendOtp) {
                AuthTextLink(
                    text = "ارسال مجدد",
                    onClick = onResendOtpClick,
                    fontSize = 12,
                )
            } else {
                Text(
                    text = formatOtpRemaining(state.remainingOtpSeconds),
                    color = AppTextBody,
                    fontSize = 12.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
        RegisterMessage(state = state)
        Spacer(modifier = Modifier.height(if (state.errorMessage == null) 32.dp else 16.dp))
        AuthPrimaryButton(
            text = if (state.isSubmitting) "در حال تایید..." else "تایید",
            onClick = onVerifyOtpClick,
            enabled = !state.isSubmitting,
        )
        Spacer(modifier = Modifier.height(32.dp))
        RegisterFooter(onLoginClick = onLoginClick)
    }
}

@Composable
private fun RegisterPasswordStep(
    state: AuthUiState,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onCompleteRegistrationClick: () -> Unit,
    onLoginClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(101.dp))
        AuthTitle(text = "ایجاد حساب کاربری جدید")
        Spacer(modifier = Modifier.height(40.dp))
        AuthTextField(
            value = state.password,
            onValueChange = onPasswordChange,
            label = "رمز عبور",
            placeholder = "رمز عبور را وارد کنید",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
            inputTextDirection = TextDirection.Ltr,
            leftIcon = { PasswordEyeIcon() },
            rightIcon = { PasswordKeyIcon() },
        )
        Spacer(modifier = Modifier.height(8.dp))
        PasswordRules(rules = state.passwordRules)
        Spacer(modifier = Modifier.height(16.dp))
        AuthTextField(
            value = state.confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label = "تایید رمز عبور",
            placeholder = "رمز عبور را دوباره وارد کنید",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
            inputTextDirection = TextDirection.Ltr,
            leftIcon = { PasswordEyeIcon() },
            rightIcon = { PasswordKeyIcon() },
        )
        RegisterMessage(state = state)
        Spacer(modifier = Modifier.height(if (state.errorMessage == null) 48.dp else 18.dp))
        AuthPrimaryButton(
            text = if (state.isSubmitting) "در حال تکمیل..." else "تکمیل عضویت",
            onClick = onCompleteRegistrationClick,
            enabled = !state.isSubmitting,
        )
        Spacer(modifier = Modifier.height(24.dp))
        RegisterFooter(onLoginClick = onLoginClick)
    }
}

@Composable
private fun PasswordRules(rules: PasswordRuleState) {
    Column(
        modifier = Modifier.width(AuthControlWidth),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.End,
    ) {
        PasswordRule(text = "حداقل 8 کاراکتر", isMet = rules.hasMinimumLength)
        PasswordRule(text = "شامل حداقل 1 عدد", isMet = rules.hasDigit)
        PasswordRule(text = "شامل حداقل 1 کاراکتر خاص", isMet = rules.hasSpecialCharacter)
    }
}

@Composable
private fun PasswordRule(text: String, isMet: Boolean) {
    val color = if (isMet) MaterialTheme.colorScheme.primary else AppTextBody

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 10.sp,
            lineHeight = 18.sp,
            textAlign = TextAlign.Right,
        )
        Spacer(modifier = Modifier.width(6.dp))
        Canvas(modifier = Modifier.size(10.dp)) {
            drawCircle(
                color = color,
                radius = 4.dp.toPx(),
                style = if (isMet) Fill else Stroke(width = 1.dp.toPx()),
            )
        }
    }
}

@Composable
private fun RegisterMessage(state: AuthUiState) {
    state.errorMessage?.let {
        Text(
            text = it,
            color = MaterialTheme.colorScheme.error,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            textAlign = TextAlign.Right,
            modifier = Modifier
                .width(AuthControlWidth)
                .padding(top = 8.dp),
        )
    }
    state.infoMessage?.let {
        Text(
            text = it,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            textAlign = TextAlign.Right,
            modifier = Modifier
                .width(AuthControlWidth)
                .padding(top = 8.dp),
        )
    }
}

@Composable
private fun RegisterFooter(onLoginClick: () -> Unit) {
    Text(
        text = "در حال حاضر حساب کاربری دارید؟",
        color = AppTextBody,
        fontSize = 14.sp,
        lineHeight = 24.sp,
        textAlign = TextAlign.Center,
    )
    AuthTextLink(
        text = "ورود",
        onClick = onLoginClick,
    )
}

private fun formatOtpRemaining(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "$minutes:${remainingSeconds.toString().padStart(2, '0')}"
}
