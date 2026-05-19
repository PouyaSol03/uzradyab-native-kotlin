package com.example.uzradyab.feature.auth.signin

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.uzradyab.core.designsystem.AppLabeledTextField
import com.example.uzradyab.core.designsystem.AppPrimaryButton
import com.example.uzradyab.core.designsystem.AppTextAction
import com.example.uzradyab.core.designsystem.AuthBackground
import com.example.uzradyab.core.designsystem.EyeOffIcon
import com.example.uzradyab.core.designsystem.KeyIcon
import com.example.uzradyab.core.designsystem.PhoneIcon
import com.example.uzradyab.ui.theme.AppTextBody
import com.example.uzradyab.ui.theme.AppTextMuted
import com.example.uzradyab.ui.theme.AppTextPrimary
import com.example.uzradyab.ui.theme.UzradyabTheme

@Composable
fun SignInRoute(
    modifier: Modifier = Modifier,
    onSignedIn: () -> Unit = {},
    onForgotPassword: () -> Unit = {},
    onCreateAccount: () -> Unit = {},
    viewModel: SignInViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.signedIn) {
        if (state.signedIn) {
            onSignedIn()
        }
    }

    SignInScreen(
        state = state,
        onPhoneNumberChange = viewModel::onPhoneNumberChange,
        onPasswordChange = viewModel::onPasswordChange,
        onPasswordVisibilityChange = viewModel::onPasswordVisibilityChange,
        onSignInClick = viewModel::signIn,
        onForgotPasswordClick = onForgotPassword,
        onCreateAccountClick = onCreateAccount,
        modifier = modifier
    )
}

@Composable
fun SignInScreen(
    state: SignInUiState,
    onPhoneNumberChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityChange: () -> Unit,
    onSignInClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onCreateAccountClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AuthBackground(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 327.dp)
                    .height(625.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    SignInCardDecoration(modifier = Modifier.matchParentSize())
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(99.dp))
                        Text(
                            text = SignInCopy.title,
                            color = AppTextPrimary,
                            fontSize = 20.sp,
                            lineHeight = 35.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(30.dp)) {
                            AppLabeledTextField(
                                value = state.phoneNumber,
                                onValueChange = onPhoneNumberChange,
                                label = SignInCopy.phoneLabel,
                                placeholder = "09",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                inputTextDirection = TextDirection.Ltr,
                                trailingIcon = { PhoneIcon(color = AppTextMuted) }
                            )
                            AppLabeledTextField(
                                value = state.password,
                                onValueChange = onPasswordChange,
                                label = SignInCopy.passwordLabel,
                                placeholder = SignInCopy.passwordPlaceholder,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                visualTransformation = if (state.isPasswordVisible) {
                                    VisualTransformation.None
                                } else {
                                    PasswordVisualTransformation()
                                },
                                leadingIcon = {
                                    androidx.compose.material3.IconButton(onClick = onPasswordVisibilityChange) {
                                        EyeOffIcon(color = MaterialTheme.colorScheme.primary)
                                    }
                                },
                                trailingIcon = { KeyIcon(color = AppTextMuted) }
                            )
                        }
                        AppTextAction(
                            text = SignInCopy.forgotPassword,
                            onClick = onForgotPasswordClick,
                            modifier = Modifier
                                .align(Alignment.Start)
                                .padding(top = 18.dp)
                        )
                        if (state.errorMessage != null) {
                            Text(
                                text = state.errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                textAlign = TextAlign.Right,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(if (state.errorMessage == null) 72.dp else 34.dp))
                        AppPrimaryButton(
                            text = when {
                                state.isCheckingSession -> SignInCopy.checkingSession
                                state.isSubmitting -> SignInCopy.submitting
                                else -> SignInCopy.signIn
                            },
                            onClick = onSignInClick,
                            enabled = !state.isSubmitting && !state.isCheckingSession
                        )
                        Spacer(modifier = Modifier.height(54.dp))
                        Text(
                            text = SignInCopy.noAccount,
                            color = AppTextBody,
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            textAlign = TextAlign.Center
                        )
                        AppTextAction(
                            text = SignInCopy.createAccount,
                            onClick = onCreateAccountClick,
                            modifier = Modifier.padding(top = 11.dp)
                        )
                    }
                }
            }
        }
    }
}

private object SignInCopy {
    const val title = "\u0648\u0631\u0648\u062F \u0628\u0647 \u062D\u0633\u0627\u0628 \u06A9\u0627\u0631\u0628\u0631\u06CC"
    const val phoneLabel = "\u0634\u0645\u0627\u0631\u0647 \u062A\u0644\u0641\u0646"
    const val passwordLabel = "\u0631\u0645\u0632 \u0639\u0628\u0648\u0631"
    const val passwordPlaceholder = "\u0631\u0645\u0632 \u0639\u0628\u0648\u0631 \u0631\u0627 \u0648\u0627\u0631\u062F \u06A9\u0646\u06CC\u062F"
    const val forgotPassword = "\u0641\u0631\u0627\u0645\u0648\u0634\u06CC \u0631\u0645\u0632 \u0639\u0628\u0648\u0631"
    const val signIn = "\u0648\u0631\u0648\u062F"
    const val submitting = "\u062F\u0631 \u062D\u0627\u0644 \u0648\u0631\u0648\u062F..."
    const val checkingSession = "\u0628\u0631\u0631\u0633\u06CC \u0646\u0634\u0633\u062A..."
    const val noAccount = "\u0622\u06CC\u0627 \u062D\u0633\u0627\u0628 \u06A9\u0627\u0631\u0628\u0631\u06CC \u0646\u062F\u0627\u0631\u06CC\u062F\u061F"
    const val createAccount = "\u0627\u06CC\u062C\u0627\u062F \u062D\u0633\u0627\u0628 \u06A9\u0627\u0631\u0628\u0631\u06CC"
}

@Composable
private fun SignInCardDecoration(modifier: Modifier = Modifier) {
    val decorationColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)

    Canvas(modifier = modifier) {
        drawPath(
            path = Path().apply {
                moveTo(0f, 78.dp.toPx())
                cubicTo(34.dp.toPx(), 78.dp.toPx(), 20.dp.toPx(), 22.dp.toPx(), 66.dp.toPx(), 26.dp.toPx())
                cubicTo(100.dp.toPx(), 28.dp.toPx(), 76.dp.toPx(), 94.dp.toPx(), 124.dp.toPx(), 90.dp.toPx())
                cubicTo(160.dp.toPx(), 88.dp.toPx(), 138.dp.toPx(), 28.dp.toPx(), 176.dp.toPx(), 26.dp.toPx())
                cubicTo(212.dp.toPx(), 24.dp.toPx(), 190.dp.toPx(), 88.dp.toPx(), 236.dp.toPx(), 86.dp.toPx())
                lineTo(size.width - 48.dp.toPx(), 86.dp.toPx())
                quadraticBezierTo(size.width - 26.dp.toPx(), 86.dp.toPx(), size.width - 26.dp.toPx(), 64.dp.toPx())
            },
            color = decorationColor,
            style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Preview(showBackground = true, widthDp = 375, heightDp = 812)
@Composable
private fun SignInScreenPreview() {
    UzradyabTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            SignInScreen(
                state = SignInUiState(),
                onPhoneNumberChange = {},
                onPasswordChange = {},
                onPasswordVisibilityChange = {},
                onSignInClick = {},
                onForgotPasswordClick = {},
                onCreateAccountClick = {}
            )
        }
    }
}
