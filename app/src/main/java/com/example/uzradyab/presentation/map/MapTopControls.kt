package com.example.uzradyab.presentation.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.uzradyab.core.utils.DeviceSearch
import com.example.uzradyab.core.utils.FormatUtils
import com.example.uzradyab.core.utils.ImmutableListWrapper
import com.example.uzradyab.presentation.device.DeviceSearchViewModel
import com.example.uzradyab.domain.model.Device
import com.example.uzradyab.domain.model.Position
import kotlin.math.roundToInt

import com.example.uzradyab.R
import androidx.compose.ui.res.stringResource
import com.example.uzradyab.ui.theme.UzradyabTheme
import com.example.uzradyab.ui.theme.themedColor

/**
 * Accounts with exactly one device (already selected) don't need a device picker.
 * The first devices page holds up to 30 items, so a size of 1 means the account has a single device.
 * A stale selection keeps the picker visible so the user can still fix it.
 */
fun isSingleDeviceAccount(devices: List<Device>, selectedDeviceId: Long?): Boolean =
    devices.size == 1 && devices.first().id == selectedDeviceId

@Composable
fun MapTopControls(
    devices: ImmutableListWrapper<Device>,
    selectedDeviceId: Long?,
    latestEvent: MapLatestEventItem?,
    isMapLocked: Boolean,
    showLockWarning: Boolean,
    onDeviceSelectorClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onEventsClick: () -> Unit,
    onLockToggleClick: () -> Unit,
    isSettingsEnabled: Boolean = true,
    selectedPosition: Position? = null,
    modifier: Modifier = Modifier,
) {
    val selectedDevice = devices.firstOrNull { it.id == selectedDeviceId }

    if (isSingleDeviceAccount(devices, selectedDeviceId) && selectedDevice != null) {
        SingleDeviceTopControls(
            device = selectedDevice,
            position = selectedPosition,
            latestEvent = latestEvent,
            isMapLocked = isMapLocked,
            showLockWarning = showLockWarning,
            onSettingsClick = onSettingsClick,
            onEventsClick = onEventsClick,
            onLockToggleClick = onLockToggleClick,
            isSettingsEnabled = isSettingsEnabled,
            modifier = modifier,
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 343.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DeviceSelectTrigger(
                text = selectedDevice?.name ?: "انتخاب دستگاه",
                onClick = onDeviceSelectorClick,
                modifier = Modifier.weight(1f),
            )
            MapSettingsTrigger(
                onClick = onSettingsClick,
                enabled = isSettingsEnabled,
                modifier = Modifier.width(142.dp),
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 343.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LatestEventsTicker(
                latestEvent = latestEvent,
                modifier = Modifier.weight(1f),
            )
            NotificationButton(onClick = onEventsClick)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 343.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            LockButton(
                isLocked = isMapLocked,
                showWarning = showLockWarning,
                onClick = onLockToggleClick
            )
        }
    }
}

@Composable
private fun SingleDeviceTopControls(
    device: Device,
    position: Position?,
    latestEvent: MapLatestEventItem?,
    isMapLocked: Boolean,
    showLockWarning: Boolean,
    onSettingsClick: () -> Unit,
    onEventsClick: () -> Unit,
    onLockToggleClick: () -> Unit,
    isSettingsEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SingleDeviceIdentityCard(
            device = device,
            position = position,
            latestEvent = latestEvent,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 343.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 343.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.Bottom,
        ) {
            // In RTL the first child sits on the right, so the warning chip appears beside the rail's lock button.
            AnimatedVisibility(
                visible = showLockWarning,
                enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandHorizontally(),
                exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkHorizontally(),
            ) {
                Text(
                    text = stringResource(R.string.str_00215df2),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    modifier = Modifier
                        .padding(end = 8.dp, bottom = 10.dp)
                        .background(UzradyabTheme.colors.primary, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                )
            }
            SingleDeviceActionRail(
                onSettingsClick = onSettingsClick,
                onEventsClick = onEventsClick,
                isMapLocked = isMapLocked,
                onLockToggleClick = onLockToggleClick,
                isSettingsEnabled = isSettingsEnabled,
            )
        }
    }
}

@Composable
private fun SingleDeviceIdentityCard(
    device: Device,
    position: Position?,
    latestEvent: MapLatestEventItem?,
    modifier: Modifier = Modifier,
) {
    val avatarColor = themedColor(light = Color(0xFFA12887), dark = Color(0xFFE184CD))
    val speedKmh = position?.let { (it.speed * 1.852).roundToInt() }

    Row(
        modifier = modifier
            .shadow(18.dp, RoundedCornerShape(16.dp), clip = false)
            .background(themedColor(light = Color.White, dark = Color(0xFF27343F)), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(avatarColor.copy(alpha = 0.16f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(avatarColor, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = themedColor(light = Color.White, dark = Color(0xFF27343F)),
                    modifier = Modifier.size(22.dp),
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = device.name,
                    color = UzradyabTheme.colors.textPrimary,
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (speedKmh != null) {
                    MotionPill(speedKmh = speedKmh, color = avatarColor)
                }
                DeviceStatusLabel(status = device.status)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                EventPulseDot()
                Text(
                    text = latestEvent?.text ?: "داده‌ای موجود نیست",
                    color = themedColor(light = Color(0xFF4B5563), dark = Color(0xFFC0CDD8)),
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                latestEvent?.timeText?.let { time ->
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Text(
                            text = time,
                            color = UzradyabTheme.colors.primary,
                            fontSize = 11.sp,
                            lineHeight = 18.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MotionPill(speedKmh: Int, color: Color) {
    val text = if (speedKmh <= 0) "متوقف" else with(FormatUtils) { "${speedKmh.toString().toPersianDigits()} km/h" }
    Text(
        text = text,
        color = color,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        modifier = Modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 2.dp),
    )
}

@Composable
private fun DeviceStatusLabel(status: String) {
    val color = when (status) {
        "online" -> themedColor(light = Color(0xFF22A566), dark = Color(0xFF00C89B))
        "offline" -> themedColor(light = Color(0xFFE45353), dark = Color(0xFFE55353))
        else -> themedColor(light = Color(0xFF8A98A8), dark = Color(0xFF97ADBF))
    }
    val text = when (status) {
        "online" -> stringResource(R.string.str_6104d0f3)
        "offline" -> stringResource(R.string.str_7717f8af)
        else -> stringResource(R.string.str_807fb3de)
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .background(color, CircleShape),
        )
        Text(
            text = text,
            color = color,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
        )
    }
}

@Composable
private fun SingleDeviceActionRail(
    onSettingsClick: () -> Unit,
    onEventsClick: () -> Unit,
    isMapLocked: Boolean,
    onLockToggleClick: () -> Unit,
    isSettingsEnabled: Boolean,
) {
    val railShape = RoundedCornerShape(12.dp)
    val surfaceColor = themedColor(light = Color.White, dark = Color(0xFF27343F))
    val dividerColor = themedColor(light = Color(0xFFEEF0F3), dark = Color(0xFF384C5C))
    Column(
        modifier = Modifier
            .width(48.dp)
            .shadow(18.dp, railShape, clip = false)
            .background(surfaceColor, railShape)
            .clip(railShape),
    ) {
        val settingsBackground = if (isSettingsEnabled) UzradyabTheme.colors.primary else themedColor(light = Color(0xFFE0E0E0), dark = Color(0xFF262626))
        val settingsTint = if (isSettingsEnabled) Color.White else themedColor(light = Color.Gray, dark = Color(0xFFA0A0A0))
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(settingsBackground)
                .let { if (isSettingsEnabled) it.clickable(onClick = onSettingsClick) else it },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Layers,
                contentDescription = stringResource(R.string.str_c9c1b29c),
                tint = settingsTint,
                modifier = Modifier.size(22.dp),
            )
        }
        Box(
            modifier = Modifier
                .size(48.dp)
                .clickable(onClick = onEventsClick),
            contentAlignment = Alignment.Center,
        ) {
            NotificationBellIcon(color = UzradyabTheme.colors.primary, size = 24)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(dividerColor),
        )
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(if (isMapLocked) UzradyabTheme.colors.primary else surfaceColor)
                .clickable(onClick = onLockToggleClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (isMapLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                contentDescription = "Lock Map",
                tint = if (isMapLocked) Color.White else UzradyabTheme.colors.primary,
                modifier = Modifier.size(22.dp),
            )
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
            .height(40.dp)
            .shadow(18.dp, RoundedCornerShape(8.dp), clip = false)
            .background(themedColor(light = Color.White, dark = Color(0xFF27343F)), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SmallCarIcon(color = themedColor(light = Color.Black, dark = Color(0xFFE0E0E0)))
        Text(
            text = text,
            color = themedColor(light = Color.Black, dark = Color(0xFFE0E0E0)),
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
        ChevronDownIcon(color = UzradyabTheme.colors.textPrimary)
    }
}

@Composable
private fun MapSettingsTrigger(
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = if (enabled) UzradyabTheme.colors.primary else themedColor(light = Color(0xFFE0E0E0), dark = Color(0xFF262626))
    val contentColor = if (enabled) themedColor(light = Color.White, dark = Color.White) else themedColor(light = Color.Gray, dark = Color(0xFFA0A0A0))

    Row(
        modifier = modifier
            .height(40.dp)
            .shadow(if (enabled) 18.dp else 0.dp, RoundedCornerShape(8.dp), clip = false)
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .let { if (enabled) it.clickable(onClick = onClick) else it }
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingsGearIcon(size = 18, color = contentColor)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.str_c9c1b29c),
            color = contentColor,
            fontSize = 14.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
        )
    }
}

@Composable
private fun LatestEventsTicker(
    latestEvent: MapLatestEventItem?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(40.dp)
            .shadow(18.dp, RoundedCornerShape(8.dp), clip = false)
            .background(themedColor(light = Color.White, dark = Color(0xFF27343F)), RoundedCornerShape(8.dp))
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EventPulseDot()
        Spacer(modifier = Modifier.width(10.dp))
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.End,
        ) {
            Text(
                text = latestEvent?.text ?: "داده‌ای موجود نیست",
                color = themedColor(light = Color(0xFF1C262E), dark = Color(0xFFC0CDD8)),
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Right,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )
            latestEvent?.timeText?.let { time ->
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Text(
                        text = time,
                        color = UzradyabTheme.colors.primary,
                        fontSize = 10.sp,
                        lineHeight = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .shadow(18.dp, RoundedCornerShape(8.dp), clip = false)
            .background(UzradyabTheme.colors.primary, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        NotificationBellIcon()
    }
}

@Composable
private fun LockButton(isLocked: Boolean, showWarning: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(40.dp)
            .widthIn(min = 40.dp)
            .shadow(18.dp, RoundedCornerShape(8.dp), clip = false)
            .background(if (isLocked) UzradyabTheme.colors.primary else themedColor(light = Color.White, dark = Color(0xFF27343F)), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            androidx.compose.animation.AnimatedVisibility(
                visible = showWarning,
                enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandHorizontally(),
                exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkHorizontally()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.str_00215df2),
                        color = themedColor(light = Color.White, dark = Color.White),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
            Icon(
                imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                contentDescription = "Lock Map",
                tint = if (isLocked) themedColor(light = Color.White, dark = Color.White) else UzradyabTheme.colors.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun MapSettingsDialog(
    currentStyle: String,
    onDismiss: () -> Unit,
    onSaveStyle: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedStyle by rememberSaveable { mutableStateOf(currentStyle) }
    val options = listOf(
        MapStyleOption("osm", "نقشه"),
        MapStyleOption("googleSatellite", "ماهواره‌ای گوگل"),
        MapStyleOption("googleRoad", "جاده گوگل"),
        MapStyleOption("carto", "اکسیر"),
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = themedColor(light = Color.White, dark = Color(0xFF27343F))),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.Start,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SettingsGearIcon(size = 22, color = UzradyabTheme.colors.textMuted)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.str_c9c1b29c),
                        color = UzradyabTheme.colors.textPrimary,
                        fontSize = 18.sp,
                        lineHeight = 28.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = stringResource(R.string.str_eab3008b),
                    color = UzradyabTheme.colors.textPrimary,
                    fontSize = 14.sp,
                    lineHeight = 24.sp,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    options.chunked(2).forEach { rowOptions ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            rowOptions.forEach { option ->
                                MapStyleCard(
                                    option = option,
                                    selected = option.id == selectedStyle,
                                    onClick = { selectedStyle = option.id },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    DialogTextButton(
                        text = stringResource(R.string.str_c8d2a1fb),
                        primary = false,
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                    )
                    DialogTextButton(
                        text = stringResource(R.string.str_55d482e1),
                        primary = true,
                        onClick = { onSaveStyle(selectedStyle) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
fun DeviceSelectDialog(
    devices: ImmutableListWrapper<Device>,
    selectedDeviceId: Long?,
    onDeviceClick: (Long) -> Unit,
    onDismiss: () -> Unit,
    onLoadMore: () -> Unit = {},
    isLoadingMore: Boolean = false,
    modifier: Modifier = Modifier,
    searchViewModel: DeviceSearchViewModel = hiltViewModel(),
) {
    var search by rememberSaveable { mutableStateOf("") }
    val query = search.trim()
    val searchState by searchViewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(query) { searchViewModel.onQueryChange(query) }
    val filteredDevices = remember(devices, searchState.results, query) {
        if (query.isBlank()) devices.items
        else DeviceSearch.merge(devices.items, searchState.results.items, query)
    }

    var isLocallyLoading by remember { mutableStateOf(false) }
    LaunchedEffect(devices.size) {
        isLocallyLoading = false
    }

    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = listState.layoutInfo.totalItemsCount
            total > 0 && lastVisible >= total - 3
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && query.isBlank() && !isLoadingMore && !isLocallyLoading) {
            isLocallyLoading = true
            onLoadMore()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .widthIn(max = 343.dp)
                .padding(vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TextField(
                value = search,
                onValueChange = { search = it },
                placeholder = {
                    Text(
                        text = stringResource(R.string.str_646e8d8f),
                        color = UzradyabTheme.colors.textPrimary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = UzradyabTheme.colors.textPrimary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Right,
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = themedColor(light = Color.White, dark = Color(0xFF27343F)),
                    unfocusedContainerColor = themedColor(light = Color.White, dark = Color(0xFF27343F)),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .border(2.dp, UzradyabTheme.colors.primary, RoundedCornerShape(8.dp)),
            )
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 0.dp)
            ) {
                items(filteredDevices, key = { it.id }) { device ->
                    DeviceSelectRow(
                        device = device,
                        selected = device.id == selectedDeviceId,
                        onClick = {
                            onDeviceClick(device.id)
                        },
                    )
                }
                if (filteredDevices.isEmpty() && searchState.isSearching) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.5.dp,
                                color = UzradyabTheme.colors.primary,
                            )
                        }
                    }
                } else if (filteredDevices.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .background(themedColor(light = Color.White, dark = Color(0xFF27343F)), RoundedCornerShape(8.dp))
                                .border(1.dp, themedColor(light = Color(0xFFE3E8EE), dark = Color(0xFF384C5C)), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = stringResource(R.string.str_69c5b430),
                                color = UzradyabTheme.colors.textMuted,
                                fontSize = 14.sp,
                            )
                        }
                    }
                }
                if ((isLoadingMore || isLocallyLoading) && filteredDevices.isNotEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.5.dp,
                                color = UzradyabTheme.colors.primary,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceSelectRow(
    device: Device,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(themedColor(light = Color.White, dark = Color(0xFF27343F)), RoundedCornerShape(8.dp))
            .border(2.dp, if (selected) UzradyabTheme.colors.primary else themedColor(light = Color(0xFFE3E8EE), dark = Color(0xFF384C5C)), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(if (selected) UzradyabTheme.colors.primary else themedColor(light = Color(0xFFE3E8EE), dark = Color(0xFF384C5C)), androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(themedColor(light = Color.White, dark = Color(0xFF27343F)), androidx.compose.foundation.shape.CircleShape),
                )
            }
            Text(
                text = device.name,
                color = if (selected) UzradyabTheme.colors.primary else UzradyabTheme.colors.textPrimary,
                fontSize = 14.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = device.phone ?: "نامشخص",
            color = themedColor(light = Color(0xFF68737D), dark = Color(0xFFCCCCCC)),
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Left,
            modifier = Modifier.weight(1f).padding(start = 16.dp),
        )
    }
}

@Composable
fun MapLatestEventDialog(
    latestEvent: MapLatestEventItem?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .widthIn(max = 360.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = themedColor(light = Color.White, dark = Color(0xFF27343F))),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.End,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.str_914a74a3),
                        color = UzradyabTheme.colors.textPrimary,
                        fontSize = 18.sp,
                        lineHeight = 28.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    NotificationBellIcon(color = UzradyabTheme.colors.primary)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = latestEvent?.text ?: "داده‌ای موجود نیست",
                    color = themedColor(light = Color(0xFF1C262E), dark = Color(0xFFC0CDD8)),
                    fontSize = 14.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth(),
                )
                latestEvent?.timeText?.let { time ->
                    Spacer(modifier = Modifier.height(6.dp))
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Text(
                            text = time,
                            color = UzradyabTheme.colors.primary,
                            fontSize = 12.sp,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                DialogTextButton(
                    text = stringResource(R.string.str_fa882772),
                    primary = true,
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun MapStyleCard(
    option: MapStyleOption,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .height(116.dp)
            .background(themedColor(light = Color.White, dark = Color(0xFF27343F)), RoundedCornerShape(8.dp))
            .border(
                width = 1.dp,
                color = if (selected) UzradyabTheme.colors.primary else themedColor(light = Color(0xFFE3E8EE), dark = Color(0xFF171E26)),
                shape = RoundedCornerShape(8.dp),
            )
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .background(themedColor(light = Color.White, dark = Color(0xFF27343F)), RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(
                selected = selected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = UzradyabTheme.colors.primary),
                modifier = Modifier.size(28.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = option.title,
                color = UzradyabTheme.colors.textPrimary,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(themedColor(light = Color(0xFFE3E8EE), dark = Color(0xFF171E26)), RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)),
        ) {
            val imageRes = when (option.id) {
                "osm" -> com.example.uzradyab.R.drawable.preview_osm
                "googleSatellite" -> com.example.uzradyab.R.drawable.preview_satellite
                "googleRoad" -> com.example.uzradyab.R.drawable.preview_road
                "carto" -> com.example.uzradyab.R.drawable.preview_exir
                else -> null
            }
            if (imageRes != null) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = imageRes),
                    contentDescription = option.title,
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun DialogTextButton(
    text: String,
    primary: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .background(if (primary) UzradyabTheme.colors.primary else Color.Transparent, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = if (primary) themedColor(light = Color.White, dark = Color(0xFF27343F)) else UzradyabTheme.colors.primary,
            fontSize = 14.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

private data class MapStyleOption(
    val id: String,
    val title: String,
)

@Composable
private fun mapPreviewColor(id: String): Color = when (id) {
    "googleSatellite" -> themedColor(light = Color(0xFF51604A), dark = Color(0xFFAFBCA9))
    "googleRoad" -> themedColor(light = Color(0xFFE9E5D7), dark = Color(0xFF312C1B))
    "carto" -> themedColor(light = Color(0xFFE7EEF5), dark = Color(0xFF121F2B))
    else -> themedColor(light = Color(0xFFEDEAE0), dark = Color(0xFF272316))
}

@Composable
private fun EventPulseDot() {
    val outerColor = themedColor(light = Color(0x29E5B850), dark = Color(0x296F5210))
    val innerColor = themedColor(light = Color(0xFFE5B850), dark = Color(0xFF6F5210))
    Canvas(modifier = Modifier.size(10.dp)) {
        drawCircle(outerColor, radius = 5.dp.toPx(), center = Offset(size.width / 2f, size.height / 2f))
        drawCircle(innerColor, radius = 3.dp.toPx(), center = Offset(size.width / 2f, size.height / 2f))
    }
}

@Composable
private fun SmallCarIcon(color: Color) {
    Icon(
        imageVector = Icons.Default.DirectionsCar,
        contentDescription = "Car",
        tint = color,
        modifier = Modifier.size(width = 20.dp, height = 16.dp)
    )
}

@Composable
private fun ChevronDownIcon(color: Color) {
    Icon(
        imageVector = Icons.Default.KeyboardArrowDown,
        contentDescription = "Chevron Down",
        tint = color,
        modifier = Modifier.size(24.dp)
    )
}

@Composable
private fun SettingsGearIcon(size: Int, color: Color = themedColor(light = Color.White, dark = Color.White)) {
    Icon(
        imageVector = Icons.Default.Settings,
        contentDescription = "Settings",
        tint = color,
        modifier = Modifier.size(size.dp)
    )
}

@Composable
private fun NotificationBellIcon(color: Color = themedColor(light = Color.White, dark = Color.White), size: Int = 28) {
    Icon(
        imageVector = Icons.Default.Notifications,
        contentDescription = "Notifications",
        tint = color,
        modifier = Modifier.size(size.dp)
    )
}
