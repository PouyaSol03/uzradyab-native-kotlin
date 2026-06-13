package com.example.uzradyab.presentation.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uzradyab.domain.model.Device
import com.example.uzradyab.domain.model.Position
import com.example.uzradyab.ui.theme.AppBlue

@Composable
fun DeviceManagementPanel(
    device: Device,
    position: Position?,
    todayDistanceText: String,
    onDeviceSpecsClick: () -> Unit,
    onDeviceSettingsClick: () -> Unit,
    onReplayTripClick: () -> Unit,
    onCommandsClick: () -> Unit,
    onReportsClick: () -> Unit,
    onEventsClick: () -> Unit,
    onAlertsSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 375.dp)
            .height(436.dp) // Adjusted height
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
    ) {
        DeviceManagementHeader(
            device = device,
            position = position,
            todayDistanceText = todayDistanceText,
            onSpecsClick = onDeviceSpecsClick,
            onReplayClick = onReplayTripClick
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.White),
        ) {
            ManagementGrid(
                onDeviceSettingsClick = onDeviceSettingsClick,
                onReplayTripClick = onReplayTripClick,
                onCommandsClick = onCommandsClick,
                onReportsClick = onReportsClick,
                onEventsClick = onEventsClick,
                onAlertsSettingsClick = onAlertsSettingsClick,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 24.dp),
            )
        }
    }
}

@Composable
private fun DeviceManagementHeader(
    device: Device,
    position: Position?,
    todayDistanceText: String,
    onSpecsClick: () -> Unit,
    onReplayClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(152.dp)
            .background(Color(0xFF384C5C))
            .padding(horizontal = 16.dp, vertical = 24.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f).padding(end = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MovingCarBadge()
                Spacer(modifier = Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = device.name,
                        color = Color.White,
                        fontSize = 16.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Right,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    )
                    Text(
                        text = if ((position?.speed ?: 0.0) > 0.0) "در حال حرکت..." else formatDeviceStatus(device.status),
                        color = Color(0xFF97ADBF),
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        textAlign = TextAlign.Right,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    )
                }
            }
            HeaderButton(
                text = "مشخصات دستگاه",
                onClick = onSpecsClick
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(Color(0xFF27343F), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "پیمایش امروز: $todayDistanceText",
                color = Color.White,
                fontSize = 12.sp,
                lineHeight = 22.sp,
                textAlign = TextAlign.Right,
                maxLines = 1,
                modifier = Modifier.padding(start = 8.dp).weight(1f),
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            )
            HeaderButton(text = "بازپخش مسیر", white = true, onClick = onReplayClick)
        }
    }
}

@Composable
private fun HeaderButton(
    text: String,
    white: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Box(
        modifier = modifier
            .height(40.dp)
            .background(if (white) Color.Transparent else AppBlue, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (white) {
                MiniPlayIcon()
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = text,
                    color = Color.White,
                    fontSize = 14.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(modifier = Modifier.width(4.dp))
                MiniChevronIcon()
            } else {
                Text(
                    text = text,
                    color = Color.White,
                    fontSize = 14.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(modifier = Modifier.width(8.dp))
                MiniChevronIcon()
            }
        }
    }
}

@Composable
private fun ManagementGrid(
    onDeviceSettingsClick: () -> Unit,
    onReplayTripClick: () -> Unit,
    onCommandsClick: () -> Unit,
    onReportsClick: () -> Unit,
    onEventsClick: () -> Unit,
    onAlertsSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ManagementTile("محدوده جغرافیایی", Color(0xFFECF4FE), Color(0xFF062C66), modifier = Modifier.weight(1f)) { MapFrameIcon(it) }
            ManagementTile("بازپخش مسیرها", Color(0xFFE7F6ED), Color(0xFF205535), modifier = Modifier.weight(1f), onClick = onReplayTripClick) { PlayIcon(it) }
            ManagementTile("تنظیمات دستگاه", Color(0xFFFEF3EC), Color(0xFF743106), modifier = Modifier.weight(1f), onClick = onDeviceSettingsClick) { SettingsIcon(it) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ManagementTile("گزارش‌ها", Color(0xFFFDF1FE), Color(0xFF6A0872), modifier = Modifier.weight(1f), onClick = onReportsClick) { DocumentsIcon(it) }
            ManagementTile("تنظیمات هشدار‌ها", Color(0xFFFEECEC), Color(0xFF6B0606), modifier = Modifier.weight(1f), onClick = onAlertsSettingsClick) { AlarmTileIcon(it) }
            ManagementTile("دستورات", Color(0xFFF4F6F8), Color(0xFF384C5C), modifier = Modifier.weight(1f), onClick = onCommandsClick) { FlashIcon(it) }
        }
    }
}

@Composable
private fun ManagementTile(
    label: String,
    background: Color,
    foreground: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    icon: @Composable (Color) -> Unit,
) {
    Box(
        modifier = modifier
            .height(78.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            icon(foreground)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                color = foreground,
                fontSize = 10.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun MovingCarBadge() {
    Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
        Box(modifier = Modifier.size(48.dp).background(Color(0x14C8009D), CircleShape))
        Box(modifier = Modifier.size(40.dp).background(Color(0x3DC8009D), CircleShape))
        Box(modifier = Modifier.size(32.dp).background(Color(0xFFA12887), CircleShape), contentAlignment = Alignment.Center) {
            CarGlyph(Color.White, Modifier.size(18.dp))
        }
    }
}

@Composable
private fun MiniChevronIcon() {
    Icon(
        imageVector = Icons.Default.ChevronLeft,
        contentDescription = "Chevron",
        tint = Color.White,
        modifier = Modifier.size(20.dp)
    )
}

@Composable
private fun MiniPlayIcon() {
    Icon(
        imageVector = Icons.Default.PlayCircleOutline,
        contentDescription = "Play",
        tint = Color.White,
        modifier = Modifier.size(16.dp)
    )
}

@Composable
private fun CarGlyph(color: Color, modifier: Modifier = Modifier) {
    Icon(
        imageVector = Icons.Default.DirectionsCar,
        contentDescription = "Car",
        tint = color,
        modifier = modifier
    )
}

@Composable
private fun MapFrameIcon(color: Color) {
    Icon(
        imageVector = Icons.Default.Layers,
        contentDescription = "Geofence",
        tint = color,
        modifier = Modifier.size(24.dp)
    )
}

@Composable
private fun PlayIcon(color: Color) {
    Icon(
        imageVector = Icons.Default.PlayCircleOutline,
        contentDescription = "Playback",
        tint = color,
        modifier = Modifier.size(24.dp)
    )
}

@Composable
private fun SettingsIcon(color: Color) {
    Icon(
        imageVector = Icons.Default.Settings,
        contentDescription = "Settings",
        tint = color,
        modifier = Modifier.size(24.dp)
    )
}

@Composable
private fun DocumentsIcon(color: Color) {
    Icon(
        imageVector = Icons.Default.Description,
        contentDescription = "Reports",
        tint = color,
        modifier = Modifier.size(24.dp)
    )
}

@Composable
private fun EventsTileIcon(color: Color) {
    Icon(
        imageVector = Icons.Default.EventNote,
        contentDescription = "Events",
        tint = color,
        modifier = Modifier.size(24.dp)
    )
}

@Composable
private fun AlarmTileIcon(color: Color) {
    Icon(
        imageVector = Icons.Default.Notifications,
        contentDescription = "Alerts",
        tint = color,
        modifier = Modifier.size(24.dp)
    )
}

@Composable
private fun FlashIcon(color: Color) {
    Icon(
        imageVector = Icons.Default.Bolt,
        contentDescription = "Commands",
        tint = color,
        modifier = Modifier.size(24.dp)
    )
}

private fun formatDeviceStatus(status: String): String = when (status) {
    "online" -> "آنلاین"
    "offline" -> "آفلاین"
    else -> "وضعیت نامشخص"
}
