package com.example.uzradyab.presentation.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.uzradyab.domain.model.Device
import com.example.uzradyab.ui.theme.AppBlue
import com.example.uzradyab.ui.theme.AppTextMuted
import com.example.uzradyab.ui.theme.AppTextPrimary

@Composable
fun MapTopControls(
    devices: List<Device>,
    selectedDeviceId: Long?,
    latestEvent: MapLatestEventItem?,
    onDeviceSelectorClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onEventsClick: () -> Unit,
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
            MapSettingsTrigger(
                onClick = onSettingsClick,
                modifier = Modifier.width(142.dp),
            )
            DeviceSelectTrigger(
                text = selectedDevice?.name ?: "انتخاب دستگاه",
                onClick = onDeviceSelectorClick,
                modifier = Modifier.weight(1f),
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
        ChevronDownIcon(color = AppTextPrimary)
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
        SmallCarIcon(color = Color.Black)
    }
}

@Composable
private fun MapSettingsTrigger(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(40.dp)
            .shadow(18.dp, RoundedCornerShape(8.dp), clip = false)
            .background(AppBlue, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingsGearIcon(size = 18)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "تنظیمات نقشه",
            color = Color.White,
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
fun MapSettingsDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedStyle by rememberSaveable { mutableStateOf("osm") }
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
                horizontalAlignment = Alignment.End,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "تنظیمات نقشه",
                        color = AppTextPrimary,
                        fontSize = 18.sp,
                        lineHeight = 28.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    SettingsGearIcon(size = 22, color = AppTextMuted)
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "نمایش نقشه",
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
                        text = "انصراف",
                        primary = false,
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                    )
                    DialogTextButton(
                        text = "ذخیره تغییرات",
                        primary = true,
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
fun DeviceSelectDialog(
    devices: List<Device>,
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
                .widthIn(max = 343.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TextField(
                value = search,
                onValueChange = { search = it },
                placeholder = {
                    Text(
                        text = "جستجو دستگاه",
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
                    .height(72.dp)
                    .border(2.dp, AppBlue, RoundedCornerShape(8.dp)),
            )
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                filteredDevices.forEach { device ->
                    DeviceSelectRow(
                        device = device,
                        selected = device.id == selectedDeviceId,
                        onClick = {
                            onDeviceClick(device.id)
                            onDismiss()
                        },
                    )
                }
                if (filteredDevices.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp)
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFFE3E8EE), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "هیچ دستگاهی پیدا نشد.",
                            color = AppTextMuted,
                            fontSize = 14.sp,
                        )
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
            .height(80.dp)
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(2.dp, if (selected) AppBlue else Color(0xFFE3E8EE), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = device.uniqueId,
            color = Color(0xFF68737D),
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = device.name,
                color = AppBlue,
                fontSize = 14.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
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
        }
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
                        text = "آخرین رویداد",
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
                    text = "بستن",
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
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = option.title,
                color = AppTextPrimary,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            RadioButton(
                selected = selected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = AppBlue),
                modifier = Modifier.size(28.dp),
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(mapPreviewColor(option.id), RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)),
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                repeat(4) { index ->
                    val y = (18 + index * 16).dp.toPx()
                    drawLine(
                        Color.White.copy(alpha = 0.55f),
                        Offset(0f, y),
                        Offset(size.width, y + 18.dp.toPx()),
                        strokeWidth = stroke.width,
                        cap = StrokeCap.Round,
                    )
                }
                repeat(3) { index ->
                    val x = (24 + index * 38).dp.toPx()
                    drawLine(
                        Color.White.copy(alpha = 0.42f),
                        Offset(x, 0f),
                        Offset(x - 18.dp.toPx(), size.height),
                        strokeWidth = stroke.width,
                        cap = StrokeCap.Round,
                    )
                }
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
    Canvas(modifier = Modifier.size(width = 20.dp, height = 16.dp)) {
        val stroke = Stroke(width = 1.7.dp.toPx(), cap = StrokeCap.Round)
        val path = Path().apply {
            moveTo(2.dp.toPx(), 10.dp.toPx())
            lineTo(5.dp.toPx(), 5.dp.toPx())
            lineTo(15.dp.toPx(), 5.dp.toPx())
            lineTo(18.dp.toPx(), 10.dp.toPx())
            lineTo(18.dp.toPx(), 13.dp.toPx())
            lineTo(2.dp.toPx(), 13.dp.toPx())
            close()
        }
        drawPath(path, color, style = stroke)
        drawCircle(color, 1.5.dp.toPx(), Offset(6.dp.toPx(), 13.dp.toPx()))
        drawCircle(color, 1.5.dp.toPx(), Offset(14.dp.toPx(), 13.dp.toPx()))
    }
}

@Composable
private fun ChevronDownIcon(color: Color) {
    Canvas(modifier = Modifier.size(24.dp)) {
        val path = Path().apply {
            moveTo(7.dp.toPx(), 9.dp.toPx())
            lineTo(12.dp.toPx(), 14.dp.toPx())
            lineTo(17.dp.toPx(), 9.dp.toPx())
        }
        drawPath(path, color, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
    }
}

@Composable
private fun SettingsGearIcon(size: Int, color: Color = Color.White) {
    Canvas(modifier = Modifier.size(size.dp)) {
        val stroke = Stroke(width = 1.7.dp.toPx(), cap = StrokeCap.Round)
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        drawCircle(color, radius = this.size.width * 0.16f, center = center, style = stroke)
        drawCircle(color, radius = this.size.width * 0.38f, center = center, style = stroke)
        repeat(8) { index ->
            val angle = Math.toRadians((index * 45).toDouble())
            drawLine(
                color,
                Offset(
                    x = center.x + kotlin.math.cos(angle).toFloat() * this.size.width * 0.38f,
                    y = center.y + kotlin.math.sin(angle).toFloat() * this.size.width * 0.38f,
                ),
                Offset(
                    x = center.x + kotlin.math.cos(angle).toFloat() * this.size.width * 0.45f,
                    y = center.y + kotlin.math.sin(angle).toFloat() * this.size.width * 0.45f,
                ),
                strokeWidth = 1.5.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }
    }
}

@Composable
private fun NotificationBellIcon(color: Color = Color.White) {
    Canvas(modifier = Modifier.size(28.dp)) {
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        val bell = Path().apply {
            moveTo(8.dp.toPx(), 20.dp.toPx())
            cubicTo(9.dp.toPx(), 18.dp.toPx(), 9.5.dp.toPx(), 16.dp.toPx(), 9.5.dp.toPx(), 12.dp.toPx())
            cubicTo(9.5.dp.toPx(), 8.dp.toPx(), 12.dp.toPx(), 6.dp.toPx(), 14.dp.toPx(), 6.dp.toPx())
            cubicTo(16.dp.toPx(), 6.dp.toPx(), 18.5.dp.toPx(), 8.dp.toPx(), 18.5.dp.toPx(), 12.dp.toPx())
            cubicTo(18.5.dp.toPx(), 16.dp.toPx(), 19.dp.toPx(), 18.dp.toPx(), 20.dp.toPx(), 20.dp.toPx())
        }
        drawPath(bell, color, style = stroke)
        drawLine(color, Offset(7.dp.toPx(), 20.dp.toPx()), Offset(21.dp.toPx(), 20.dp.toPx()), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(color, radius = 1.8.dp.toPx(), center = Offset(14.dp.toPx(), 23.dp.toPx()))
        drawCircle(color, radius = 3.dp.toPx(), center = Offset(20.dp.toPx(), 7.dp.toPx()))
    }
}
