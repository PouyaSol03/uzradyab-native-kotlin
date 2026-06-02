package com.example.uzradyab.presentation.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
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
    onLoginClick: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    RegisterScreen(
        state = state,
        onNameChange = viewModel::onNameChange,
        onPhoneNumberChange = viewModel::onPhoneNumberChange,
        onRegisterClick = viewModel::registerVisualSubmit,
        onLoginClick = onLoginClick,
    )
}

@Composable
fun RegisterScreen(
    state: AuthUiState,
    onNameChange: (String) -> Unit,
    onPhoneNumberChange: (String) -> Unit,
    onRegisterClick: () -> Unit,
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
                    state.errorMessage?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                    state.infoMessage?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                    Spacer(
                        modifier = Modifier.height(
                            if (state.errorMessage == null && state.infoMessage == null) 48.dp else 18.dp,
                        ),
                    )
                    AuthPrimaryButton(
                        text = "دریافت کد تایید",
                        onClick = onRegisterClick,
                    )
                    Spacer(modifier = Modifier.height(24.dp))
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
            }
        }
    }
}
