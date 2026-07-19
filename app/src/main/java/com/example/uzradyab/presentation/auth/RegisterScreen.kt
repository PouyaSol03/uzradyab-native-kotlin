package com.example.uzradyab.presentation.auth

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.uzradyab.core.designsystem.AuthBackground

import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import com.example.uzradyab.presentation.components.LocalSnackbarController
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import com.example.uzradyab.R
import androidx.compose.ui.res.stringResource
import com.example.uzradyab.ui.theme.UzradyabTheme
import com.example.uzradyab.ui.theme.themedColor

@Composable
fun RegisterRoute(
    onSignedIn: () -> Unit,
    onLoginClick: () -> Unit,
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
        onPrivacyPolicyAcceptChange = viewModel::onPrivacyPolicyAcceptChange,
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
    onPrivacyPolicyAcceptChange: (Boolean) -> Unit,
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
                        onPrivacyPolicyAcceptChange = onPrivacyPolicyAcceptChange,
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
        AuthTitle(text = stringResource(R.string.str_f53c8810))
        Spacer(modifier = Modifier.height(32.dp))
        AuthTextField(
            value = state.name,
            onValueChange = onNameChange,
            label = stringResource(R.string.str_4304506f),
            placeholder = "نام و نام خانوادگی را وارد کنید",
            rightIcon = { UserFieldIcon() },
        )
        Spacer(modifier = Modifier.height(16.dp))
        AuthTextField(
            value = state.phoneNumber,
            onValueChange = onPhoneNumberChange,
            label = stringResource(R.string.str_c4cd95ab),
            placeholder = "09",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            inputTextDirection = TextDirection.Ltr,
            rightIcon = { PhoneFieldIcon() },
        )
        Spacer(modifier = Modifier.height(16.dp))
        AuthLanguageField()
        Spacer(modifier = Modifier.height(48.dp))
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
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val message = result.data?.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)
            message?.let {
                val otpPattern = "\\d{6}".toRegex()
                val match = otpPattern.find(it)
                if (match != null) {
                    onOtpChange(match.value)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        val client = SmsRetriever.getClient(context)
        client.startSmsUserConsent(null) // Listen for any sender
    }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (SmsRetriever.SMS_RETRIEVED_ACTION == intent.action) {
                    val extras = intent.extras
                    val status = extras?.get(SmsRetriever.EXTRA_STATUS) as? Status
                    if (status?.statusCode == CommonStatusCodes.SUCCESS) {
                        val consentIntent = extras.getParcelable<Intent>(SmsRetriever.EXTRA_CONSENT_INTENT)
                        consentIntent?.let {
                            launcher.launch(it)
                        }
                    }
                }
            }
        }
        val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
        ContextCompat.registerReceiver(
            context,
            receiver,
            intentFilter,
            ContextCompat.RECEIVER_EXPORTED
        )

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(101.dp))
        AuthTitle(text = stringResource(R.string.str_f53c8810))
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "کد تایید ارسال شده به شماره ${maskPhoneNumber(state.phoneNumber)} را وارد کنید.",
            color = UzradyabTheme.colors.textBody,
            fontSize = 14.sp,
            lineHeight = 22.sp,
            textAlign = TextAlign.Right,
            modifier = Modifier.width(AuthControlWidth),
        )
        Spacer(modifier = Modifier.height(16.dp))
        AuthTextField(
            value = state.otp,
            onValueChange = onOtpChange,
            label = stringResource(R.string.str_f5b1f6c0),
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
                text = stringResource(R.string.str_c20b7c3b),
                onClick = onChangePhoneClick,
                fontSize = 12,
            )
            if (state.canResendOtp) {
                AuthTextLink(
                    text = stringResource(R.string.str_665fa1fc),
                    onClick = onResendOtpClick,
                    fontSize = 12,
                )
            } else {
                Text(
                    text = formatOtpRemaining(state.remainingOtpSeconds),
                    color = UzradyabTheme.colors.textBody,
                    fontSize = 12.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
        Spacer(modifier = Modifier.height(48.dp))
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
@OptIn(ExperimentalMaterial3Api::class)
private fun RegisterPasswordStep(
    state: AuthUiState,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onPrivacyPolicyAcceptChange: (Boolean) -> Unit,
    onCompleteRegistrationClick: () -> Unit,
    onLoginClick: () -> Unit,
) {
    var showPrivacyPolicy by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var passwordVisible by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var confirmPasswordVisible by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(101.dp))
        AuthTitle(text = stringResource(R.string.str_f53c8810))
        Spacer(modifier = Modifier.height(40.dp))
        AuthTextField(
            value = state.password,
            onValueChange = onPasswordChange,
            label = stringResource(R.string.str_6814380a),
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
        PasswordRules(rules = state.passwordRules)
        Spacer(modifier = Modifier.height(16.dp))
        AuthTextField(
            value = state.confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label = stringResource(R.string.str_9cc24ffb),
            placeholder = "رمز عبور را دوباره وارد کنید",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            inputTextDirection = TextDirection.Ltr,
            leftIcon = { 
                PasswordEyeIcon(
                    isVisible = confirmPasswordVisible,
                    onClick = { confirmPasswordVisible = !confirmPasswordVisible }
                ) 
            },
            rightIcon = { PasswordKeyIcon() },
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.width(AuthControlWidth),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            androidx.compose.material3.Checkbox(
                checked = state.isPrivacyPolicyAccepted,
                onCheckedChange = onPrivacyPolicyAcceptChange,
                colors = androidx.compose.material3.CheckboxDefaults.colors(
                    checkedColor = UzradyabTheme.colors.primary,
                    uncheckedColor = UzradyabTheme.colors.textBody
                )
            )
            Spacer(modifier = Modifier.width(4.dp))
            Row(
                modifier = Modifier.clickable { showPrivacyPolicy = true },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "با ",
                    color = UzradyabTheme.colors.textPrimary,
                    fontSize = 13.sp,
                )
                Text(
                    text = stringResource(R.string.str_e59048e1),
                    color = UzradyabTheme.colors.primary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                )
                Text(
                    text = " موافقم",
                    color = UzradyabTheme.colors.textPrimary,
                    fontSize = 13.sp,
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        AuthPrimaryButton(
            text = if (state.isSubmitting) "در حال تکمیل..." else "تکمیل عضویت",
            onClick = onCompleteRegistrationClick,
            enabled = !state.isSubmitting,
        )
        Spacer(modifier = Modifier.height(24.dp))
        RegisterFooter(onLoginClick = onLoginClick)
    }

    if (showPrivacyPolicy) {
        val sheetState = androidx.compose.material3.rememberModalBottomSheetState()
        androidx.compose.material3.ModalBottomSheet(
            onDismissRequest = { showPrivacyPolicy = false },
            sheetState = sheetState,
            containerColor = themedColor(light = Color.White, dark = Color(0xFF27343F)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.str_e59048e1),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = UzradyabTheme.colors.textPrimary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.str_46a0f4a8),
                    fontSize = 14.sp,
                    lineHeight = 24.sp,
                    color = UzradyabTheme.colors.textBody,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(32.dp))
                AuthPrimaryButton(
                    text = stringResource(R.string.str_ed52d39d),
                    onClick = { showPrivacyPolicy = false }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun PasswordRules(rules: PasswordRuleState) {
    Column(
        modifier = Modifier.width(AuthControlWidth),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        PasswordRule(text = stringResource(R.string.str_62ad2446), isMet = rules.hasMinimumLength)
        PasswordRule(text = stringResource(R.string.str_7037fc5a), isMet = rules.hasDigit)
        PasswordRule(text = stringResource(R.string.str_7be2abc0), isMet = rules.hasSpecialCharacter)
    }
}

@Composable
private fun PasswordRule(text: String, isMet: Boolean) {
    val color = if (isMet) MaterialTheme.colorScheme.primary else UzradyabTheme.colors.textBody

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Canvas(modifier = Modifier.size(10.dp)) {
            drawCircle(

                color = color,
                radius = 4.dp.toPx(),
                style = if (isMet) Fill else Stroke(width = 1.dp.toPx()),
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            color = color,
            fontSize = 10.sp,
            lineHeight = 18.sp,
            textAlign = TextAlign.Right,
        )
    }
}

@Composable
private fun RegisterFooter(onLoginClick: () -> Unit) {
    Text(
        text = stringResource(R.string.str_aaa682a4),
        color = UzradyabTheme.colors.textBody,
        fontSize = 14.sp,
        lineHeight = 24.sp,
        textAlign = TextAlign.Center,
    )
    AuthTextLink(
        text = stringResource(R.string.str_32a81e55),
        onClick = onLoginClick,
    )
}

private fun formatOtpRemaining(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "$minutes:${remainingSeconds.toString().padStart(2, '0')}"
}
