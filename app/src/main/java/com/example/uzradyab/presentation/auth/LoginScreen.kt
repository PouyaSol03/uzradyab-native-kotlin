package com.example.uzradyab.presentation.auth

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.example.uzradyab.presentation.common.UzradyabInput
import com.example.uzradyab.presentation.common.UzradyabPrimaryButton
import com.example.uzradyab.presentation.common.UzradyabTextAction
import com.example.uzradyab.ui.theme.AppTextBody
import com.example.uzradyab.ui.theme.AppTextPrimary

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
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 372.dp)
                    .height(625.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(modifier = Modifier.height(96.dp))
                    Text(
                        text = "ورود به حساب کاربری",
                        color = AppTextPrimary,
                        fontSize = 20.sp,
                        lineHeight = 35.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(28.dp)) {
                        UzradyabInput(
                            value = state.phoneNumber,
                            onValueChange = onPhoneNumberChange,
                            label = "شماره تلفن",
                            placeholder = "09",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            inputTextDirection = TextDirection.Ltr,
                        )
                        UzradyabInput(
                            value = state.password,
                            onValueChange = onPasswordChange,
                            label = "رمز عبور",
                            placeholder = "رمز عبور را وارد کنید",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            visualTransformation = PasswordVisualTransformation(),
                            inputTextDirection = TextDirection.Ltr,
                        )
                    }
                    UzradyabTextAction(
                        text = "فراموشی رمز عبور",
                        onClick = {},
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(top = 18.dp),
                    )
                    state.errorMessage?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            textAlign = TextAlign.Right,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                        )
                    }
                    Spacer(modifier = Modifier.height(if (state.errorMessage == null) 70.dp else 34.dp))
                    UzradyabPrimaryButton(
                        text = if (state.isSubmitting) "در حال ورود..." else "ورود",
                        onClick = onLoginClick,
                        enabled = !state.isSubmitting,
                    )
                    Spacer(modifier = Modifier.height(52.dp))
                    Text(
                        text = "آیا حساب کاربری ندارید؟",
                        color = AppTextBody,
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        textAlign = TextAlign.Center,
                    )
                    UzradyabTextAction(
                        text = "ایجاد حساب کاربری",
                        onClick = onRegisterClick,
                        modifier = Modifier.padding(top = 11.dp),
                    )
                }
            }
        }
    }
}
