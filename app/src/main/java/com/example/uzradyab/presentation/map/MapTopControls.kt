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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.example.uzradyab.core.utils.ImmutableListWrapper
import com.example.uzradyab.domain.model.Device
import com.example.uzradyab.ui.theme.AppBlue
import com.example.uzradyab.ui.theme.AppTextMuted
import com.example.uzradyab.ui.theme.AppTextPrimary
import com.example.uzradyab.R
import androidx.compose.ui.res.stringResource

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
    modifier: Modifier = Modifier,
) {
    val selectedDevice = devices.firstOrNull { it.id == selectedDeviceId }

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
private fun DeviceSelectTrigger(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(40.dp)
            .shadow(18.dp, RoundedCornerShape(8.dp), clip = false)
            .background(Color.White, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SmallCarIcon(color = Color.Black)
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
        ChevronDownIcon(color = AppTextPrimary)
    }
}

@Composable
private fun MapSettingsTrigger(
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = if (enabled) AppBlue else Color(0xFFE0E0E0)
    val contentColor = if (enabled) Color.White else Color.Gray

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
            .background(Color.White, RoundedCornerShape(8.dp))
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
                color = Color(0xFF1C262E),
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
                        color = AppBlue,
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
            .background(AppBlue, RoundedCornerShape(8.dp))
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
            .background(if (isLocked) AppBlue else Color.White, RoundedCornerShape(8.dp))
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
                        color = Color.White,
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
                tint = if (isLocked) Color.White else AppBlue,
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
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.Start,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SettingsGearIcon(size = 22, color = AppTextMuted)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.str_c9c1b29c),
                        color = AppTextPrimary,
                        fontSize = 18.sp,
                        lineHeight = 28.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = stringResource(R.string.str_eab3008b),
                    color = AppTextPrimary,
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
    modifier: Modifier = Modifier,
) {
    var search by rememberSaveable { mutableStateOf("") }
    val query = search.trim()
    val filteredDevices = if (query.isBlank()) {
        devices
    } else {
        devices.filter { device ->
            listOf(device.name, device.uniqueId, device.status)
                .any { value -> value.contains(query, ignoreCase = true) }
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
                        color = AppTextPrimary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = AppTextPrimary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Right,
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .border(2.dp, AppBlue, RoundedCornerShape(8.dp)),
            )
            LazyColumn(
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
                if (filteredDevices.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .background(Color.White, RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFFE3E8EE), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = stringResource(R.string.str_69c5b430),
                                color = AppTextMuted,
                                fontSize = 14.sp,
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
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(2.dp, if (selected) AppBlue else Color(0xFFE3E8EE), RoundedCornerShape(8.dp))
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
                    .background(if (selected) AppBlue else Color(0xFFE3E8EE), androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color.White, androidx.compose.foundation.shape.CircleShape),
                )
            }
            Text(
                text = device.name,
                color = AppBlue,
                fontSize = 14.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = device.phone ?: "نامشخص",
            color = Color(0xFF68737D),
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
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.End,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.str_914a74a3),
                        color = AppTextPrimary,
                        fontSize = 18.sp,
                        lineHeight = 28.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    NotificationBellIcon(color = AppBlue)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = latestEvent?.text ?: "داده‌ای موجود نیست",
                    color = Color(0xFF1C262E),
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
                            color = AppBlue,
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
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(
                width = 1.dp,
                color = if (selected) AppBlue else Color(0xFFE3E8EE),
                shape = RoundedCornerShape(8.dp),
            )
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .background(Color.White, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(
                selected = selected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = AppBlue),
                modifier = Modifier.size(28.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = option.title,
                color = AppTextPrimary,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(Color(0xFFE3E8EE), RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
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
            .background(if (primary) AppBlue else Color.Transparent, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = if (primary) Color.White else AppBlue,
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

private fun mapPreviewColor(id: String): Color = when (id) {
    "googleSatellite" -> Color(0xFF51604A)
    "googleRoad" -> Color(0xFFE9E5D7)
    "carto" -> Color(0xFFE7EEF5)
    else -> Color(0xFFEDEAE0)
}

@Composable
private fun EventPulseDot() {
    Canvas(modifier = Modifier.size(10.dp)) {
        drawCircle(Color(0x29E5B850), radius = 5.dp.toPx(), center = Offset(size.width / 2f, size.height / 2f))
        drawCircle(Color(0xFFE5B850), radius = 3.dp.toPx(), center = Offset(size.width / 2f, size.height / 2f))
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
private fun SettingsGearIcon(size: Int, color: Color = Color.White) {
    Icon(
        imageVector = Icons.Default.Settings,
        contentDescription = "Settings",
        tint = color,
        modifier = Modifier.size(size.dp)
    )
}

@Composable
private fun NotificationBellIcon(color: Color = Color.White) {
    Icon(
        imageVector = Icons.Default.Notifications,
        contentDescription = "Notifications",
        tint = color,
        modifier = Modifier.size(28.dp)
    )
}
