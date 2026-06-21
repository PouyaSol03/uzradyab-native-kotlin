package com.example.uzradyab.presentation.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.uzradyab.core.designsystem.SkeletonBox
import com.example.uzradyab.presentation.map.AppMenuDialog
import com.example.uzradyab.presentation.map.AppTopToolbar
import com.example.uzradyab.presentation.map.BackButton
import com.example.uzradyab.presentation.map.DeviceSelectDialog
import com.example.uzradyab.presentation.map.MenuGridButton

@Composable
fun ReportsRoute(
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onAddDeviceClick: () -> Unit,
    onNavigateToDeviceStatus: () -> Unit,
    onNavigateToStopReports: () -> Unit,
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ReportsScreen(
        state = state,
        onBackClick = onBackClick,
        onLogoutClick = onLogoutClick,
        onAddDeviceClick = onAddDeviceClick,
        onDeviceSelected = viewModel::selectDevice,
        onNavigateToDeviceStatus = onNavigateToDeviceStatus,
        onNavigateToStopReports = onNavigateToStopReports
    )
}

@Composable
fun ReportsScreen(
    state: ReportsUiState,
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onAddDeviceClick: () -> Unit,
    onDeviceSelected: (Long) -> Unit,
    onNavigateToDeviceStatus: () -> Unit,
    onNavigateToStopReports: () -> Unit
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "گزارش ترکیبی",
                                color = Color(0xFF676C70),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                            )
                            Icon(
                                imageVector = Icons.Default.EventNote,
                                contentDescription = null,
                                tint = Color(0xFF676C70),
                                modifier = Modifier.size(20.dp)
                            )
                        }
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
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(figmaBackground)
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        val selectedDevice = state.devices.firstOrNull { it.id == state.selectedDeviceId }
                        DeviceSelectTrigger(
                            text = selectedDevice?.name ?: "انتخاب دستگاه",
                            onClick = { deviceSelectorOpen = true },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        CurrentDeviceStatusSection(
                            state = state,
                            onNavigateToDeviceStatus = onNavigateToDeviceStatus
                        )

                        OtherReportsSection(onItemClick = { reportType ->
                            if (reportType == "وضعیت دستگاه") {
                                onNavigateToDeviceStatus()
                            } else if (reportType == "توقف‌ها") {
                                onNavigateToStopReports()
                            } else {
                                android.util.Log.d("ReportsRoute", "Clicked on: $reportType")
                            }
                        })

                        Spacer(modifier = Modifier.height(24.dp))
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
            }
        }
    }
}

@Composable
private fun CurrentDeviceStatusSection(
    state: ReportsUiState,
    onNavigateToDeviceStatus: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "وضعیت جاری دستگاه",
                color = Color(0xFF333638),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier
                    .background(Color(0xFF9B26B6), RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = state.deviceStatusText,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.GpsFixed,
                    contentDescription = null,
                    tint = Color(0xFF307EF3),
                    modifier = Modifier.size(28.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "موقعیت",
                        color = Color(0xFF8F99A3),
                        fontSize = 12.sp
                    )
                    if (state.isLoading && state.currentAddress == "در حال دریافت...") {
                        SkeletonBox(modifier = Modifier.width(180.dp).height(18.dp))
                    } else {
                        Text(
                            text = state.currentAddress,
                            color = Color(0xFF333638),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = "مسافت پیموده",
                value = state.distanceKm,
                unit = "کیلومتر",
                icon = Icons.Default.Route,
                isLoading = state.isLoading
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = "مصرف سوخت",
                value = state.fuelLiters,
                unit = "لیتر",
                icon = Icons.Default.LocalGasStation,
                isLoading = state.isLoading
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = "میانگین سرعت",
                value = state.averageSpeed,
                unit = "کیلومتر",
                icon = Icons.Default.Speed,
                isLoading = state.isLoading
            )
        }

        OutlinedButton(
            onClick = onNavigateToDeviceStatus,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF307EF3)
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF307EF3))
        ) {
            Text(
                text = "جزئیات بیشتر",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun DeviceSelectTrigger(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(44.dp)
            .shadow(18.dp, RoundedCornerShape(8.dp), clip = false)
            .background(Color.White, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.DirectionsCar,
            contentDescription = "Car",
            tint = Color.Black,
            modifier = Modifier.size(width = 20.dp, height = 16.dp)
        )
        Text(
            text = text,
            color = Color.Black,
            fontSize = 14.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Right,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
        )
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = "Chevron Down",
            tint = Color(0xFF1C262E),
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    unit: String,
    icon: ImageVector,
    isLoading: Boolean = false
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF307EF3),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = title,
                    color = Color(0xFF8F99A3),
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }

            if (isLoading && value == "۰") {
                SkeletonBox(modifier = Modifier.width(50.dp).height(16.dp))
            } else {
                Text(
                    text = "$value $unit",
                    color = Color(0xFF333638),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun OtherReportsSection(onItemClick: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "سایر گزارش‌ها",
            color = Color(0xFF333638),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        val reportMenuItems = listOf(
            "وضعیت دستگاه",
            "توقف‌ها",
            "وضعیت روزانه",
            "بازپخش مسیر",
            "رویدادها",
            "مسافت‌ها"
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
        ) {
            reportMenuItems.forEachIndexed { index, title ->
                ReportMenuItem(
                    title = title,
                    onClick = { onItemClick(title) }
                )
                if (index < reportMenuItems.lastIndex) {
                    HorizontalDivider(
                        color = Color(0xFFF3F4F6),
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ReportMenuItem(title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color(0xFF307EF3),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Navigate",
            tint = Color(0xFF307EF3),
            modifier = Modifier.size(20.dp)
        )
    }
}
