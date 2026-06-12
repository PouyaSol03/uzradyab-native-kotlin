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

@Composable
fun ProfileRoute(
    onLogoutClick: () -> Unit,
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
        onResetSaveSuccess = viewModel::resetSaveSuccess
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    state: ProfileUiState,
    onLogoutClick: () -> Unit,
    onSaveClick: (com.example.uzradyab.data.remote.dto.SessionDto) -> Unit,
    onResetSaveSuccess: () -> Unit,
) {
    val context = LocalContext.current
    val biometricHelper = remember { BiometricHelper(context) }
    
    var name by remember(state.sessionDto) { mutableStateOf(state.sessionDto?.name ?: "") }
    var email by remember(state.sessionDto) { mutableStateOf(state.sessionDto?.email ?: "") }
    var password by remember { mutableStateOf("") }
    var phone by remember(state.sessionDto) { mutableStateOf(state.sessionDto?.phone ?: "") }
    var expirationTime by remember(state.sessionDto) { mutableStateOf(state.sessionDto?.expirationTime?.take(10) ?: "2099-12-31") }

    
    var isBiometricEnabled by remember { mutableStateOf(biometricHelper.isBiometricEnabled()) }

    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            // Show toast or snackbar
            android.widget.Toast.makeText(context, "تغییرات با موفقیت ذخیره شد", android.widget.Toast.LENGTH_SHORT).show()
            onResetSaveSuccess()
        }
    }

    LaunchedEffect(state.error) {
        if (state.error != null) {
            android.widget.Toast.makeText(context, state.error, android.widget.Toast.LENGTH_LONG).show()
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppBackground)
                .statusBarsPadding()
        ) {
            // Top Bar
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = "حساب کاربری", 
                        fontSize = 16.sp, 
                        fontWeight = FontWeight.Bold,
                        color = AppTextPrimary
                    ) 
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppBlue)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
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
                                        text = "اطلاعات کاربر",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            // Body
                            Column(modifier = Modifier.padding(24.dp)) {
                                
                                // Expiration Time (Readonly)
                                Text("تاریخ انقضا", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF6A8BA5))
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = expirationTime,
                                    onValueChange = {},
                                    enabled = false,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledContainerColor = Color(0xFFF8FAFC),
                                        disabledBorderColor = Color(0xFFE2E8F0),
                                        disabledTextColor = Color(0xFF6A8BA5)
                                    )
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))

                                // Name
                                Text("نام کامل", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AppTextPrimary)
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = name,
                                    onValueChange = { name = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true,
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Email
                                Text("آدرس ایمیل", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AppTextPrimary)
                                Spacer(modifier = Modifier.height(8.dp))
                                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                    OutlinedTextField(
                                        value = email,
                                        onValueChange = { email = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        singleLine = true,
                                        placeholder = { Text("example@test.com") }
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Phone
                                Text("شماره موبایل", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AppTextPrimary)
                                Spacer(modifier = Modifier.height(8.dp))
                                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                    OutlinedTextField(
                                        value = phone,
                                        onValueChange = { phone = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        singleLine = true,
                                        placeholder = { Text("09123456789") }
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Password
                                Text("رمز عبور", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AppTextPrimary)
                                Spacer(modifier = Modifier.height(8.dp))
                                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                    OutlinedTextField(
                                        value = password,
                                        onValueChange = { password = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        singleLine = true,
                                        visualTransformation = PasswordVisualTransformation(),
                                        placeholder = { Text("تغییر رمز عبور (اختیاری)") }
                                    )
                                }
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
                                    text = "ورود با اثرانگشت / تشخیص چهره",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppTextPrimary
                                )
                                Text(
                                    text = "فعال‌سازی ورود سریع بیومتریک",
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
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                        ) {
                            Text(text = "خروج", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                        
                        Button(
                            onClick = { 
                                state.sessionDto?.let { session ->
                                    val updatedSession = session.copy(
                                        name = name,
                                        email = email,
                                        phone = phone,
                                        password = password.takeIf { it.isNotEmpty() }
                                    )
                                    onSaveClick(updatedSession)
                                }
                            },
                            enabled = !state.isSaving,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AppBlue)
                        ) {
                            if (state.isSaving) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Text(text = "ذخیره", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}
