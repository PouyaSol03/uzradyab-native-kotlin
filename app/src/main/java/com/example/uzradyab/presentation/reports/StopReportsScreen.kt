package com.example.uzradyab.presentation.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.uzradyab.presentation.map.AppTopToolbar
import com.example.uzradyab.presentation.map.AppMenuDialog
import com.example.uzradyab.presentation.map.DeviceSelectDialog
import com.example.uzradyab.presentation.map.BackButton
import com.example.uzradyab.presentation.components.JalaliDateTime
import com.example.uzradyab.ui.theme.AppBlue
import com.example.uzradyab.ui.theme.AppTextPrimary
import com.example.uzradyab.core.utils.toImmutable
import com.example.uzradyab.core.utils.FormatUtils.toPersianDigits

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StopReportsRoute(
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onAddDeviceClick: () -> Unit,
    viewModel: StopReportsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    StopReportsScreen(
        state = state,
        onBackClick = onBackClick,
        onLogoutClick = onLogoutClick,
        onAddDeviceClick = onAddDeviceClick,
        onDeviceSelect = viewModel::onDeviceSelected,
        onDateFilterSelected = viewModel::onDateFilterSelected,
        onCustomDateSelected = viewModel::onCustomDateSelected,
        onApplyCustomDateRange = viewModel::applyCustomDateRange,
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
fun StopReportsScreen(
    state: StopReportsUiState,
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onAddDeviceClick: () -> Unit,
    onDeviceSelect: (Long) -> Unit,
    onDateFilterSelected: (String) -> Unit,
    onCustomDateSelected: (String, String) -> Unit,
    onApplyCustomDateRange: (JalaliDateTime?, JalaliDateTime?) -> Unit,
    onCustomDateDismiss: () -> Unit,
    onResolveAddress: suspend (Double, Double) -> String,
    onOpenColumnSelector: () -> Unit,
    onDismissColumnSelector: () -> Unit,
    onToggleColumn: (String) -> Unit,
    onClearError: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }
    var deviceSelectorOpen by remember { mutableStateOf(false) }
    val figmaBackground = Color(0xFFF9F9F9)

    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }

    androidx.compose.runtime.LaunchedEffect(state.error) {
        state.error?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = androidx.compose.material3.SnackbarDuration.Short
            )
            onClearError()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(figmaBackground)) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppTopToolbar(
                startContent = { BackButton(onClick = onBackClick) },
                centerContent = {
                    Text(
                        text = "گزارشات / توقف ها",
                        color = Color(0xFF676C70),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                    )
                },
                endContent = { 
                    com.example.uzradyab.presentation.map.MenuGridButton(onClick = { menuOpen = true }) 
                },
                modifier = Modifier
                    .statusBarsPadding()
                    .height(64.dp)
            )

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

            // Date Filters and Column Selector
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
                        contentDescription = "انتخاب ستون‌ها",
                        tint = Color(0xFF676C70)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF307EF3))
                }
            } else if (state.reports.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "توقفی یافت نشد", color = Color.Gray, fontSize = 16.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = state.reports,
                        key = { report -> report.startTime }
                    ) { report ->
                        StopReportCard(
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
                onApplyCustomRange = onApplyCustomDateRange
            )
        }

        if (state.showColumnSelector) {
            ColumnsSelectionBottomSheet(
                options = STOP_REPORT_COLUMNS,
                selectedIds = state.selectedColumns,
                onToggle = onToggleColumn,
                onDismiss = onDismissColumnSelector
            )
        }

        // Custom Error Snackbar
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
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFDECEA)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Info,
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
    }
}

@Composable
fun StopReportCard(
    report: StopReportUiModel,
    selectedColumns: Set<String>,
    onResolveAddress: suspend (Double, Double) -> String
) {
    var addressText by remember(report) { mutableStateOf(report.address) }

    androidx.compose.runtime.LaunchedEffect(report) {
        if (addressText.isNullOrEmpty() || addressText == "نامشخص") {
            addressText = "در حال دریافت..."
            addressText = onResolveAddress(report.latitude, report.longitude)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (selectedColumns.contains("duration")) {
                // Header: Duration
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = Color(0xFFE5B850),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "مدت توقف:",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = report.duration,
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Times
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                if (selectedColumns.contains("startTime")) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "شروع توقف", color = Color.Gray, fontSize = 10.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.AccessTime, contentDescription = null, tint = Color(0xFF307EF3), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = report.startTime, fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Medium)
                        }
                    }
                }
                if (selectedColumns.contains("endTime")) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "پایان توقف", color = Color.Gray, fontSize = 10.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.AccessTime, contentDescription = null, tint = Color(0xFF307EF3), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = report.endTime, fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedColumns.contains("engineHours")) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.SettingsSuggest, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "ساعات کارکرد موتور: ${report.engineHours}", color = Color.DarkGray, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            if (selectedColumns.contains("spentFuel")) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.LocalGasStation, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "سوخت مصرفی: ${report.spentFuel} لیتر", color = Color.DarkGray, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (selectedColumns.contains("address")) {
                // Address
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp).padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = addressText ?: "آدرس نامشخص",
                        color = Color.DarkGray,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}



@Composable
fun DeviceSelectTrigger(
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
            textAlign = androidx.compose.ui.text.style.TextAlign.Right,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
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
fun FilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(32.dp)
            .background(if (isSelected) AppBlue else Color.White, RoundedCornerShape(8.dp))
            .border(
                1.dp, 
                if (isSelected) AppBlue else Color(0xFFE3E8EE), 
                RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else AppTextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun CustomDateBottomSheet(
    onDismiss: () -> Unit,
    onApplyCustomRange: (com.example.uzradyab.presentation.components.JalaliDateTime?, com.example.uzradyab.presentation.components.JalaliDateTime?) -> Unit
) {
    val sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showStartPicker by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var showEndPicker by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var customStart by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<com.example.uzradyab.presentation.components.JalaliDateTime?>(null) }
    var customEnd by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<com.example.uzradyab.presentation.components.JalaliDateTime?>(null) }
    
    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFFF7F9FA)
    ) {
        androidx.compose.runtime.CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl) {
            if (showStartPicker) {
                com.example.uzradyab.presentation.components.JalaliDateTimePicker(
                    title = "انتخاب تاریخ و ساعت شروع",
                    initialDateTime = customStart,
                    onConfirm = { 
                        customStart = it
                        showStartPicker = false
                    },
                    onCancel = { showStartPicker = false }
                )
            } else if (showEndPicker) {
                com.example.uzradyab.presentation.components.JalaliDateTimePicker(
                    title = "انتخاب تاریخ و ساعت پایان",
                    initialDateTime = customEnd,
                    onConfirm = { 
                        customEnd = it
                        showEndPicker = false
                    },
                    onCancel = { showEndPicker = false }
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    androidx.compose.material3.Text(
                        text = "بازه زمانی دلخواه",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF384C5C),
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                    
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                        androidx.compose.material3.Text("تاریخ و ساعت شروع", fontSize = 14.sp, color = Color(0xFF6A8BA5), modifier = Modifier.padding(bottom = 8.dp))
                        androidx.compose.material3.OutlinedButton(
                            onClick = { showStartPicker = true },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = Color(0xFF384C5C)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC0CDD8))
                        ) {
                            androidx.compose.material3.Text(
                                if (customStart != null) "${customStart!!.year}/${customStart!!.month.toString().padStart(2, '0')}/${customStart!!.day.toString().padStart(2, '0')} ${customStart!!.hour.toString().padStart(2, '0')}:${customStart!!.minute.toString().padStart(2, '0')}".toPersianDigits() 
                                else "انتخاب کنید"
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                        androidx.compose.material3.Text("تاریخ و ساعت پایان", fontSize = 14.sp, color = Color(0xFF6A8BA5), modifier = Modifier.padding(bottom = 8.dp))
                        androidx.compose.material3.OutlinedButton(
                            onClick = { showEndPicker = true },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = Color(0xFF384C5C)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC0CDD8))
                        ) {
                            androidx.compose.material3.Text(
                                if (customEnd != null) "${customEnd!!.year}/${customEnd!!.month.toString().padStart(2, '0')}/${customEnd!!.day.toString().padStart(2, '0')} ${customEnd!!.hour.toString().padStart(2, '0')}:${customEnd!!.minute.toString().padStart(2, '0')}".toPersianDigits() 
                                else "انتخاب کنید"
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        androidx.compose.material3.Button(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFFEFF3F5), contentColor = Color(0xFF6A8BA5)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            androidx.compose.material3.Text("انصراف", fontSize = 16.sp)
                        }
                        androidx.compose.material3.Button(
                            onClick = {
                                if (customStart != null && customEnd != null) {
                                    onApplyCustomRange(customStart, customEnd)
                                }
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = AppBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            androidx.compose.material3.Text("ثبت", fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}
