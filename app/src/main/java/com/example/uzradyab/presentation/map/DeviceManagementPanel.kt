package com.example.uzradyab.presentation.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 375.dp)
            .height(369.dp),
    ) {
        DeviceManagementHeader(
            device = device,
            position = position,
            todayDistanceText = todayDistanceText,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(241.dp)
                .background(Color.White),
        ) {
            ManagementGrid(
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
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(128.dp)
            .background(Color(0xFF384C5C))
            .padding(horizontal = 16.dp, vertical = 24.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HeaderButton(text = "مشخصات دستگاه")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = device.name,
                        color = Color.White,
                        fontSize = 16.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Right,
                        maxLines = 1,
                    )
                    Text(
                        text = if ((position?.speed ?: 0.0) > 0.0) "در حال حرکت..." else formatDeviceStatus(device.status),
                        color = Color(0xFF97ADBF),
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        textAlign = TextAlign.Right,
                        maxLines = 1,
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                MovingCarBadge()
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(Color(0xFF27343F), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HeaderButton(text = "بازپخش مسیر", white = true, width = 139)
            Text(
                text = "پیمایش امروز: $todayDistanceText",
                color = Color.White,
                fontSize = 12.sp,
                lineHeight = 22.sp,
                textAlign = TextAlign.Right,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun HeaderButton(
    text: String,
    white: Boolean = false,
    width: Int = 137,
) {
    Box(
        modifier = Modifier
            .width(width.dp)
            .height(40.dp)
            .background(if (white) Color.Transparent else AppBlue, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = text,
                color = Color.White,
                fontSize = 14.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(modifier = Modifier.width(8.dp))
            if (white) {
                MiniPlayIcon()
            } else {
                MiniChevronIcon()
            }
        }
    }
}

@Composable
private fun ManagementGrid(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.width(343.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ManagementTile("محدوده جغرافیایی", Color(0xFFECF4FE), Color(0xFF062C66)) { MapFrameIcon(it) }
            ManagementTile("بازپخش مسیرها", Color(0xFFE7F6ED), Color(0xFF205535)) { PlayIcon(it) }
            ManagementTile("تنظیمات دستگاه", Color(0xFFFEF3EC), Color(0xFF743106)) { SettingsIcon(it) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ManagementTile("گزارش‌ها", Color(0xFFFDF1FE), Color(0xFF6A0872)) { DocumentsIcon(it) }
            ManagementTile("تنظیمات هشدار‌ها", Color(0xFFFEECEC), Color(0xFF6B0606)) { AlarmTileIcon(it) }
            ManagementTile("دستورات", Color(0xFFEDECFE), Color(0xFF0D0679)) { FlashIcon(it) }
        }
    }
}

@Composable
private fun ManagementTile(
    label: String,
    background: Color,
    foreground: Color,
    icon: @Composable (Color) -> Unit,
) {
    Box(
        modifier = Modifier
            .width(103.67f.dp)
            .height(78.dp)
            .background(background, RoundedCornerShape(12.dp)),
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
                fontWeight = if (label == "تنظیمات دستگاه") FontWeight.Medium else FontWeight.Normal,
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
    Canvas(modifier = Modifier.size(20.dp)) {
        val path = Path().apply {
            moveTo(12.dp.toPx(), 4.dp.toPx())
            lineTo(6.dp.toPx(), 10.dp.toPx())
            lineTo(12.dp.toPx(), 16.dp.toPx())
        }
        drawPath(path, Color.White, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
    }
}

@Composable
private fun MiniPlayIcon() {
    Canvas(modifier = Modifier.size(16.dp)) {
        drawCircle(Color.White, radius = 7.dp.toPx(), center = Offset(size.width / 2, size.height / 2), style = Stroke(1.5.dp.toPx()))
        val path = Path().apply {
            moveTo(6.dp.toPx(), 5.dp.toPx())
            lineTo(11.dp.toPx(), 8.dp.toPx())
            lineTo(6.dp.toPx(), 11.dp.toPx())
            close()
        }
        drawPath(path, Color.White)
    }
}

@Composable
private fun CarGlyph(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        val path = Path().apply {
            moveTo(1.dp.toPx(), 11.dp.toPx())
            lineTo(4.dp.toPx(), 6.dp.toPx())
            lineTo(14.dp.toPx(), 6.dp.toPx())
            lineTo(17.dp.toPx(), 11.dp.toPx())
            lineTo(17.dp.toPx(), 14.dp.toPx())
            lineTo(1.dp.toPx(), 14.dp.toPx())
            close()
        }
        drawPath(path, color, style = stroke)
    }
}

@Composable
private fun MapFrameIcon(color: Color) = SimpleBoxIcon(color)

@Composable
private fun PlayIcon(color: Color) {
    Canvas(modifier = Modifier.size(24.dp)) {
        drawCircle(color, radius = 10.dp.toPx(), center = Offset(12.dp.toPx(), 12.dp.toPx()), style = Stroke(2.dp.toPx()))
        val path = Path().apply {
            moveTo(10.dp.toPx(), 8.dp.toPx())
            lineTo(16.dp.toPx(), 12.dp.toPx())
            lineTo(10.dp.toPx(), 16.dp.toPx())
            close()
        }
        drawPath(path, color)
    }
}

@Composable
private fun SettingsIcon(color: Color) = SimpleGearIcon(color)

@Composable
private fun DocumentsIcon(color: Color) {
    Canvas(modifier = Modifier.size(24.dp)) {
        drawRoundRect(color, topLeft = Offset(5.dp.toPx(), 4.dp.toPx()), size = androidx.compose.ui.geometry.Size(12.dp.toPx(), 16.dp.toPx()), cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()), style = Stroke(2.dp.toPx()))
        drawLine(color, Offset(8.dp.toPx(), 9.dp.toPx()), Offset(14.dp.toPx(), 9.dp.toPx()), strokeWidth = 1.5.dp.toPx())
        drawLine(color, Offset(8.dp.toPx(), 13.dp.toPx()), Offset(14.dp.toPx(), 13.dp.toPx()), strokeWidth = 1.5.dp.toPx())
    }
}

@Composable
private fun AlarmTileIcon(color: Color) {
    Canvas(modifier = Modifier.size(24.dp)) {
        drawArc(color, 205f, 130f, false, Offset(4.dp.toPx(), 6.dp.toPx()), androidx.compose.ui.geometry.Size(16.dp.toPx(), 14.dp.toPx()), style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
        drawLine(color, Offset(7.dp.toPx(), 21.dp.toPx()), Offset(17.dp.toPx(), 21.dp.toPx()), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
    }
}

@Composable
private fun FlashIcon(color: Color) {
    Canvas(modifier = Modifier.size(24.dp)) {
        val path = Path().apply {
            moveTo(13.dp.toPx(), 2.dp.toPx())
            lineTo(5.dp.toPx(), 13.dp.toPx())
            lineTo(12.dp.toPx(), 13.dp.toPx())
            lineTo(10.dp.toPx(), 22.dp.toPx())
            lineTo(19.dp.toPx(), 10.dp.toPx())
            lineTo(12.dp.toPx(), 10.dp.toPx())
            close()
        }
        drawPath(path, color)
    }
}

@Composable
private fun SimpleBoxIcon(color: Color) {
    Canvas(modifier = Modifier.size(24.dp)) {
        drawRoundRect(color, topLeft = Offset(3.dp.toPx(), 5.dp.toPx()), size = androidx.compose.ui.geometry.Size(18.dp.toPx(), 14.dp.toPx()), cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()), style = Stroke(2.dp.toPx()))
        drawLine(color, Offset(3.dp.toPx(), 10.dp.toPx()), Offset(21.dp.toPx(), 10.dp.toPx()), strokeWidth = 2.dp.toPx())
    }
}

@Composable
private fun SimpleGearIcon(color: Color) {
    Canvas(modifier = Modifier.size(24.dp)) {
        drawCircle(color, radius = 8.dp.toPx(), center = Offset(12.dp.toPx(), 12.dp.toPx()), style = Stroke(2.dp.toPx()))
        drawCircle(color, radius = 2.5.dp.toPx(), center = Offset(12.dp.toPx(), 12.dp.toPx()))
    }
}

private fun formatDeviceStatus(status: String): String = when (status) {
    "online" -> "آنلاین"
    "offline" -> "آفلاین"
    else -> "وضعیت نامشخص"
}
