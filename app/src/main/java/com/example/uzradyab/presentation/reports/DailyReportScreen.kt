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
import com.example.uzradyab.core.utils.toImmutable
import com.example.uzradyab.R
import androidx.compose.ui.res.stringResource

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
        onCustomDateDismiss = viewModel::dismissCustomDatePicker,
        onClearError = viewModel::clearError
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
    onCustomDateDismiss: () -> Unit,
    onClearError: () -> Unit
) {
    val figmaBackground = Color(0xFFF3F4F6)
    var menuOpen by remember { mutableStateOf(false) }
    var deviceSelectorOpen by remember { mutableStateOf(false) }
    
    val snackbarHostState = remember { SnackbarHostState() }

    /*
    LaunchedEffect(state.error) {
        state.error?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            onClearError()
        }
    }
    */

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                AppTopToolbar(
                    startContent = { BackButton(onClick = onBackClick) },
                    centerContent = {
                        Text(
                            text = stringResource(R.string.str_454a0f23),
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
                        val selectedDevice = remember(state.devices, state.selectedDeviceId) {
                            state.devices.firstOrNull { it.id == state.selectedDeviceId }
                        }
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
                        
                        Spacer(modifier = Modifier.height(16.dp))

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
                            title = stringResource(R.string.str_07dff32f),
                            address = state.startAddressResolved,
                            icon = Icons.Default.GpsFixed,
                            isLoading = state.isLoading
                        )
                        
                        LocationCard(
                            title = stringResource(R.string.str_f5fda019),
                            address = state.endAddressResolved,
                            icon = Icons.Default.GpsFixed,
                            isLoading = state.isLoading
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            DetailStatCard(
                                modifier = Modifier.weight(1f),
                                title = stringResource(R.string.str_21c081b4),
                                value = state.jalaliStartTime,
                                unit = "",
                                icon = Icons.Default.FlashOn,
                                isLoading = state.isLoading
                            )
                            DetailStatCard(
                                modifier = Modifier.weight(1f),
                                title = stringResource(R.string.str_3d6f575f),
                                value = state.ignitionDuration,
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
                                title = stringResource(R.string.str_0ddc0842),
                                value = state.averageSpeed,
                                unit = "کیلومتر بر ساعت",
                                icon = Icons.Default.Speed,
                                isLoading = state.isLoading
                            )
                            DetailStatCard(
                                modifier = Modifier.weight(1f),
                                title = stringResource(R.string.str_8263bc39),
                                value = state.maxSpeed,
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
                                title = stringResource(R.string.str_c85e5dc9),
                                value = state.distance,
                                unit = "کیلومتر",
                                icon = Icons.Default.Speed,
                                isLoading = state.isLoading
                            )
                            DetailStatCard(
                                modifier = Modifier.weight(1f),
                                title = stringResource(R.string.str_a604190b),
                                value = state.spentFuel,
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
                                title = stringResource(R.string.str_2fecc133),
                                value = state.startOdometer,
                                unit = "کیلومتر",
                                icon = Icons.Outlined.Timer,
                                isLoading = state.isLoading
                            )
                            DetailStatCard(
                                modifier = Modifier.weight(1f),
                                title = stringResource(R.string.str_9d6d33d5),
                                value = state.endOdometer,
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
                            Text(stringResource(R.string.str_f792dea4), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onTraveledPathsClick,
                            modifier = Modifier
                                .weight(2f)
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF307EF3))
                        ) {
                            Text(stringResource(R.string.str_fbcc4761), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
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

                /*
                // Custom Error Snackbar
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                ) {
                    SnackbarHost(
                        hostState = snackbarHostState,
                        snackbar = { data ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFDECEA)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = androidx.compose.material.icons.Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = Color(0xFFE55353)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = data.visuals.message,
                                        color = Color(0xFFE55353),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    )
                }
                */
            }
        }
    }
}
