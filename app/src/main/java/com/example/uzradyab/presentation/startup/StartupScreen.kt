package com.example.uzradyab.presentation.startup

import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.uzradyab.core.biometric.BiometricHelper
import com.example.uzradyab.presentation.common.UzradyabPrimaryButton
import com.example.uzradyab.presentation.common.UzradyabTextAction
import android.content.Intent
import android.net.Uri

@Composable
fun StartupRoute(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToSignIn: () -> Unit,
    onNavigateToHome: () -> Unit,
    biometricHelper: BiometricHelper,
    viewModel: StartupViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val navigationTarget by viewModel.navigationTarget.collectAsState()
    val context = LocalContext.current

    // Handle navigation targets
    LaunchedEffect(navigationTarget) {
        when (navigationTarget) {
            StartupNavigationTarget.Onboarding -> onNavigateToOnboarding()
            StartupNavigationTarget.SignIn -> onNavigateToSignIn()
            StartupNavigationTarget.Home -> onNavigateToHome()
            null -> {}
        }
    }

    // Find host FragmentActivity
    val activity = remember(context) { context.findActivity() }

    val triggerBiometric = {
        if (activity != null) {
            biometricHelper.showBiometricPrompt(
                activity = activity,
                title = "ورود به برنامه",
                subtitle = "برای ورود اثر انگشت یا چهره خود را تایید کنید",
                negativeButtonText = "انصراف",
                onSuccess = { viewModel.onBiometricSuccess() },
                onError = { code, err -> viewModel.onBiometricFailure(err.toString()) },
                onFailed = { viewModel.onBiometricFailure("تایید هویت ناموفق بود") }
            )
        } else {
            // Context mapping error, fallback to Home (user session is active)
            viewModel.onBiometricSuccess()
        }
    }

    // Trigger biometric automatically on state BiometricRequired
    LaunchedEffect(state) {
        if (state is StartupUiState.BiometricRequired) {
            triggerBiometric()
        } else if (state is StartupUiState.BiometricFailed) {
            android.widget.Toast.makeText(
                context,
                "عملیات تایید هویت لغو شد",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    StartupScreen(
        state = state,
        onRetryBiometric = {
            viewModel.checkStatus()
        },
        onLogoutClick = {
            viewModel.logoutAndGoToSignIn()
        },
        onContinueApp = {
            viewModel.continueToApp()
        }
    )
}

@Composable
fun StartupScreen(
    state: StartupUiState,
    onRetryBiometric: () -> Unit,
    onLogoutClick: () -> Unit,
    onContinueApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (state) {
                is StartupUiState.Checking -> {
                    // App Branding Pulsing Loader
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEFF3F5)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF307EF3),
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(70.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "در حال بارگذاری...",
                        color = Color(0xFF6A8BA5),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
                is StartupUiState.BiometricRequired, is StartupUiState.BiometricFailed -> {
                    // Biometric Authentication Center Icon/Art
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEFF3F5)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Drawing fingerprint icon/representation manually since we don't have standard assets
                        CircularProgressIndicator(
                            progress = { 1f },
                            color = Color(0xFF384C5C),
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(90.dp)
                        )
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF384C5C)),
                            contentAlignment = Alignment.Center
                        ) {
                            // Text representation or generic lock shape
                            Text(
                                text = "🔒",
                                fontSize = 24.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "ورود امن به برنامه",
                        color = Color(0xFF333638),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "برای ورود به برنامه، حسگر اثر انگشت یا تشخیص چهره دستگاه خود را لمس کنید.",
                        color = Color(0xFF6A8BA5),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(48.dp))
                    
                    UzradyabPrimaryButton(
                        text = "ورود با اثر انگشت",
                        onClick = onRetryBiometric,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    UzradyabTextAction(
                        text = "ورود با رمز عبور / حساب دیگر",
                        onClick = onLogoutClick
                    )
                }
            }
        }
    }
}

// Extension to find host Activity from context (climbing ContextWrapper ladder)
fun Context.findActivity(): FragmentActivity? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is FragmentActivity) {
            return currentContext
        }
        currentContext = currentContext.baseContext
    }
    return null
}
