package com.example.uzradyab.presentation.reports

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.uzradyab.presentation.map.AppMenuDialog
import com.example.uzradyab.presentation.map.AppTopToolbar
import com.example.uzradyab.presentation.map.BackButton
import com.example.uzradyab.presentation.map.DeviceSelectDialog
import com.example.uzradyab.presentation.map.MenuGridButton
import com.example.uzradyab.core.utils.JalaliUtils
import java.text.SimpleDateFormat
import java.util.TimeZone
import java.util.Locale

@Composable
fun DailyReportRoute(
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onAddDeviceClick: () -> Unit,
    onTraveledPathsClick: () -> Unit,
    viewModel: DailyReportViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    DailyReportScreen(
        state = state,
        onBackClick = onBackClick,
        onLogoutClick = onLogoutClick,
        onAddDeviceClick = onAddDeviceClick,
        onTraveledPathsClick = onTraveledPathsClick,
        onDeviceSelected = viewModel::onDeviceSelected,
        onFilterSelected = viewModel::onDateFilterSelected,
        onCustomDateApply = viewModel::applyCustomDateRange,
        onCustomDateDismiss = viewModel::dismissCustomDatePicker
    )
}

@Composable
fun DailyReportScreen(
    state: DailyReportUiState,
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onAddDeviceClick: () -> Unit,
    onTraveledPathsClick: () -> Unit,
    onDeviceSelected: (Long) -> Unit,
    onFilterSelected: (String) -> Unit,
    onCustomDateApply: (com.example.uzradyab.presentation.components.JalaliDateTime?, com.example.uzradyab.presentation.components.JalaliDateTime?) -> Unit,
    onCustomDateDismiss: () -> Unit
) {
    val figmaBackground = Color(0xFFF3F4F6)
    var menuOpen by remember { mutableStateOf(false) }
    var deviceSelectorOpen by remember { mutableStateOf(false) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                AppTopToolbar(
                    startContent = { BackButton(onClick = onBackClick) },
                    centerContent = {
                        Text(
                            text = "وضعیت روزانه",
                            color = Color(0xFF676C70),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    },
                    endContent = { MenuGridButton(onClick = { menuOpen = true }) },
                    modifier = Modifier
                        .statusBarsPadding()
                        .height(64.dp)
                )
            },
            containerColor = figmaBackground,
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // Search and Filter section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(16.dp)
                    ) {
                        val selectedDevice = state.devices.firstOrNull { it.id == state.selectedDeviceId }
                        DeviceSelectTrigger(
                            text = selectedDevice?.name ?: "انتخاب دستگاه",
                            onClick = { deviceSelectorOpen = true },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("امروز", "دیروز", "هفته گذشته", "تاریخ سفارشی").forEach { filter ->
                                FilterChip(
                                    text = filter,
                                    isSelected = state.selectedDateFilter == filter,
                                    onClick = { onFilterSelected(filter) }
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        if (state.error != null) {
                            Text(
                                text = state.error,
                                color = Color.Red,
                                fontSize = 14.sp,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }

                        // Status card
                        val deviceStatusText = "روشن" // Defaulting to on, as summary is just daily aggregated data
                        val statusColor = Color(0xFF14B8A6) // Green color for online

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(statusColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsCar,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = deviceStatusText,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        LocationCard(
                            title = "موقعیت مبدا",
                            address = state.startAddressResolved,
                            icon = Icons.Default.GpsFixed,
                            isLoading = state.isLoading
                        )
                        
                        LocationCard(
                            title = "موقعیت جاری دستگاه",
                            address = state.endAddressResolved,
                            icon = Icons.Default.GpsFixed,
                            isLoading = state.isLoading
                        )

                        // Convert engine hours (ms to hours)
                        val engineHoursMs = state.summaryReport?.engineHours ?: 0L
                        val ignitionDuration = String.format("%.1f", engineHoursMs / 3600000.0) + " ساعت"
                        
                        // Parse start time to Jalali
                        var jalaliStartTime = "- : -"
                        state.summaryReport?.startTime?.let { isoStart ->
                            jalaliStartTime = formatIsoTime(isoStart)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            DetailStatCard(
                                modifier = Modifier.weight(1f),
                                title = "اولین زمان روشن شدن",
                                value = jalaliStartTime,
                                unit = "",
                                icon = Icons.Default.FlashOn,
                                isLoading = state.isLoading
                            )
                            DetailStatCard(
                                modifier = Modifier.weight(1f),
                                title = "مدت روشن بودن دستگاه",
                                value = ignitionDuration,
                                unit = "",
                                icon = Icons.Default.AccessTime,
                                isLoading = state.isLoading
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            DetailStatCard(
                                modifier = Modifier.weight(1f),
                                title = "میانگین سرعت",
                                value = String.format("%.1f", state.summaryReport?.averageSpeed?.times(1.852) ?: 0.0),
                                unit = "کیلومتر بر ساعت",
                                icon = Icons.Default.Speed,
                                isLoading = state.isLoading
                            )
                            DetailStatCard(
                                modifier = Modifier.weight(1f),
                                title = "حداکثر سرعت",
                                value = String.format("%.1f", state.summaryReport?.maxSpeed?.times(1.852) ?: 0.0),
                                unit = "کیلومتر بر ساعت",
                                icon = Icons.Default.Speed,
                                isLoading = state.isLoading
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            DetailStatCard(
                                modifier = Modifier.weight(1f),
                                title = "مسافت طی شده",
                                value = String.format("%.1f", state.summaryReport?.distance?.div(1000.0) ?: 0.0),
                                unit = "کیلومتر",
                                icon = Icons.Default.Speed,
                                isLoading = state.isLoading
                            )
                            DetailStatCard(
                                modifier = Modifier.weight(1f),
                                title = "سوخت مصرفی",
                                value = String.format("%.1f", state.summaryReport?.spentFuel ?: 0.0),
                                unit = "لیتر",
                                icon = Icons.Default.LocalGasStation,
                                isLoading = state.isLoading
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            DetailStatCard(
                                modifier = Modifier.weight(1f),
                                title = "متر مسافت (ابتدا)",
                                value = String.format("%.1f", state.summaryReport?.startOdometer?.div(1000.0) ?: 0.0),
                                unit = "کیلومتر",
                                icon = Icons.Outlined.Timer,
                                isLoading = state.isLoading
                            )
                            DetailStatCard(
                                modifier = Modifier.weight(1f),
                                title = "متر مسافت (انتها)",
                                value = String.format("%.1f", state.summaryReport?.endOdometer?.div(1000.0) ?: 0.0),
                                unit = "کیلومتر",
                                icon = Icons.Outlined.Timer,
                                isLoading = state.isLoading
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(figmaBackground)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { /* Handle Export */ },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF307EF3)),
                            border = BorderStroke(1.dp, Color(0xFF307EF3))
                        ) {
                            Text("خروجی", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onTraveledPathsClick,
                            modifier = Modifier
                                .weight(2f)
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF307EF3))
                        ) {
                            Text("مسیرهای پیموده شده", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                if (menuOpen) {
                    AppMenuDialog(
                        onDismiss = { menuOpen = false },
                        onLogoutClick = onLogoutClick,
                        onAddDeviceClick = onAddDeviceClick
                    )
                }

                if (deviceSelectorOpen) {
                    DeviceSelectDialog(
                        devices = state.devices,
                        selectedDeviceId = state.selectedDeviceId,
                        onDeviceClick = { deviceId ->
                            onDeviceSelected(deviceId)
                            deviceSelectorOpen = false
                        },
                        onDismiss = { deviceSelectorOpen = false }
                    )
                }
                
                if (state.showCustomDatePicker) {
                    CustomDateBottomSheet(
                        onDismiss = onCustomDateDismiss,
                        onApplyCustomRange = onCustomDateApply
                    )
                }
            }
        }
    }
}

private fun formatIsoTime(isoString: String): String {
    try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = sdf.parse(isoString) ?: return "- : -"
        
        val localCal = java.util.Calendar.getInstance(TimeZone.getTimeZone("Asia/Tehran")).apply {
            time = date
        }
        val year = localCal.get(java.util.Calendar.YEAR)
        val month = localCal.get(java.util.Calendar.MONTH) + 1
        val day = localCal.get(java.util.Calendar.DAY_OF_MONTH)
        
        val jalali = JalaliUtils.gregorianToJalali(year, month, day)
        
        val timeFormat = SimpleDateFormat("HH:mm", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("Asia/Tehran")
        }
        val localTimeStr = timeFormat.format(date)
        
        return "${jalali[0]}/${String.format("%02d", jalali[1])}/${String.format("%02d", jalali[2])} $localTimeStr"
    } catch (e: Exception) {
        return "- : -"
    }
}
