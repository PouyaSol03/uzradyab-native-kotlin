package com.example.uzradyab.presentation.reports

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.uzradyab.domain.model.Device
import com.example.uzradyab.domain.model.CombinedReportItem
import com.example.uzradyab.domain.model.Event
import com.example.uzradyab.domain.model.Position
import com.example.uzradyab.map.tile.ExirFirmTileSource
import com.example.uzradyab.presentation.map.AppMenuDialog
import com.example.uzradyab.presentation.map.AppTopToolbar
import com.example.uzradyab.presentation.map.BackButton
import com.example.uzradyab.presentation.map.MenuGridButton
import org.osmdroid.config.Configuration
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private const val OSMDROID_PREFS = "osmdroid"

@Composable
fun ReportsRoute(
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onAddDeviceClick: () -> Unit,
    viewModel: ReportsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current

    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
        }
    }

    ReportsScreen(
        devices = viewModel.devices,
        selectedDeviceIds = viewModel.selectedDeviceIds,
        period = viewModel.period,
        customFromDate = viewModel.customFromDate,
        customToDate = viewModel.customToDate,
        isLoading = viewModel.isLoading,
        reportItems = viewModel.reportItems,
        onToggleDevice = viewModel::toggleDeviceSelection,
        onSelectAllDevices = viewModel::selectAllDevices,
        onClearDeviceSelection = viewModel::clearDeviceSelection,
        onPeriodChange = viewModel::onPeriodChange,
        onCustomFromDateChange = { viewModel.customFromDate = it },
        onCustomToDateChange = { viewModel.customToDate = it },
        onLoadReport = viewModel::loadReport,
        onBackClick = onBackClick,
        onLogoutClick = onLogoutClick,
        onAddDeviceClick = onAddDeviceClick
    )
}

@Composable
fun ReportsScreen(
    devices: List<Device>,
    selectedDeviceIds: Set<Long>,
    period: String,
    customFromDate: Date?,
    customToDate: Date?,
    isLoading: Boolean,
    reportItems: List<CombinedReportItem>,
    onToggleDevice: (Long) -> Unit,
    onSelectAllDevices: () -> Unit,
    onClearDeviceSelection: () -> Unit,
    onPeriodChange: (String) -> Unit,
    onCustomFromDateChange: (Date) -> Unit,
    onCustomToDateChange: (Date) -> Unit,
    onLoadReport: () -> Unit,
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onAddDeviceClick: () -> Unit,
) {
    val context = LocalContext.current
    val figmaBackground = Color(0xFFF3F4F6)
    var menuOpen by remember { mutableStateOf(false) }

    val allEvents = remember(reportItems) {
        reportItems.flatMap { item ->
            item.events.map { event ->
                val associatedPos = item.positions.find { it.id == event.positionId }
                EventReportUIItem(
                    event = event,
                    deviceName = devices.find { it.id == item.deviceId }?.name ?: "دستگاه ناشناس",
                    latitude = associatedPos?.latitude,
                    longitude = associatedPos?.longitude
                )
            }
        }.sortedByDescending { it.event.eventTime.orEmpty() }
    }

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
                    // Map view at the top taking 40% height if report data exists
                    if (reportItems.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                                .background(Color(0xFFE8F0F6))
                        ) {
                            ReportsMapView(
                                reportItems = reportItems,
                                devices = devices,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        // Filters Card
                        item {
                            FilterCard(
                                devices = devices,
                                selectedDeviceIds = selectedDeviceIds,
                                period = period,
                                customFromDate = customFromDate,
                                customToDate = customToDate,
                                isLoading = isLoading,
                                onToggleDevice = onToggleDevice,
                                onSelectAllDevices = onSelectAllDevices,
                                onClearDeviceSelection = onClearDeviceSelection,
                                onPeriodChange = onPeriodChange,
                                onCustomFromDateChange = onCustomFromDateChange,
                                onCustomToDateChange = onCustomToDateChange,
                                onLoadReport = onLoadReport
                            )
                        }

                        // Results List Title
                        if (allEvents.isNotEmpty()) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FilterList,
                                        contentDescription = null,
                                        tint = Color(0xFF333638),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "رویدادهای ثبت شده (${allEvents.size.toString().toPersianDigits()})",
                                        color = Color(0xFF333638),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            items(allEvents, key = { it.event.id }) { uiItem ->
                                EventReportRowItem(item = uiItem)
                            }
                        } else if (!isLoading) {
                            item {
                                EmptyStateItem()
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
            }
        }
    }
}

@Composable
private fun ReportsMapView(
    reportItems: List<CombinedReportItem>,
    devices: List<Device>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allPoints = remember(reportItems) {
        reportItems.flatMap { item ->
            item.route.map { GeoPoint(it.latitude, it.longitude) }
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            Configuration.getInstance().load(
                ctx,
                ctx.getSharedPreferences(OSMDROID_PREFS, Context.MODE_PRIVATE)
            )
            MapView(ctx).apply {
                setTileSource(ExirFirmTileSource())
                setMultiTouchControls(true)
                setBuiltInZoomControls(false)
                zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                setMinZoomLevel(3.0)
                setMaxZoomLevel(23.0)
            }
        },
        update = { mapView ->
            mapView.overlays.clear()

            val colors = listOf(
                android.graphics.Color.BLUE,
                android.graphics.Color.RED,
                android.graphics.Color.GREEN,
                android.graphics.Color.rgb(161, 40, 135), // purple
                android.graphics.Color.DKGRAY
            )

            reportItems.forEachIndexed { index, item ->
                if (item.route.isNotEmpty()) {
                    val points = item.route.map { GeoPoint(it.latitude, it.longitude) }
                    val polyline = Polyline(mapView).apply {
                        setPoints(points)
                        outlinePaint.color = colors[index % colors.size]
                        outlinePaint.strokeWidth = 6f
                    }
                    mapView.overlays.add(polyline)
                }

                item.events.forEach { event ->
                    val pos = item.positions.find { it.id == event.positionId }
                    if (pos != null) {
                        val marker = Marker(mapView).apply {
                            position = GeoPoint(pos.latitude, pos.longitude)
                            title = devices.find { it.id == item.deviceId }?.name ?: "دستگاه"
                            subDescription = "${eventTitle(event.type)} - ${formatEventTime(event.eventTime)}"
                            infoWindow = null
                        }
                        mapView.overlays.add(marker)
                    }
                }
            }

            if (allPoints.isNotEmpty()) {
                mapView.post {
                    try {
                        if (allPoints.size >= 2) {
                            val boundingBox = BoundingBox.fromGeoPoints(allPoints)
                            mapView.zoomToBoundingBox(boundingBox, false, 80)
                        } else {
                            mapView.controller.setZoom(16.0)
                            mapView.controller.setCenter(allPoints.first())
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("ReportsMapView", "Error zooming map to bounding box", e)
                        if (allPoints.isNotEmpty()) {
                            mapView.controller.setZoom(14.0)
                            mapView.controller.setCenter(allPoints.first())
                        }
                    }
                }
            }

            mapView.invalidate()
        }
    )
}

@Composable
private fun ReportsDeviceSelectTrigger(
    selectedCount: Int,
    devicesText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFDEE0E1), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.DirectionsCar,
                contentDescription = "Car",
                tint = Color(0xFF676C70),
                modifier = Modifier.size(width = 20.dp, height = 16.dp)
            )
            Text(
                text = if (selectedCount == 0) "انتخاب دستگاه‌ها" else devicesText,
                color = if (selectedCount == 0) Color(0xFF8F99A3) else Color(0xFF333638),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (selectedCount > 0) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFF307EF3).copy(alpha = 0.1f), CircleShape)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${selectedCount.toString().toPersianDigits()} دستگاه",
                        color = Color(0xFF307EF3),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Chevron Down",
                tint = Color(0xFF676C70),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun FilterCard(
    devices: List<Device>,
    selectedDeviceIds: Set<Long>,
    period: String,
    customFromDate: Date?,
    customToDate: Date?,
    isLoading: Boolean,
    onToggleDevice: (Long) -> Unit,
    onSelectAllDevices: () -> Unit,
    onClearDeviceSelection: () -> Unit,
    onPeriodChange: (String) -> Unit,
    onCustomFromDateChange: (Date) -> Unit,
    onCustomToDateChange: (Date) -> Unit,
    onLoadReport: () -> Unit
) {
    val context = LocalContext.current
    var deviceDialogOpen by remember { mutableStateOf(false) }
    var fromDatePickerOpen by remember { mutableStateOf(false) }
    var toDatePickerOpen by remember { mutableStateOf(false) }

    val selectedDevicesText = remember(selectedDeviceIds, devices) {
        val selectedNames = devices.filter { selectedDeviceIds.contains(it.id) }.map { it.name }
        if (selectedNames.isEmpty()) {
            "هیچ دستگاهی انتخاب نشده"
        } else if (selectedNames.size == 1) {
            selectedNames.first()
        } else {
            "${selectedNames.first()} و ${selectedNames.size - 1} دستگاه دیگر"
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Devices Title
            Text(
                text = "انتخاب دستگاه‌ها",
                color = Color(0xFF333638),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            // ReportsDeviceSelectTrigger (Styled like HomeMapScreen selector)
            ReportsDeviceSelectTrigger(
                selectedCount = selectedDeviceIds.size,
                devicesText = selectedDevicesText,
                onClick = { deviceDialogOpen = true }
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Time Period Title
            Text(
                text = "بازه زمانی گزارش",
                color = Color(0xFF333638),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            // Period Chips
            val periods = listOf(
                Pair("today", "امروز"),
                Pair("yesterday", "دیروز"),
                Pair("thisWeek", "هفته جاری"),
                Pair("thisMonth", "ماه جاری"),
                Pair("custom", "سفارشی")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                periods.forEach { (pKey, pLabel) ->
                    val isSelected = period == pKey
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp)
                            .background(
                                color = if (isSelected) Color(0xFF307EF3) else Color(0xFFF3F4F6),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .clickable { onPeriodChange(pKey) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = pLabel,
                            color = if (isSelected) Color.White else Color(0xFF333638),
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            // Custom Range Selection Dialog triggers
            if (period == "custom") {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // From Date Trigger
                        val fromLabel = customFromDate?.let { formatJalaliTime(it) } ?: "تاریخ شروع"
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFFDEE0E1), RoundedCornerShape(8.dp))
                                .clickable { fromDatePickerOpen = true }
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = Color(0xFF676C70),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "از: $fromLabel",
                                    color = Color(0xFF333638),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // To Date Trigger
                        val toLabel = customToDate?.let { formatJalaliTime(it) } ?: "تاریخ پایان"
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFFDEE0E1), RoundedCornerShape(8.dp))
                                .clickable { toDatePickerOpen = true }
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = Color(0xFF676C70),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "تا: $toLabel",
                                    color = Color(0xFF333638),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            // Submit Button
            Button(
                onClick = onLoadReport,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF307EF3),
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFF307EF3).copy(alpha = 0.5f)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "مشاهده گزارش ردیابی",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (deviceDialogOpen) {
        ReportsDeviceSelectDialog(
            devices = devices,
            selectedDeviceIds = selectedDeviceIds,
            onToggleDevice = onToggleDevice,
            onSelectAllDevices = onSelectAllDevices,
            onClearDeviceSelection = onClearDeviceSelection,
            onDismiss = { deviceDialogOpen = false }
        )
    }

    if (fromDatePickerOpen) {
        JalaliDatePickerDialog(
            initialDate = customFromDate ?: Date(),
            onDateSelected = { selectedDate ->
                val calendar = Calendar.getInstance().apply { time = customFromDate ?: Date() }
                TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        val finalCal = Calendar.getInstance().apply {
                            time = selectedDate
                            set(Calendar.HOUR_OF_DAY, hourOfDay)
                            set(Calendar.MINUTE, minute)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        onCustomFromDateChange(finalCal.time)
                        fromDatePickerOpen = false
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            },
            onDismiss = { fromDatePickerOpen = false }
        )
    }

    if (toDatePickerOpen) {
        JalaliDatePickerDialog(
            initialDate = customToDate ?: Date(),
            onDateSelected = { selectedDate ->
                val calendar = Calendar.getInstance().apply { time = customToDate ?: Date() }
                TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        val finalCal = Calendar.getInstance().apply {
                            time = selectedDate
                            set(Calendar.HOUR_OF_DAY, hourOfDay)
                            set(Calendar.MINUTE, minute)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        onCustomToDateChange(finalCal.time)
                        toDatePickerOpen = false
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            },
            onDismiss = { toDatePickerOpen = false }
        )
    }
}

@Composable
fun ReportsDeviceSelectDialog(
    devices: List<Device>,
    selectedDeviceIds: Set<Long>,
    onToggleDevice: (Long) -> Unit,
    onSelectAllDevices: () -> Unit,
    onClearDeviceSelection: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var search by remember { mutableStateOf("") }
    val query = search.trim()
    val filteredDevices = if (query.isBlank()) {
        devices
    } else {
        devices.filter { device ->
            device.name.contains(query, ignoreCase = true) || device.uniqueId.contains(query, ignoreCase = true)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .widthIn(max = 343.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "انتخاب دستگاه‌ها",
                    color = Color(0xFF333638),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )

                TextField(
                    value = search,
                    onValueChange = { search = it },
                    placeholder = {
                        Text(
                            text = "جستجو دستگاه...",
                            color = Color(0xFF8F99A3),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    },
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = Color(0xFF333638),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Right,
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF3F4F6),
                        unfocusedContainerColor = Color(0xFFF3F4F6),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "انتخاب همه",
                        color = Color(0xFF307EF3),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable(onClick = onSelectAllDevices)
                    )
                    Text(
                        text = "پاک کردن",
                        color = Color(0xFFE55353),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable(onClick = onClearDeviceSelection)
                    )
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredDevices, key = { it.id }) { device ->
                        val isSelected = selectedDeviceIds.contains(device.id)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .background(
                                    color = if (isSelected) Color(0xFF307EF3).copy(alpha = 0.1f) else Color.White,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) Color(0xFF307EF3) else Color(0xFFDEE0E1),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { onToggleDevice(device.id) }
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = device.name,
                                color = if (isSelected) Color(0xFF307EF3) else Color(0xFF333638),
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(
                                        color = if (isSelected) Color(0xFF307EF3) else Color.Transparent,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) Color(0xFF307EF3) else Color(0xFFDEE0E1),
                                        shape = RoundedCornerShape(4.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                    if (filteredDevices.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "هیچ دستگاهی یافت نشد.",
                                    color = Color(0xFF8F99A3),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF307EF3))
                ) {
                    Text(
                        text = "تایید",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun JalaliDatePickerDialog(
    initialDate: Date,
    onDateSelected: (Date) -> Unit,
    onDismiss: () -> Unit
) {
    val initialCal = Calendar.getInstance().apply { time = initialDate }
    val initialJalali = gregorianToJalali(
        initialCal.get(Calendar.YEAR),
        initialCal.get(Calendar.MONTH) + 1,
        initialCal.get(Calendar.DAY_OF_MONTH)
    )

    var currentYear by remember { mutableStateOf(initialJalali[0]) }
    var currentMonth by remember { mutableStateOf(initialJalali[1]) }
    var selectedDay by remember { mutableStateOf(initialJalali[2]) }

    val monthNames = listOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )

    val maxDays = remember(currentYear, currentMonth) {
        when {
            currentMonth <= 6 -> 31
            currentMonth in 7..11 -> 30
            currentMonth == 12 -> if (isJalaliLeapYear(currentYear)) 30 else 29
            else -> 30
        }
    }

    LaunchedEffect(maxDays) {
        if (selectedDay > maxDays) {
            selectedDay = maxDays
        }
    }

    val emptySlots = remember(currentYear, currentMonth) {
        val gFirst = jalaliToGregorian(currentYear, currentMonth, 1)
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, gFirst[0])
            set(Calendar.MONTH, gFirst[1] - 1)
            set(Calendar.DAY_OF_MONTH, gFirst[2])
        }
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        when (dayOfWeek) {
            Calendar.SATURDAY -> 0
            Calendar.SUNDAY -> 1
            Calendar.MONDAY -> 2
            Calendar.TUESDAY -> 3
            Calendar.WEDNESDAY -> 4
            Calendar.THURSDAY -> 5
            Calendar.FRIDAY -> 6
            else -> 0
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 340.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF307EF3), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "انتخاب تاریخ جلالی",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$selectedDay ${monthNames[currentMonth - 1]} $currentYear".toPersianDigits(),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFFF3F4F6), CircleShape)
                                .clickable {
                                    if (currentMonth == 1) {
                                        currentMonth = 12
                                        currentYear--
                                    } else {
                                        currentMonth--
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("<", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }

                        Text(
                            text = monthNames[currentMonth - 1],
                            color = Color(0xFF333638),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(64.dp),
                            textAlign = TextAlign.Center
                        )

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFFF3F4F6), CircleShape)
                                .clickable {
                                    if (currentMonth == 12) {
                                        currentMonth = 1
                                        currentYear++
                                    } else {
                                        currentMonth++
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(">", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFFF3F4F6), CircleShape)
                                .clickable { currentYear-- },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("-", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }

                        Text(
                            text = currentYear.toString().toPersianDigits(),
                            color = Color(0xFF333638),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(44.dp),
                            textAlign = TextAlign.Center
                        )

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFFF3F4F6), CircleShape)
                                .clickable { currentYear++ },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("ش", "ی", "د", "س", "چ", "پ", "ج").forEach { dayLabel ->
                        Text(
                            text = dayLabel,
                            color = Color(0xFF8F99A3),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val totalSlots = emptySlots + maxDays
                    val rowsCount = (totalSlots + 6) / 7
                    for (r in 0 until rowsCount) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            for (c in 0 until 7) {
                                val slotIndex = r * 7 + c
                                if (slotIndex < emptySlots || slotIndex >= totalSlots) {
                                    Spacer(modifier = Modifier.weight(1f))
                                } else {
                                    val dayNum = slotIndex - emptySlots + 1
                                    val isSelected = selectedDay == dayNum
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .background(
                                                color = if (isSelected) Color(0xFF307EF3) else Color.Transparent,
                                                shape = CircleShape
                                            )
                                            .clickable { selectedDay = dayNum },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = dayNum.toString().toPersianDigits(),
                                            color = if (isSelected) Color.White else Color(0xFF333638),
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF3F4F6), contentColor = Color(0xFF333638)),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("لغو", fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            val gDateArr = jalaliToGregorian(currentYear, currentMonth, selectedDay)
                            val finalCal = Calendar.getInstance().apply {
                                set(Calendar.YEAR, gDateArr[0])
                                set(Calendar.MONTH, gDateArr[1] - 1)
                                set(Calendar.DAY_OF_MONTH, gDateArr[2])
                            }
                            onDateSelected(finalCal.time)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF307EF3), contentColor = Color.White),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("تایید", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private data class EventReportUIItem(
    val event: Event,
    val deviceName: String,
    val latitude: Double?,
    val longitude: Double?
)

@Composable
private fun EventReportRowItem(item: EventReportUIItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val iconColor = remember(item.event.type) {
                when (item.event.type) {
                    "ignitionOn" -> Color(0xFF239F40) // Green
                    "ignitionOff" -> Color(0xFFE55353) // Red
                    "deviceOverspeed" -> Color(0xFFE5B850) // Yellow/Amber
                    "alarm" -> Color(0xFFE55353) // Red
                    else -> Color(0xFF307EF3) // Blue
                }
            }

            val icon = remember(item.event.type) {
                when (item.event.type) {
                    "ignitionOn", "ignitionOff" -> Icons.Default.DirectionsCar
                    "deviceOverspeed" -> Icons.Default.DirectionsCar
                    "alarm" -> Icons.Default.Notifications
                    else -> Icons.Default.Map
                }
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconColor.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "${item.deviceName} - ${eventTitle(item.event.type)}",
                    color = Color(0xFF333638),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatEventTime(item.event.eventTime),
                    color = Color(0xFF676C70),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal
                )
            }

            if (item.latitude != null && item.longitude != null) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFFF3F4F6), RoundedCornerShape(16.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "موقعیـت ثبت",
                        color = Color(0xFF676C70),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyStateItem() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = Color(0xFFBEC1C3),
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = "گزارشی برای نمایش وجود ندارد.",
            color = Color(0xFF676C70),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        Text(
            text = "دستگاه و بازه زمانی دلخواه را انتخاب کرده و دکمه مشاهده را کلیک کنید.",
            color = Color(0xFFBEC1C3),
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

private fun formatJalaliTime(date: Date): String {
    val cal = Calendar.getInstance().apply { time = date }
    val year = cal.get(Calendar.YEAR)
    val month = cal.get(Calendar.MONTH) + 1
    val day = cal.get(Calendar.DAY_OF_MONTH)
    val jalali = gregorianToJalali(year, month, day)
    
    val timeFormat = SimpleDateFormat("HH:mm", Locale.US).format(date)
    return "${jalali[0]}/${jalali[1].toString().padStart(2, '0')}/${jalali[2].toString().padStart(2, '0')} $timeFormat".toPersianDigits()
}

private fun formatEventTime(isoTime: String?): String {
    if (isoTime.isNullOrBlank()) return ""
    val parsed = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSX",
        "yyyy-MM-dd'T'HH:mm:ssX",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
    ).firstNotNullOfOrNull { pattern ->
        runCatching {
            SimpleDateFormat(pattern, Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.parse(isoTime)
        }.getOrNull()
    } ?: return isoTime

    val cal = Calendar.getInstance().apply { time = parsed }
    val year = cal.get(Calendar.YEAR)
    val month = cal.get(Calendar.MONTH) + 1
    val day = cal.get(Calendar.DAY_OF_MONTH)
    val jalali = gregorianToJalali(year, month, day)

    val timeFormat = SimpleDateFormat("HH:mm", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("Asia/Tehran")
    }.format(parsed)

    return "${jalali[0]}/${jalali[1].toString().padStart(2, '0')}/${jalali[2].toString().padStart(2, '0')} ساعت $timeFormat".toPersianDigits()
}

private fun gregorianToJalali(gy: Int, gm: Int, gd: Int): IntArray {
    val gDaysInMonth = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 335)
    val gy2 = if (gm > 2) gy + 1 else gy
    var gDays = 355666 + (365 * gy) + ((gy2 + 3) / 4) - ((gy2 + 99) / 100) + ((gy2 + 399) / 400) + gDaysInMonth[gm - 1] + gd
    var jy = -1595 + 33 * (gDays / 12053)
    gDays %= 12053
    jy += 4 * (gDays / 1461)
    gDays %= 1461
    if (gDays > 365) {
        jy += ((gDays - 1) / 365)
        gDays = (gDays - 1) % 365
    }
    val jm = if (gDays < 186) 1 + (gDays / 31) else 7 + ((gDays - 186) / 30)
    val jd = 1 + (if (gDays < 186) gDays % 31 else (gDays - 186) % 30)
    return intArrayOf(jy, jm, jd)
}

private fun jalaliToGregorian(jy: Int, jm: Int, jd: Int): IntArray {
    val jalaliYear = jy - 979
    val jalaliMonth = jm - 1
    val jalaliDay = jd - 1

    var jalaliDayNo = 365 * jalaliYear + (jalaliYear / 33) * 8 + (jalaliYear % 33 + 3) / 4
    for (i in 0 until jalaliMonth) {
        if (i < 6) {
            jalaliDayNo += 31
        } else {
            jalaliDayNo += 30
        }
    }
    jalaliDayNo += jalaliDay

    var gDayNo = jalaliDayNo + 79
    var gy = 1600 + 400 * (gDayNo / 146097)
    gDayNo %= 146097

    var leap = 1
    if (gDayNo >= 36525) {
        gDayNo--
        gy += 100 * (gDayNo / 36524)
        gDayNo %= 36524
        if (gDayNo >= 365) {
            gDayNo++
        } else {
            leap = 0
        }
    }

    gy += 4 * (gDayNo / 1461)
    gDayNo %= 1461

    if (gDayNo >= 366) {
        leap = 0
        gDayNo--
        gy += gDayNo / 365
        gDayNo %= 365
    }

    var gd = gDayNo + 1
    val gDaysInMonth = intArrayOf(0, 31, if (leap == 1 && (gy % 4 == 0 && gy % 100 != 0 || gy % 400 == 0)) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    var gm = 1
    while (gm <= 12 && gd > gDaysInMonth[gm]) {
        gd -= gDaysInMonth[gm]
        gm++
    }
    return intArrayOf(gy, gm, gd)
}

private fun isJalaliLeapYear(jy: Int): Boolean {
    val g = jalaliToGregorian(jy, 12, 30)
    val j = gregorianToJalali(g[0], g[1], g[2])
    return j[0] == jy && j[1] == 12 && j[2] == 30
}

private fun eventTitle(type: String): String {
    return when (type) {
        "ignitionOn" -> "روشن شدن موتور"
        "ignitionOff" -> "خاموش شدن موتور"
        "geofenceExit" -> "خروج از محدوده جغرافیایی"
        "geofenceEnter" -> "ورود به محدوده جغرافیایی"
        "deviceOverspeed" -> "سرعت غیر مجاز"
        "alarm" -> "هشدار دستگاه"
        else -> if (type.isBlank()) "رویداد ثبت شده" else type
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
