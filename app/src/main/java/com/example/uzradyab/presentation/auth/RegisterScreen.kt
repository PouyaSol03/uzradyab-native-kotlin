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
import com.example.uzradyab.ui.theme.AppTextPrimary

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
        onPasswordChange = viewModel::onPasswordChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onRegisterClick = viewModel::registerVisualSubmit,
        onLoginClick = onLoginClick,
    )
}

@Composable
fun RegisterScreen(
    state: AuthUiState,
    onNameChange: (String) -> Unit,
    onPhoneNumberChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
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
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 372.dp)
                    .height(665.dp),
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
                    Spacer(modifier = Modifier.height(56.dp))
                    Text(
                        text = "ایجاد حساب کاربری جدید",
                        color = AppTextPrimary,
                        fontSize = 20.sp,
                        lineHeight = 35.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(28.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                        UzradyabInput(
                            value = state.name,
                            onValueChange = onNameChange,
                            label = "نام و نام خانوادگی",
                            placeholder = "نام و نام خانوادگی را وارد کنید",
                        )
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
                            placeholder = "رمز عبور خود را وارد کنید",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            visualTransformation = PasswordVisualTransformation(),
                            inputTextDirection = TextDirection.Ltr,
                        )
                        UzradyabInput(
                            value = state.confirmPassword,
                            onValueChange = onConfirmPasswordChange,
                            label = "تایید رمز عبور",
                            placeholder = "رمز عبور را مجددا وارد کنید",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            visualTransformation = PasswordVisualTransformation(),
                            inputTextDirection = TextDirection.Ltr,
                        )
                    }
                    state.infoMessage?.let {
                        Text(
                            text = it,
                            color = if (it.contains("OTP")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            textAlign = TextAlign.Right,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                        )
                    }
                    Spacer(modifier = Modifier.height(if (state.infoMessage == null) 32.dp else 18.dp))
                    UzradyabPrimaryButton(
                        text = "دریافت کد تایید",
                        onClick = onRegisterClick,
                    )
                    Spacer(modifier = Modifier.height(22.dp))
                    Text(
                        text = "در حال حاضر حساب کاربری دارید؟",
                        color = AppTextPrimary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                    )
                    UzradyabTextAction(
                        text = "ورود",
                        onClick = onLoginClick,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
        }
    }
}
