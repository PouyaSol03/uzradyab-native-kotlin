package com.example.uzradyab.presentation.alerts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.uzradyab.R
import com.example.uzradyab.ui.theme.*
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.example.uzradyab.presentation.map.AppTopToolbar
import com.example.uzradyab.presentation.map.BackButton

data class NotificationOption(val label: String, val key: String)

data class NotificationSection(
    val title: String,
    val icon: ImageVector,
    val options: List<NotificationOption>
)

val NOTIFICATION_SECTIONS = listOf(
    NotificationSection(
        title = "وضعیت دستگاه",
        icon = Icons.Default.DirectionsCar,
        options = listOf(
            NotificationOption("حرکت دستگاه", "device_movement"),
            NotificationOption("باز شدن سوییچ", "ignition_on"),
            NotificationOption("بسته شدن سوییچ", "ignition_off"),
            NotificationOption("وضعیت آنلاین", "online_status")
        )
    ),
    NotificationSection(
        title = "محدوده جغرافیایی",
        icon = Icons.Default.Map,
        options = listOf(
            NotificationOption("ورود به محدوده", "geofence_enter"),
            NotificationOption("خروج از محدوده", "geofence_exit")
        )
    ),
    NotificationSection(
        title = "فنی",
        icon = Icons.Default.Settings,
        options = listOf(
            NotificationOption("نیاز به تعمیر", "maintenance_required"),
            NotificationOption("سرعت بالا", "high_speed"),
            NotificationOption("تصادف", "accident_sos"),
            NotificationOption("یدک کش", "towing"),
            NotificationOption("قطع ولتاژ", "power_cut")
        )
    ),
    NotificationSection(
        title = "اتصالات",
        icon = Icons.Default.Sensors,
        options = listOf(
            NotificationOption("دریافت پیامک", "sms_received"),
            NotificationOption("وضعیت آنلاین", "online_status")
        )
    )
)

@Composable
fun AlertsSettingsRoute(
    onBackClick: () -> Unit,
    viewModel: AlertsSettingsViewModel = hiltViewModel()
) {
    AlertsSettingsScreen(
        isLoading = viewModel.isLoading,
        errorMessage = viewModel.errorMessage,
        notificationStates = viewModel.notificationStates,
        onTogglePreference = viewModel::togglePreference,
        onBackClick = onBackClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsSettingsScreen(
    isLoading: Boolean,
    errorMessage: String?,
    notificationStates: Map<String, Boolean>,
    onTogglePreference: (String) -> Unit,
    onBackClick: () -> Unit
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                AppTopToolbar(
                    startContent = { BackButton(onClick = onBackClick) },
                    centerContent = {
                        Text(
                            text = "تنظیمات هشدارها",
                            color = Color(0xFF676C70),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    },
                    modifier = Modifier
                        .statusBarsPadding()
                        .height(64.dp)
                )
            },
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = onBackClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF307EF3))
                    ) {
                        Text("ذخیــــــره تغییرات", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                }
            },
            containerColor = Color(0xFFF3F4F6),
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF6A8BA5), RoundedCornerShape(8.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "با فعال کردن هر گزینه، اعلان مربوط به آن از طریق نوتیفیکیشن به موبایل شما ارسال خواهد شد.",
                            color = Color.White,
                            fontSize = 12.sp,
                            lineHeight = 22.sp
                        )
                    }
                }

                items(NOTIFICATION_SECTIONS) { section ->
                    SectionCard(
                        section = section,
                        notificationStates = notificationStates,
                        onTogglePreference = onTogglePreference
                    )
                }
            }
        }
    }
    }
}

@Composable
fun SectionCard(
    section: NotificationSection,
    notificationStates: Map<String, Boolean>,
    onTogglePreference: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp)), // Gray200
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(section.icon, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = section.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            section.options.forEachIndexed { index, option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = option.label,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Light,
                        color = Color.Black
                    )
                    Switch(
                        checked = notificationStates[option.key] ?: true,
                        onCheckedChange = { onTogglePreference(option.key) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF307EF3), // Primary500
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFFD1D5DB) // Gray300
                        )
                    )
                }
            }
        }
    }
}
