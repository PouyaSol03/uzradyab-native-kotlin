package com.example.uzradyab.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import com.example.uzradyab.presentation.components.LocalSnackbarController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.uzradyab.core.biometric.BiometricHelper
import com.example.uzradyab.ui.theme.AppBackground
import com.example.uzradyab.ui.theme.AppBlue
import com.example.uzradyab.ui.theme.AppTextPrimary
import com.example.uzradyab.presentation.map.AppTopToolbar
import com.example.uzradyab.presentation.map.BackButton
import com.example.uzradyab.presentation.map.MenuGridButton
import com.example.uzradyab.R
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDirection
import com.example.uzradyab.presentation.common.UzradyabInput
import com.example.uzradyab.presentation.common.UzradyabPrimaryButton

@Composable
fun ProfileRoute(
    onLogoutClick: () -> Unit,
    onBackClick: () -> Unit,
    onMenuClick: () -> Unit,
) {
    val viewModel: ProfileViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.signedOut) {
        if (state.signedOut) {
            onLogoutClick()
        }
    }

    ProfileScreen(
        state = state,
        onLogoutClick = viewModel::logout,
        onSaveClick = viewModel::updateProfile,
        onResetSaveSuccess = viewModel::resetSaveSuccess,
        onClearError = viewModel::clearError,
        onBackClick = onBackClick,
        onMenuClick = onMenuClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    state: ProfileUiState,
    onLogoutClick: () -> Unit,
    onSaveClick: (com.example.uzradyab.data.remote.dto.SessionDto) -> Unit,
    onResetSaveSuccess: () -> Unit,
    onClearError: () -> Unit,
    onBackClick: () -> Unit,
    onMenuClick: () -> Unit,
) {
    val context = LocalContext.current
    val snackbarController = LocalSnackbarController.current
    val biometricHelper = remember { BiometricHelper(context) }
    
    var name by remember(state.sessionDto) { mutableStateOf(state.sessionDto?.name ?: "") }
    var email by remember(state.sessionDto) { mutableStateOf(state.sessionDto?.email ?: "") }
    var password by remember { mutableStateOf("") }
    var phone by remember(state.sessionDto) { mutableStateOf(state.sessionDto?.phone ?: "") }
    var expirationTime by remember(state.sessionDto) { 
        mutableStateOf(state.sessionDto?.expirationTime?.take(10)?.let { gregorianDate ->
            val parts = gregorianDate.split("-")
            if (parts.size == 3) {
                val gY = parts[0].toIntOrNull() ?: 2099
                val gM = parts[1].toIntOrNull() ?: 12
                val gD = parts[2].toIntOrNull() ?: 31
                val jDate = com.example.uzradyab.core.utils.JalaliUtils.gregorianToJalali(gY, gM, gD)
                val monthName = com.example.uzradyab.core.utils.JalaliUtils.getMonthName(jDate[1])
                com.example.uzradyab.core.utils.JalaliUtils.run { "${jDate[2]} $monthName ${jDate[0]}".toPersianDigits() }
            } else {
                gregorianDate
            }
        } ?: "نامشخص") 
    }
    
    var isBiometricEnabled by remember { mutableStateOf(biometricHelper.isBiometricEnabled()) }

    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            snackbarController.showSuccess("تغییرات با موفقیت ذخیره شد")
            onResetSaveSuccess()
        }
    }

    LaunchedEffect(state.error) {
        if (state.error != null) {
            snackbarController.showError(state.error)
            onClearError()
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                AppTopToolbar(
                    startContent = {
                        BackButton(onClick = onBackClick)
                    },
                    centerContent = {
                        Text(
                            text = stringResource(R.string.str_a802ae5f), 
                            fontSize = 16.sp, 
                            fontWeight = FontWeight.Bold,
                            color = AppTextPrimary
                        ) 
                    },
                    endContent = {
                        MenuGridButton(onClick = onMenuClick)
                    },
                    modifier = Modifier
                        .statusBarsPadding()
                        .height(64.dp)
                )
            },
            containerColor = AppBackground,
        ) { innerPadding ->
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppBlue)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Modern User Info Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column {
                            // Header matching React App
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(AppBlue)
                                    .padding(vertical = 12.dp, horizontal = 20.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(R.string.str_85a99996),
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            // Body
                            Column(modifier = Modifier.padding(24.dp)) {
                                
                                // Expiration Time (Readonly)
                                UzradyabInput(
                                    value = expirationTime,
                                    onValueChange = {},
                                    label = stringResource(R.string.str_cb32d819),
                                    enabled = false
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))

                                // Name
                                UzradyabInput(
                                    value = name,
                                    onValueChange = { name = it },
                                    label = stringResource(R.string.str_eaa6c1e8)
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Email
                                UzradyabInput(
                                    value = email,
                                    onValueChange = { email = it },
                                    label = stringResource(R.string.str_6505762a),
                                    placeholder = "example@test.com",
                                    inputTextDirection = TextDirection.Ltr
                                )

                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Phone
                                UzradyabInput(
                                    value = phone,
                                    onValueChange = { phone = it },
                                    label = stringResource(R.string.str_1d020430),
                                    placeholder = "09123456789",
                                    inputTextDirection = TextDirection.Ltr
                                )

                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Password
                                UzradyabInput(
                                    value = password,
                                    onValueChange = { password = it },
                                    label = stringResource(R.string.str_6814380a),
                                    placeholder = stringResource(R.string.str_8634402d),
                                    visualTransformation = PasswordVisualTransformation(),
                                    inputTextDirection = TextDirection.Ltr
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Security Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.str_2944a2c7),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppTextPrimary
                                )
                                Text(
                                    text = stringResource(R.string.str_f57206ad),
                                    fontSize = 12.sp,
                                    color = Color(0xFF6A8BA5)
                                )
                            }
                            Switch(
                                checked = isBiometricEnabled,
                                onCheckedChange = { 
                                    isBiometricEnabled = it
                                    biometricHelper.setBiometricEnabled(it)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = AppBlue
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action Buttons
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Button(
                            onClick = onLogoutClick,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                        ) {
                            Text(text = stringResource(R.string.str_60806661), fontSize = 16.sp, fontWeight = FontWeight.Normal)
                        }
                        
                        UzradyabPrimaryButton(
                            text = if (state.isSaving) "در حال ذخیره..." else stringResource(R.string.str_9b860f70),
                            onClick = { 
                                state.sessionDto?.let { session ->
                                    val updatedSession = session.copy(
                                        name = name.takeIf { it.isNotBlank() },
                                        email = email.takeIf { it.isNotBlank() },
                                        phone = phone.takeIf { it.isNotBlank() },
                                        password = password.takeIf { it.isNotBlank() }
                                    )
                                    onSaveClick(updatedSession)
                                }
                            },
                            enabled = !state.isSaving,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
