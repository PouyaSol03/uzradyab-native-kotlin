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
import com.example.uzradyab.core.utils.toImmutable
import com.example.uzradyab.presentation.map.AppMenuDialog
import com.example.uzradyab.presentation.map.AppTopToolbar
import com.example.uzradyab.presentation.map.BackButton
import com.example.uzradyab.presentation.map.DeviceSelectDialog
import com.example.uzradyab.presentation.map.MenuGridButton
import com.example.uzradyab.R
import androidx.compose.ui.res.stringResource
import com.example.uzradyab.ui.theme.themedColor

@Composable
fun ReportsRoute(
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onAddDeviceClick: () -> Unit,
    onNavigateToDeviceStatus: () -> Unit,
    onNavigateToDailyReport: (Long?) -> Unit,
    onNavigateToStopReports: () -> Unit,
    onNavigateToReplayTrip: (Long?) -> Unit,
    onNavigateToEvents: () -> Unit,
    onNavigateToTripReports: () -> Unit,
    onNavigateToGeofences: () -> Unit = {},
    onNavigateToMaintenance: () -> Unit = {},
    onAboutClick: () -> Unit = {},
    onContactSupportClick: () -> Unit = {},
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
        onNavigateToDailyReport = onNavigateToDailyReport,
        onNavigateToStopReports = onNavigateToStopReports,
        onNavigateToReplayTrip = onNavigateToReplayTrip,
        onNavigateToEvents = onNavigateToEvents,
        onNavigateToTripReports = onNavigateToTripReports,
        onNavigateToGeofences = onNavigateToGeofences,
        onNavigateToMaintenance = onNavigateToMaintenance,
        onAboutClick = onAboutClick,
        onContactSupportClick = onContactSupportClick,
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
    onNavigateToDailyReport: (Long?) -> Unit,
    onNavigateToStopReports: () -> Unit,
    onNavigateToReplayTrip: (Long?) -> Unit,
    onNavigateToEvents: () -> Unit,
    onNavigateToTripReports: () -> Unit,
    onNavigateToGeofences: () -> Unit,
    onNavigateToMaintenance: () -> Unit,
    onAboutClick: () -> Unit,
    onContactSupportClick: () -> Unit
) {
    val figmaBackground = themedColor(light = Color(0xFFF3F4F6), dark = Color(0xFF1A1D23))
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
                                text = stringResource(R.string.str_8fd3df27),
                                color = themedColor(light = Color(0xFF676C70), dark = Color(0xFF929292)),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                            )
                            Icon(
                                imageVector = Icons.Default.EventNote,
                                contentDescription = null,
                                tint = themedColor(light = Color(0xFF676C70), dark = Color(0xFF929292)),
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
                        val selectedDevice = remember(state.devices, state.selectedDeviceId) {
                            state.devices.firstOrNull { it.id == state.selectedDeviceId }
                        }
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
                            } else if (reportType == "وضعیت روزانه") {
                                onNavigateToDailyReport(state.selectedDeviceId)
                            } else if (reportType == "توقف‌ها") {
                                onNavigateToStopReports()
                            } else if (reportType == "بازپخش مسیر") {
                                onNavigateToReplayTrip(state.selectedDeviceId)
                            } else if (reportType == "رویدادها") {
                                onNavigateToEvents()
                            } else if (reportType == "مسافت‌ها") {
                                onNavigateToTripReports()
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
                        onAddDeviceClick = onAddDeviceClick,
                        onGeofencesClick = onNavigateToGeofences,
                        onMaintenanceClick = onNavigateToMaintenance,
                        onAboutClick = onAboutClick,
                        onContactSupportClick = onContactSupportClick,
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
                text = stringResource(R.string.str_cf441594),
                color = themedColor(light = Color(0xFF333638), dark = Color(0xFFAFB3B6)),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier
                    .background(themedColor(light = Color(0xFF9B26B6), dark = Color(0xFFB959CF)), RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = themedColor(light = Color.White, dark = Color.White),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = state.deviceStatusText,
                    color = themedColor(light = Color.White, dark = Color.White),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = themedColor(light = Color.White, dark = Color(0xFF27343F))),
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
                    tint = themedColor(light = Color(0xFF307EF3), dark = Color(0xFF5F98EC)),
                    modifier = Modifier.size(28.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.str_6ba39ff9),
                        color = themedColor(light = Color(0xFF8F99A3), dark = Color(0xFFA6A6A6)),
                        fontSize = 12.sp
                    )
                    if (state.isLoading && state.currentAddress == "در حال دریافت...") {
                        SkeletonBox(modifier = Modifier.width(180.dp).height(18.dp))
                    } else {
                        Text(
                            text = state.currentAddress,
                            color = themedColor(light = Color(0xFF333638), dark = Color(0xFFAFB3B6)),
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
                title = stringResource(R.string.str_0a62ea7c),
                value = state.distanceKm,
                unit = "کیلومتر",
                icon = Icons.Default.Route,
                isLoading = state.isLoading
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.str_1b7dc619),
                value = state.fuelLiters,
                unit = "لیتر",
                icon = Icons.Default.LocalGasStation,
                isLoading = state.isLoading
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.str_0ddc0842),
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
                contentColor = themedColor(light = Color(0xFF307EF3), dark = Color(0xFF5F98EC))
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, themedColor(light = Color(0xFF307EF3), dark = Color(0xFF5F98EC)))
        ) {
            Text(
                text = stringResource(R.string.str_0799c18b),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
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
        colors = CardDefaults.cardColors(containerColor = themedColor(light = Color.White, dark = Color(0xFF27343F))),
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
                    tint = themedColor(light = Color(0xFF307EF3), dark = Color(0xFF5F98EC)),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = title,
                    color = themedColor(light = Color(0xFF8F99A3), dark = Color(0xFFA6A6A6)),
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }

            if (isLoading && value == "۰") {
                SkeletonBox(modifier = Modifier.width(50.dp).height(16.dp))
            } else {
                Text(
                    text = "$value $unit",
                    color = themedColor(light = Color(0xFF333638), dark = Color(0xFFAFB3B6)),
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
            text = stringResource(R.string.str_8ffce954),
            color = themedColor(light = Color(0xFF333638), dark = Color(0xFFAFB3B6)),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        val reportMenuItems = listOf(
            "وضعیت دستگاه",
            "توقف‌ها",
            "بازپخش مسیر",
            "رویدادها",
            "مسافت‌ها"
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(themedColor(light = Color.White, dark = Color(0xFF27343F)))
        ) {
            reportMenuItems.forEachIndexed { index, title ->
                ReportMenuItem(
                    title = title,
                    onClick = { onItemClick(title) }
                )
                if (index < reportMenuItems.lastIndex) {
                    HorizontalDivider(
                        color = themedColor(light = Color(0xFFF3F4F6), dark = Color(0xFF1A1D23)),
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
            color = themedColor(light = Color(0xFF307EF3), dark = Color(0xFF5F98EC)),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Navigate",
            tint = themedColor(light = Color(0xFF307EF3), dark = Color(0xFF5F98EC)),
            modifier = Modifier.size(20.dp)
        )
    }
}
