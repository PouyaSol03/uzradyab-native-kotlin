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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.uzradyab.R
import com.example.uzradyab.core.designsystem.AuthBackground
import com.example.uzradyab.ui.theme.AppTextBody

@Composable
fun LoginRoute(
    onSignedIn: () -> Unit,
    onRegisterClick: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.isSignedIn) {
        if (state.isSignedIn) {
            onSignedIn()
        }
    }

    LoginScreen(
        state = state,
        onPhoneNumberChange = viewModel::onPhoneNumberChange,
        onPasswordChange = viewModel::onPasswordChange,
        onLoginClick = viewModel::login,
        onRegisterClick = onRegisterClick,
    )
}

@Composable
fun LoginScreen(
    state: AuthUiState,
    onPhoneNumberChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
                        visualTransformation = PasswordVisualTransformation(),
                        inputTextDirection = TextDirection.Ltr,
                        leftIcon = { PasswordEyeIcon() },
                        rightIcon = { PasswordKeyIcon() },
                    )
                    Spacer(modifier = Modifier.height(13.dp))
                    AuthTextLink(
                        text = "فراموشی رمز عبور",
                        onClick = {},
                        modifier = Modifier.align(Alignment.Start),
                        fontSize = 12,
                    )
                    state.errorMessage?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            textAlign = TextAlign.Right,
                            modifier = Modifier
                                .padding(top = 4.dp),
                        )
                    }
                    Spacer(modifier = Modifier.height(if (state.errorMessage == null) 48.dp else 18.dp))
                    AuthPrimaryButton(
                        text = if (state.isSubmitting) "در حال ورود..." else "ورود",
                        onClick = onLoginClick,
                        enabled = !state.isSubmitting,
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = "آیا حساب کاربری ندارید؟",
                        color = AppTextBody,
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
