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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import com.example.uzradyab.domain.model.StopReport
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

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
        onDeviceSelected = viewModel::onDeviceSelected,
        onDateFilterSelected = viewModel::onDateFilterSelected,
        onCustomDateSelected = viewModel::onCustomDateSelected,
        onDismissCustomDatePicker = viewModel::dismissCustomDatePicker
    )
}

@Composable
fun StopReportsScreen(
    state: StopReportsUiState,
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onAddDeviceClick: () -> Unit,
    onDeviceSelected: (Long) -> Unit,
    onDateFilterSelected: (String) -> Unit,
    onCustomDateSelected: (String, String) -> Unit,
    onDismissCustomDatePicker: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }
    var deviceSelectorOpen by remember { mutableStateOf(false) }
    val figmaBackground = Color(0xFFF9F9F9)

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

            // Date Filters
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
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
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.reports) { report ->
                        StopReportCard(report)
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
                    onDeviceSelected(deviceId)
                },
                onDismiss = { deviceSelectorOpen = false }
            )
        }
    }
}

@Composable
fun StopReportCard(report: StopReport) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                    text = formatDuration(report.duration),
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Times
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = "شروع توقف", color = Color.Gray, fontSize = 10.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.AccessTime, contentDescription = null, tint = Color(0xFF307EF3), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = formatIsoTime(report.startTime), fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Medium)
                    }
                }
                Column {
                    Text(text = "پایان توقف", color = Color.Gray, fontSize = 10.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.AccessTime, contentDescription = null, tint = Color(0xFF307EF3), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = formatIsoTime(report.endTime), fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

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
                    text = report.address ?: "آدرس نامشخص",
                    color = Color.DarkGray,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
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

private fun formatIsoTime(isoString: String): String {
    return try {
        val formatIn = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = formatIn.parse(isoString.substring(0, 19))
        val formatOut = SimpleDateFormat("HH:mm | yyyy/MM/dd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("Asia/Tehran")
        }
        formatOut.format(date!!).toPersianDigits()
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
private fun FilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) Color(0xFF307EF3) else Color.White
    val textColor = if (isSelected) Color.White else Color.Gray

    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(16.dp))
            .border(
                1.dp, 
                if (isSelected) Color.Transparent else Color(0xFFE3E8EE), 
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
