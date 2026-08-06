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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.launch
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
import com.example.uzradyab.presentation.components.LocalSnackbarController
import com.example.uzradyab.presentation.components.MaintenanceWrapper
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
)

@Composable
fun AlertsSettingsRoute(
    onBackClick: () -> Unit,
    viewModel: AlertsSettingsViewModel = hiltViewModel()
) {
    AlertsSettingsScreen(
        isLoading = viewModel.isLoading,
        isMaintenanceMode = viewModel.isMaintenanceMode,
        errorMessage = viewModel.errorMessage,
        notificationStates = viewModel.notificationStates,
        onTogglePreference = viewModel::togglePreference,
        onClearError = viewModel::clearError,
        onBackClick = onBackClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsSettingsScreen(
    isLoading: Boolean,
    isMaintenanceMode: Boolean,
    errorMessage: String?,
    notificationStates: androidx.compose.runtime.snapshots.SnapshotStateMap<String, Boolean>,
    onTogglePreference: (String) -> Unit,
    onClearError: () -> Unit,
    onBackClick: () -> Unit
) {
    val snackbarController = LocalSnackbarController.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarController.showError(errorMessage)
            onClearError()
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaintenanceWrapper(isMaintenanceMode = isMaintenanceMode) {
            Scaffold(
            topBar = {
                AppTopToolbar(
                    startContent = { BackButton(onClick = onBackClick) },
                    centerContent = {
                        Text(
                            text = "تنظیمات هشدارها",
                            color = themedColor(light = Color(0xFF676C70), dark = Color(0xFF929292)),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    },
                    modifier = Modifier
                        .statusBarsPadding()
                        .height(64.dp)
                )
            },
            containerColor = themedColor(light = Color(0xFFF3F4F6), dark = Color(0xFF1A1D23)),
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
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(themedColor(light = Color(0xFF6A8BA5), dark = Color(0xFF99A7B3)), RoundedCornerShape(8.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "با فعال کردن هر گزینه، اعلان مربوط به آن از طریق نوتیفیکیشن به موبایل شما ارسال خواهد شد.",
                            color = themedColor(light = Color.White, dark = Color.White),
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
}

@Composable
fun SectionCard(
    section: NotificationSection,
    notificationStates: androidx.compose.runtime.snapshots.SnapshotStateMap<String, Boolean>,
    onTogglePreference: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, themedColor(light = Color(0xFFE5E7EB), dark = Color(0xFF1B1D23)), RoundedCornerShape(8.dp)), // Gray200
        colors = CardDefaults.cardColors(containerColor = themedColor(light = Color.White, dark = Color(0xFF27343F))),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(section.icon, contentDescription = null, tint = themedColor(light = Color.Black, dark = Color(0xFFE0E0E0)), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = section.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = themedColor(light = Color.Black, dark = Color(0xFFE0E0E0))
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            section.options.forEachIndexed { index, option ->
                NotificationRow(
                    option = option,
                    isChecked = notificationStates[option.key] ?: true,
                    onTogglePreference = onTogglePreference
                )
            }
        }
    }
}

@Composable
fun NotificationRow(
    option: NotificationOption,
    isChecked: Boolean,
    onTogglePreference: (String) -> Unit
) {
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
            color = themedColor(light = Color.Black, dark = Color(0xFFE0E0E0))
        )
        Switch(
            checked = isChecked,
            onCheckedChange = { onTogglePreference(option.key) },
            colors = SwitchDefaults.colors(
                checkedThumbColor = themedColor(light = Color.White, dark = Color.White),
                checkedTrackColor = themedColor(light = Color(0xFF307EF3), dark = Color(0xFF5F98EC)), // Primary500
                uncheckedThumbColor = themedColor(light = Color.White, dark = Color.White),
                uncheckedTrackColor = themedColor(light = Color(0xFFD1D5DB), dark = Color(0xFF22252B)) // Gray300
            )
        )
    }
}
