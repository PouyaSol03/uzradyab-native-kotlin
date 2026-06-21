package com.example.uzradyab.presentation.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.uzradyab.domain.model.TripReport
import com.example.uzradyab.presentation.map.AppMenuDialog
import com.example.uzradyab.presentation.map.AppTopToolbar
import com.example.uzradyab.presentation.map.BackButton
import com.example.uzradyab.presentation.map.DeviceSelectDialog
import com.example.uzradyab.presentation.map.MenuGridButton
import com.example.uzradyab.presentation.components.JalaliDateTime
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

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
        onToggleColumn = viewModel::toggleColumn
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
    onToggleColumn: (String) -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }
    var deviceSelectorOpen by remember { mutableStateOf(false) }
    val figmaBackground = Color(0xFFF3F4F6)

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
                                text = "گزارشات / مسافت‌ها",
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
            }
        ) { innerPadding ->
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
                        Text(text = "مسافتی یافت نشد", color = Color.Gray, fontSize = 16.sp)
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
                    devices = state.devices,
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
        }
    }
}

@Composable
fun TripReportCard(
    report: TripReport,
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            
            if (selectedColumns.contains("distance")) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Route,
                        contentDescription = null,
                        tint = Color(0xFF307EF3),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "مسافت:", color = Color.Gray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${String.format(Locale.US, "%.2f", report.distance / 1000).toPersianDigits()} کیلومتر",
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                if (selectedColumns.contains("startTime")) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "شروع حرکت", color = Color.Gray, fontSize = 10.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.AccessTime, contentDescription = null, tint = Color(0xFF307EF3), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = formatIsoTime(report.startTime), fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Medium)
                        }
                    }
                }
                if (selectedColumns.contains("endTime")) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "پایان حرکت", color = Color.Gray, fontSize = 10.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.AccessTime, contentDescription = null, tint = Color(0xFF307EF3), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = formatIsoTime(report.endTime), fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (selectedColumns.contains("averageSpeed") || selectedColumns.contains("maxSpeed")) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    if (selectedColumns.contains("averageSpeed")) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "میانگین سرعت", color = Color.Gray, fontSize = 10.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = Color(0xFFE5B850), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "${String.format(Locale.US, "%.0f", report.averageSpeed * 1.852).toPersianDigits()} کیلومتر بر ساعت", fontSize = 12.sp, color = Color.DarkGray)
                            }
                        }
                    }
                    if (selectedColumns.contains("maxSpeed")) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "حداکثر سرعت", color = Color.Gray, fontSize = 10.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = Color(0xFFE5B850), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "${String.format(Locale.US, "%.0f", report.maxSpeed * 1.852).toPersianDigits()} کیلومتر بر ساعت", fontSize = 12.sp, color = Color.DarkGray)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (selectedColumns.contains("duration")) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Timer, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "مدت زمان سفر: ${formatDuration(report.duration)}", color = Color.DarkGray, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            if (selectedColumns.contains("spentFuel")) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.LocalGasStation, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "سوخت مصرفی: ${String.format(Locale.US, "%.1f", report.spentFuel).toPersianDigits()} لیتر", color = Color.DarkGray, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (selectedColumns.contains("startAddress")) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp).padding(top = 2.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "مبدأ: ${startAddressText ?: "نامشخص"}", color = Color.DarkGray, fontSize = 12.sp, lineHeight = 18.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (selectedColumns.contains("endAddress")) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp).padding(top = 2.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "مقصد: ${endAddressText ?: "نامشخص"}", color = Color.DarkGray, fontSize = 12.sp, lineHeight = 18.sp)
                }
            }
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalMinutes = durationMs / 60000
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    
    val hStr = if (hours > 0) "$hours ساعت " else ""
    val mStr = if (minutes > 0 || hours == 0L) "$minutes دقیقه" else ""
    val andStr = if (hours > 0 && minutes > 0) "و " else ""
    
    return "$hStr$andStr$mStr".toPersianDigits()
}

private fun formatIsoTime(isoString: String?): String {
    if (isoString.isNullOrEmpty()) return "نامشخص"
    return try {
        val formatIn = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = formatIn.parse(isoString.substring(0, 19)) ?: return isoString
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tehran")).apply { time = date }
        
        val h = cal.get(Calendar.HOUR_OF_DAY).toString().padStart(2, '0')
        val min = cal.get(Calendar.MINUTE).toString().padStart(2, '0')
        
        val gY = cal.get(Calendar.YEAR)
        val gM = cal.get(Calendar.MONTH) + 1
        val gD = cal.get(Calendar.DAY_OF_MONTH)
        val jDate = com.example.uzradyab.core.utils.JalaliUtils.gregorianToJalali(gY, gM, gD)
        
        val y = jDate[0]
        val m = jDate[1].toString().padStart(2, '0')
        val d = jDate[2].toString().padStart(2, '0')
        
        "$h:$min | $y/$m/$d".toPersianDigits()
    } catch (e: Exception) {
        isoString
    }
}

private fun String.toPersianDigits(): String {
    val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    return buildString(length) {
        this@toPersianDigits.forEach { char ->
            append(if (char in '0'..'9') persianDigits[char - '0'] else char)
        }
    }
}
