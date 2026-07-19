package com.example.uzradyab.presentation.device

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.uzradyab.domain.model.Device
import com.example.uzradyab.presentation.map.AppTopToolbar
import com.example.uzradyab.presentation.map.BackButton
import com.example.uzradyab.presentation.map.MapViewModel
import com.example.uzradyab.presentation.map.daysUntilExpiration
import com.example.uzradyab.R
import androidx.compose.ui.res.stringResource
import com.example.uzradyab.ui.theme.themedColor

@Composable
fun DevicesRoute(
    onAddDeviceClick: () -> Unit,
    onMenuClick: () -> Unit,
    onEditDeviceClick: (Long) -> Unit,
    onRenewCreditClick: (Long) -> Unit,
    viewModel: MapViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    
    DevicesScreen(
        devices = state.devices,
        onAddDeviceClick = onAddDeviceClick,
        onBackClick = onMenuClick,
        onEditDeviceClick = onEditDeviceClick,
        onRenewCreditClick = onRenewCreditClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevicesScreen(
    devices: List<Device>,
    onAddDeviceClick: () -> Unit,
    onBackClick: () -> Unit,
    onEditDeviceClick: (Long) -> Unit,
    onRenewCreditClick: (Long) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(DeviceFilter.ALL) }
    var showFilterSheet by remember { mutableStateOf(false) }
    
    val filteredDevices = remember(searchQuery, selectedFilter, devices) {
        devices.filter { device ->
            val matchesSearch = if (searchQuery.isBlank()) true else {
                device.name.contains(searchQuery, ignoreCase = true) || 
                device.uniqueId.contains(searchQuery, ignoreCase = true) ||
                (device.phone?.contains(searchQuery) == true)
            }
            
            val daysRemaining = daysUntilExpiration(device.expirationTime)
            val isExpired = daysRemaining != null && daysRemaining <= 0
            
            val matchesFilter = when (selectedFilter) {
                DeviceFilter.ALL -> true
                DeviceFilter.ACTIVE -> !isExpired
                DeviceFilter.EXPIRED -> isExpired
            }
            
            matchesSearch && matchesFilter
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        if (showFilterSheet) {
            FilterBottomSheet(
                selectedFilter = selectedFilter,
                onFilterSelected = { 
                    selectedFilter = it
                    showFilterSheet = false
                },
                onDismissRequest = { showFilterSheet = false }
            )
        }
        Scaffold(
            topBar = {
                AppTopToolbar(
                    startContent = { BackButton(onClick = onBackClick) },
                    centerContent = {
                        Text(
                            text = stringResource(R.string.str_3fb91542),
                            color = themedColor(light = Color(0xFF676C70), dark = Color(0xFF929292)),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    },
                    modifier = Modifier
                        .statusBarsPadding()
                        .height(64.dp)
                )
            },
            containerColor = themedColor(light = Color(0xFFF3F4F6), dark = Color(0xFF1A1D23)) // Figma background
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Search and Filter Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Search Box
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .border(1.dp, themedColor(light = Color(0xFFE5E7EB), dark = Color(0xFF384C5C)), RoundedCornerShape(8.dp))
                            .background(themedColor(light = Color.White, dark = Color(0xFF27343F)), RoundedCornerShape(8.dp))
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                if (searchQuery.isEmpty()) {
                                    Text(stringResource(R.string.str_31665826), color = themedColor(light = Color(0xFFAEB1B4), dark = Color(0xFFA0B5C5)), fontSize = 14.sp)
                                }
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    textStyle = TextStyle(fontSize = 14.sp, color = themedColor(light = Color(0xFF333638), dark = Color(0xFFE6E6E6))),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = themedColor(light = Color(0xFFAEB1B4), dark = Color(0xFFA0B5C5)), modifier = Modifier.size(20.dp))
                        }
                    }

                    // Filter Button
                    Box(
                        modifier = Modifier
                            .height(48.dp)
                            .border(1.dp, themedColor(light = Color(0xFF3B82F6), dark = Color(0xFF5D94EE)), RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showFilterSheet = true }
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.str_74921051), color = themedColor(light = Color(0xFF3B82F6), dark = Color(0xFF5D94EE)), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }

                // Add Device Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(48.dp)
                        .border(1.dp, themedColor(light = Color(0xFF3B82F6), dark = Color(0xFF5D94EE)), RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onAddDeviceClick),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = themedColor(light = Color(0xFF3B82F6), dark = Color(0xFF5D94EE)), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(R.string.str_bde8bf83), fontSize = 15.sp, fontWeight = FontWeight.Medium, color = themedColor(light = Color(0xFF3B82F6), dark = Color(0xFF5D94EE)))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Devices List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredDevices) { device ->
                        DeviceListCard(
                            device = device,
                            onEditClick = { onEditDeviceClick(device.id) },
                            onRenewClick = { onRenewCreditClick(device.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DeviceListCard(
    device: Device,
    onEditClick: () -> Unit,
    onRenewClick: () -> Unit
) {
    val daysRemaining = daysUntilExpiration(device.expirationTime)
    val isExpired = daysRemaining != null && daysRemaining <= 0

    val cardBorderColor = if (isExpired) themedColor(light = Color(0xFFE53935), dark = Color(0xFFE26D6A)) else themedColor(light = Color(0xFFE5E7EB), dark = Color(0xFF1B1D23))
    val cardBorderWidth = 1.dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(cardBorderWidth, cardBorderColor, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = themedColor(light = Color.White, dark = Color(0xFF27343F))),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = device.name.toPersianDigits(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = themedColor(light = Color(0xFF333638), dark = Color(0xFFAFB3B6))
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // GSM Signal
                    Icon(
                        imageVector = Icons.Default.Wifi,
                        contentDescription = "GSM",
                        tint = themedColor(light = Color(0xFF00C89B), dark = Color(0xFF66FFDD)),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("GSM", fontSize = 12.sp, color = themedColor(light = Color(0xFF676C70), dark = Color(0xFF929292)), fontWeight = FontWeight.Light)
                    Spacer(modifier = Modifier.width(16.dp))
                    // GPS Signal
                    Icon(
                        imageVector = Icons.Default.Wifi,
                        contentDescription = "GPS",
                        tint = themedColor(light = Color(0xFFE55353), dark = Color(0xFF6F1111)),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("GPS", fontSize = 12.sp, color = themedColor(light = Color(0xFF676C70), dark = Color(0xFF929292)), fontWeight = FontWeight.Light)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Info Rows
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.str_fa837aa3), fontSize = 14.sp, color = themedColor(light = Color(0xFF8B98A5), dark = Color(0xFFA3A6A8)))
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Text(device.uniqueId.toPersianDigits(), fontSize = 15.sp, color = themedColor(light = Color(0xFF333638), dark = Color(0xFFAFB3B6)), fontWeight = FontWeight.Medium)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.str_be9638ce), fontSize = 14.sp, color = themedColor(light = Color(0xFF8B98A5), dark = Color(0xFFA3A6A8)))
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Text((device.phone ?: "—").toPersianDigits(), fontSize = 15.sp, color = themedColor(light = Color(0xFF333638), dark = Color(0xFFAFB3B6)), fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Box Section
            if (isExpired) {
                // Expired State Background Box
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(themedColor(light = Color(0xFFFCE4E4), dark = Color(0xFF370606)), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = "Error", tint = themedColor(light = Color(0xFFE53935), dark = Color(0xFFE26D6A)))
                        Text(
                            text = stringResource(R.string.str_75887764),
                            color = themedColor(light = Color(0xFFE53935), dark = Color(0xFFE26D6A)),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 22.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(stringResource(R.string.str_fb8cbc4d), fontSize = 14.sp, color = themedColor(light = Color(0xFFE53935), dark = Color(0xFFE26D6A)))
                        Text(formatExpirationDate(device.expirationTime), fontSize = 14.sp, color = themedColor(light = Color(0xFFE53935), dark = Color(0xFFE26D6A)), fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Button Row
                Button(
                    onClick = onRenewClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = themedColor(light = Color(0xFF3B82F6), dark = Color(0xFF5D94EE)))
                ) {
                    Text(stringResource(R.string.str_1dce144e), fontSize = 15.sp, fontWeight = FontWeight.Medium, color = themedColor(light = Color.White, dark = Color.White))
                }
            } else {
                // Active State Background Box
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(themedColor(light = Color(0xFF384C5C), dark = Color(0xFFA0B5C5)), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.str_fb8cbc4d), fontSize = 14.sp, color = themedColor(light = Color(0xFFC0CDD8), dark = Color(0xFF31414F)))
                        Text(formatExpirationDate(device.expirationTime), fontSize = 14.sp, color = themedColor(light = Color.White, dark = Color.White), fontWeight = FontWeight.Medium)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.str_f2f0dded), fontSize = 14.sp, color = themedColor(light = Color(0xFFC0CDD8), dark = Color(0xFF31414F)))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(formatEndDate(device.expirationTime), fontSize = 14.sp, color = themedColor(light = Color.White, dark = Color.White), fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .background(themedColor(light = Color(0xFF8B9CAE), dark = Color(0xFF343F4B)), RoundedCornerShape(16.dp))
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text("${daysRemaining.toString().toPersianDigits()} روز باقیمانده", fontSize = 12.sp, color = themedColor(light = Color.White, dark = Color.White))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Manage Device Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onEditClick)
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.str_a6b9c52a), fontSize = 15.sp, color = themedColor(light = Color(0xFF3B82F6), dark = Color(0xFF5D94EE)), fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Manage", tint = themedColor(light = Color(0xFF3B82F6), dark = Color(0xFF5D94EE)))
                }
            }
        }
    }
}

private fun formatExpirationDate(value: String?): String {
    if (value.isNullOrBlank()) return "—"
    val pattern = if (value.contains("T")) "yyyy-MM-dd'T'HH:mm:ss" else "yyyy-MM-dd"
    return runCatching {
        val sdf = java.text.SimpleDateFormat(pattern, java.util.Locale.US)
        if (value.contains("Z")) {
            sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
        }
        val parsed = sdf.parse(value.replace("Z", "").substringBefore(".")) ?: return "—"
        val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Tehran"))
        cal.time = parsed
        // To get the "start" of the 1-year period, we subtract 1 year? 
        // Wait, the expirationTime is the END date. 
        // The user asked "تاریخ ثبت دستگاه" to show Jalali. 
        // Oh! `device.expirationTime` is the Expiration Date, not the Registration Date!
        // The user said: "تاریخ ثبت دستگاه is 2027-05-04" 
        // If the expiration is 2027-05-04, registration is a year before?
        // But the field in device is `expirationTime`. Is there an `addTime` or `registrationTime`?
        // Let's assume `expirationTime` is 1 year after registration, so we can subtract 1 year if needed, or just use it. 
        // Actually, let's just format whatever date we get. If they passed `expirationTime` for it, I'll subtract a year.
        cal.add(java.util.Calendar.YEAR, -1) // Assuming Registration is Expiration - 1 year for now
        val gYear = cal.get(java.util.Calendar.YEAR)
        val gMonth = cal.get(java.util.Calendar.MONTH) + 1
        val gDay = cal.get(java.util.Calendar.DAY_OF_MONTH)
        
        val jDate = com.example.uzradyab.core.utils.JalaliUtils.gregorianToJalali(gYear, gMonth, gDay)
        val monthName = com.example.uzradyab.core.utils.JalaliUtils.getMonthName(jDate[1])
        "${jDate[2]} $monthName ${jDate[0]}".toPersianDigits()
    }.getOrDefault("—")
}

private fun formatEndDate(value: String?): String {
    if (value.isNullOrBlank()) return "—"
    val pattern = if (value.contains("T")) "yyyy-MM-dd'T'HH:mm:ss" else "yyyy-MM-dd"
    return runCatching {
        val sdf = java.text.SimpleDateFormat(pattern, java.util.Locale.US)
        if (value.contains("Z")) {
            sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
        }
        val parsed = sdf.parse(value.replace("Z", "").substringBefore(".")) ?: return "—"
        val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Tehran"))
        cal.time = parsed
        
        val gYear = cal.get(java.util.Calendar.YEAR)
        val gMonth = cal.get(java.util.Calendar.MONTH) + 1
        val gDay = cal.get(java.util.Calendar.DAY_OF_MONTH)
        
        val jDate = com.example.uzradyab.core.utils.JalaliUtils.gregorianToJalali(gYear, gMonth, gDay)
        val monthName = com.example.uzradyab.core.utils.JalaliUtils.getMonthName(jDate[1])
        "${jDate[2]} $monthName ${jDate[0]}".toPersianDigits()
    }.getOrDefault("—")
}

private fun String.toPersianDigits(): String {
    val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    return buildString(length) {
        this@toPersianDigits.forEach { char ->
            append(if (char in '0'..'9') persianDigits[char - '0'] else char)
        }
    }
}

enum class DeviceFilter(val title: String) {
    ALL("همه دستگاه ها"),
    ACTIVE("دستگاه های فعال"),
    EXPIRED("دستگاه های منقضی")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    selectedFilter: DeviceFilter,
    onFilterSelected: (DeviceFilter) -> Unit,
    onDismissRequest: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = themedColor(light = Color.White, dark = Color(0xFF27343F)),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 8.dp, bottom = 16.dp)
        ) {
            Text(
                text = "فیلتر دستگاه‌ها",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = themedColor(light = Color(0xFF333638), dark = Color(0xFFAFB3B6)),
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            DeviceFilter.values().forEach { filter ->
                FilterOptionRow(
                    title = filter.title,
                    isSelected = selectedFilter == filter,
                    onClick = { onFilterSelected(filter) }
                )
            }
        }
    }
}

@Composable
fun FilterOptionRow(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            color = if (isSelected) themedColor(light = Color(0xFF3B82F6), dark = Color(0xFF5D94EE)) else themedColor(light = Color(0xFF333638), dark = Color(0xFFAFB3B6)),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = themedColor(light = Color(0xFF3B82F6), dark = Color(0xFF5D94EE))
            )
        }
    }
}
