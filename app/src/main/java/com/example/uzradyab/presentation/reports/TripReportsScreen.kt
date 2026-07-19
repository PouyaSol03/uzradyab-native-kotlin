package com.example.uzradyab.presentation.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.uzradyab.presentation.components.JalaliDateTime
import com.example.uzradyab.presentation.map.AppMenuDialog
import com.example.uzradyab.presentation.map.AppTopToolbar
import com.example.uzradyab.presentation.map.BackButton
import com.example.uzradyab.presentation.map.DeviceSelectDialog
import com.example.uzradyab.presentation.map.MenuGridButton
import com.example.uzradyab.core.utils.toImmutable
import com.example.uzradyab.R
import androidx.compose.ui.res.stringResource
import com.example.uzradyab.ui.theme.themedColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripReportsRoute(
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onAddDeviceClick: () -> Unit,
    viewModel: TripReportsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    TripReportsScreen(
        state = state,
        onBackClick = onBackClick,
        onLogoutClick = onLogoutClick,
        onAddDeviceClick = onAddDeviceClick,
        onDeviceSelect = viewModel::onDeviceSelected,
        onDateFilterSelected = viewModel::onDateFilterSelected,
        onCustomDateApply = viewModel::applyCustomDateRange,
        onCustomDateDismiss = viewModel::dismissCustomDatePicker,
        onResolveAddress = viewModel::resolveAddress,
        onOpenColumnSelector = viewModel::openColumnSelector,
        onDismissColumnSelector = viewModel::dismissColumnSelector,
        onToggleColumn = viewModel::toggleColumn,
        onClearError = viewModel::clearError
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripReportsScreen(
    state: TripReportsUiState,
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onAddDeviceClick: () -> Unit,
    onDeviceSelect: (Long) -> Unit,
    onDateFilterSelected: (String) -> Unit,
    onCustomDateApply: (JalaliDateTime?, JalaliDateTime?) -> Unit,
    onCustomDateDismiss: () -> Unit,
    onResolveAddress: suspend (Double, Double) -> String,
    onOpenColumnSelector: () -> Unit,
    onDismissColumnSelector: () -> Unit,
    onToggleColumn: (String) -> Unit,
    onClearError: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }
    var deviceSelectorOpen by remember { mutableStateOf(false) }
    val figmaBackground = themedColor(light = Color(0xFFF3F4F6), dark = Color(0xFF1A1D23))

    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }

    /*
    androidx.compose.runtime.LaunchedEffect(state.error) {
        state.error?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = androidx.compose.material3.SnackbarDuration.Short
            )
            onClearError()
        }
    }
    */

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            containerColor = figmaBackground,
            topBar = {
                AppTopToolbar(
                    startContent = { BackButton(onClick = onBackClick) },
                    centerContent = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.str_d815091c),
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
            }
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                // Device Selector
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    val selectedDevice = state.devices.firstOrNull { it.id == state.selectedDeviceId }
                    DeviceSelectTrigger(
                        text = selectedDevice?.name ?: "انتخاب دستگاه",
                        onClick = { deviceSelectorOpen = true },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Date Filters & Column Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("امروز", "دیروز", "تاریخ سفارشی").forEach { filter ->
                            FilterChip(
                                text = filter,
                                isSelected = state.selectedDateFilter == filter,
                                onClick = { onDateFilterSelected(filter) }
                            )
                        }
                    }
                    
                    IconButton(onClick = onOpenColumnSelector) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = stringResource(R.string.str_68a2de5f),
                            tint = themedColor(light = Color(0xFF676C70), dark = Color(0xFF929292))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (state.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = themedColor(light = Color(0xFF307EF3), dark = Color(0xFF5F98EC)))
                    }
                } else if (state.reports.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = stringResource(R.string.str_fb6f9fa9), color = themedColor(light = Color.Gray, dark = Color(0xFFA0A0A0)), fontSize = 16.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = state.reports,
                            key = { report -> "${report.startPositionId}_${report.startTime}" }
                        ) { report ->
                            TripReportCard(
                                report = report,
                                selectedColumns = state.selectedColumns,
                                onResolveAddress = onResolveAddress
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                        }
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
                    devices = state.devices.toImmutable(),
                    selectedDeviceId = state.selectedDeviceId,
                    onDeviceClick = { deviceId ->
                        deviceSelectorOpen = false
                        onDeviceSelect(deviceId)
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

            if (state.showColumnSelector) {
                ColumnsSelectionBottomSheet(
                    options = TRIP_REPORT_COLUMNS,
                    selectedIds = state.selectedColumns,
                    onToggle = onToggleColumn,
                    onDismiss = onDismissColumnSelector
                )
            }

            // Custom Error Snackbar
            /*
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
            ) {
                androidx.compose.material3.SnackbarHost(
                    hostState = snackbarHostState,
                    snackbar = { data ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = themedColor(light = Color(0xFFFDECEA), dark = Color(0xFF380B05))),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = themedColor(light = Color(0xFFE55353), dark = Color(0xFF6F1111))
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = data.visuals.message,
                                    color = themedColor(light = Color(0xFFE55353), dark = Color(0xFF6F1111)),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                )
            }
            */
            } // Close Box
        } // Close Scaffold
    } // Close CompositionLocalProvider
} // Close TripReportsScreen

@Composable
fun TripReportCard(
    report: TripReportUiModel,
    selectedColumns: Set<String>,
    onResolveAddress: suspend (Double, Double) -> String
) {
    var startAddressText by remember(report) { mutableStateOf(report.startAddress) }
    var endAddressText by remember(report) { mutableStateOf(report.endAddress) }

    val showStartAddress = selectedColumns.contains("startAddress")
    val showEndAddress = selectedColumns.contains("endAddress")

    androidx.compose.runtime.LaunchedEffect(report, showStartAddress) {
        if (showStartAddress) {
            if (startAddressText.isNullOrEmpty() || startAddressText == "نامشخص") {
                startAddressText = "در حال دریافت..."
                startAddressText = onResolveAddress(report.startLat, report.startLon)
            }
        }
    }

    androidx.compose.runtime.LaunchedEffect(report, showEndAddress) {
        if (showEndAddress) {
            if (endAddressText.isNullOrEmpty() || endAddressText == "نامشخص") {
                endAddressText = "در حال دریافت..."
                endAddressText = onResolveAddress(report.endLat, report.endLon)
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = themedColor(light = Color.White, dark = Color(0xFF27343F))),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            
            if (selectedColumns.contains("distance")) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Route,
                        contentDescription = null,
                        tint = themedColor(light = Color(0xFF307EF3), dark = Color(0xFF5F98EC)),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(R.string.str_4190f2e7), color = themedColor(light = Color.Gray, dark = Color(0xFFA0A0A0)), fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${report.distance} کیلومتر",
                        color = themedColor(light = Color.Black, dark = Color(0xFFE0E0E0)),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                if (selectedColumns.contains("startTime")) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = stringResource(R.string.str_1ba8f1e1), color = themedColor(light = Color.Gray, dark = Color(0xFFA0A0A0)), fontSize = 10.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.AccessTime, contentDescription = null, tint = themedColor(light = Color(0xFF307EF3), dark = Color(0xFF5F98EC)), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = report.startTime, fontSize = 12.sp, color = themedColor(light = Color.Black, dark = Color(0xFFE0E0E0)), fontWeight = FontWeight.Medium)
                        }
                    }
                }
                if (selectedColumns.contains("endTime")) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = stringResource(R.string.str_00682caf), color = themedColor(light = Color.Gray, dark = Color(0xFFA0A0A0)), fontSize = 10.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.AccessTime, contentDescription = null, tint = themedColor(light = Color(0xFF307EF3), dark = Color(0xFF5F98EC)), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = report.endTime, fontSize = 12.sp, color = themedColor(light = Color.Black, dark = Color(0xFFE0E0E0)), fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (selectedColumns.contains("averageSpeed") || selectedColumns.contains("maxSpeed")) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    if (selectedColumns.contains("averageSpeed")) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = stringResource(R.string.str_0ddc0842), color = themedColor(light = Color.Gray, dark = Color(0xFFA0A0A0)), fontSize = 10.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = themedColor(light = Color(0xFFE5B850), dark = Color(0xFF6F5210)), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "${report.averageSpeed} کیلومتر بر ساعت", fontSize = 12.sp, color = themedColor(light = Color.DarkGray, dark = Color(0xFFB0B0B0)))
                            }
                        }
                    }
                    if (selectedColumns.contains("maxSpeed")) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = stringResource(R.string.str_8263bc39), color = themedColor(light = Color.Gray, dark = Color(0xFFA0A0A0)), fontSize = 10.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = themedColor(light = Color(0xFFE5B850), dark = Color(0xFF6F5210)), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "${report.maxSpeed} کیلومتر بر ساعت", fontSize = 12.sp, color = themedColor(light = Color.DarkGray, dark = Color(0xFFB0B0B0)))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (selectedColumns.contains("duration")) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Timer, contentDescription = null, tint = themedColor(light = Color.Gray, dark = Color(0xFFA0A0A0)), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "مدت زمان سفر: ${report.duration}", color = themedColor(light = Color.DarkGray, dark = Color(0xFFB0B0B0)), fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            if (selectedColumns.contains("spentFuel")) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.LocalGasStation, contentDescription = null, tint = themedColor(light = Color.Gray, dark = Color(0xFFA0A0A0)), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "سوخت مصرفی: ${report.spentFuel} لیتر", color = themedColor(light = Color.DarkGray, dark = Color(0xFFB0B0B0)), fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (selectedColumns.contains("startAddress")) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = themedColor(light = Color.Gray, dark = Color(0xFFA0A0A0)), modifier = Modifier.size(16.dp).padding(top = 2.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "مبدأ: ${startAddressText ?: "نامشخص"}", color = themedColor(light = Color.DarkGray, dark = Color(0xFFB0B0B0)), fontSize = 12.sp, lineHeight = 18.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (selectedColumns.contains("endAddress")) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = themedColor(light = Color.Gray, dark = Color(0xFFA0A0A0)), modifier = Modifier.size(16.dp).padding(top = 2.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "مقصد: ${endAddressText ?: "نامشخص"}", color = themedColor(light = Color.DarkGray, dark = Color(0xFFB0B0B0)), fontSize = 12.sp, lineHeight = 18.sp)
                }
            }
        }
    }
}


