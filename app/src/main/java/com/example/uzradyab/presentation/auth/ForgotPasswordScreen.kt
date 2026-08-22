package com.example.uzradyab.presentation.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uzradyab.core.designsystem.AuthBackground
import com.example.uzradyab.ui.theme.UzradyabTheme
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status

@Composable
fun ForgotPasswordScreen(
    state: AuthUiState,
    onPhoneNumberChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onOtpChange: (String) -> Unit,
    onSendOtpClick: () -> Unit,
    onVerifyOtpClick: () -> Unit,
    onChangePasswordClick: () -> Unit,
    onResendOtpClick: () -> Unit,
    onChangePhoneClick: () -> Unit,
    onBackToLoginClick: () -> Unit,
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
                    AuthTitle(text = "فراموشی رمز عبور")
                    Spacer(modifier = Modifier.height(16.dp))

                    when (state.forgotPasswordStep) {
                        ForgotPasswordStep.Phone -> {
                            Text(
                                text = "لطفاً شماره تلفن خود را وارد کنید",
                                color = UzradyabTheme.colors.textBody,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
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
                            Spacer(modifier = Modifier.height(32.dp))
                            AuthPrimaryButton(
                                text = if (state.isSubmitting) "در حال بررسی..." else "دریافت کد تایید",
                                onClick = onSendOtpClick,
                                enabled = !state.isSubmitting,
                            )
                        }
                        ForgotPasswordStep.Otp -> {
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

                            Text(
                                text = "کد تایید به شماره ${state.phoneNumber} پیامک شد",
                                color = UzradyabTheme.colors.textBody,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OtpInputField(
                                value = state.otp,
                                onValueChange = onOtpChange,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            AuthPrimaryButton(
                                text = if (state.isSubmitting) "در حال بررسی..." else "تایید کد",
                                onClick = onVerifyOtpClick,
                                enabled = !state.isSubmitting,
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            ResendOtpRow(
                                remainingSeconds = state.remainingOtpSeconds,
                                canResend = state.canResendOtp,
                                onResendClick = onResendOtpClick,
                                onChangePhoneClick = onChangePhoneClick
                            )
                        }
                        ForgotPasswordStep.NewPassword -> {
                            Text(
                                text = "لطفاً رمز عبور جدید خود را وارد کنید",
                                color = UzradyabTheme.colors.textBody,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            AuthTextField(
                                value = state.password,
                                onValueChange = onPasswordChange,
                                label = "رمز عبور جدید",
                                placeholder = "رمز عبور جدید را وارد کنید",
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
                            PasswordRules(state.passwordRules)
                            Spacer(modifier = Modifier.height(16.dp))
                            AuthTextField(
                                value = state.confirmPassword,
                                onValueChange = onConfirmPasswordChange,
                                label = "تایید رمز عبور جدید",
                                placeholder = "تایید رمز عبور جدید را وارد کنید",
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
                            Spacer(modifier = Modifier.height(32.dp))
                            AuthPrimaryButton(
                                text = if (state.isSubmitting) "در حال تغییر..." else "تغییر رمز عبور",
                                onClick = onChangePasswordClick,
                                enabled = !state.isSubmitting,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    AuthTextLink(
                        text = "بازگشت به صفحه ورود",
                        onClick = onBackToLoginClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun ResendOtpRow(
    remainingSeconds: Int,
    canResend: Boolean,
    onResendClick: () -> Unit,
    onChangePhoneClick: () -> Unit
) {
    Row(
        modifier = Modifier.width(AuthControlWidth),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AuthTextLink(
            text = androidx.compose.ui.res.stringResource(com.example.uzradyab.R.string.str_c20b7c3b),
            onClick = onChangePhoneClick,
            fontSize = 12,
        )
        if (canResend) {
            AuthTextLink(
                text = androidx.compose.ui.res.stringResource(com.example.uzradyab.R.string.str_665fa1fc),
                onClick = onResendClick,
                fontSize = 12,
            )
        } else {
            Text(
                text = formatOtpRemaining(remainingSeconds),
                color = UzradyabTheme.colors.textBody,
                fontSize = 12.sp,
                lineHeight = 24.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
            )
        }
    }
}
