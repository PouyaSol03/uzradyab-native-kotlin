package com.example.uzradyab.presentation.map

import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
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
import com.example.uzradyab.ui.theme.AppTextPrimary

@Composable
fun SelectedDeviceStatusCard(
    device: Device,
    position: Position?,
    todayDistanceText: String,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onManageClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasExpirationWarning = device.expirationTime != null
    val cardHeight = when {
        expanded && hasExpirationWarning -> 273.dp
        expanded -> 214.dp
        else -> 163.dp
    }

    Box(
        modifier = modifier
            .fillMaxWidth(0.914f)
            .widthIn(max = 343.dp)
            .height(cardHeight),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(cardHeight - 23.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 14.dp)) {
                DeviceHeader(device = device, position = position, showWarningIcon = expanded && hasExpirationWarning)
                if (expanded) {
                    Spacer(modifier = Modifier.height(15.dp))
                    if (hasExpirationWarning) {
                        ExpirationRow()
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    DistanceRow(todayDistanceText = todayDistanceText, dark = false)
                    Spacer(modifier = Modifier.height(16.dp))
                } else {
                    Spacer(modifier = Modifier.height(19.dp))
                }
                ActionRow(onManageClick = onManageClick)
            }
        }
        ChevronHandle(
            expanded = expanded,
            onClick = onToggleExpanded,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

@Composable
private fun DeviceHeader(
    device: Device,
    position: Position?,
    showWarningIcon: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SignalBlock(label = "GSM", active = position != null)
            SignalBlock(label = "GPS", active = position != null)
        }
        Column(horizontalAlignment = Alignment.End) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showWarningIcon) {
                    WarningDotIcon()
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = device.name,
                    color = Color(0xFF333638),
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Right,
                    maxLines = 1,
                )
            }
            Text(
                text = "آخرین بروزرسانی: ${position?.serverTime ?: device.lastUpdate ?: "نامشخص"}",
                color = Color(0xFF9DA2A5),
                fontSize = 12.sp,
                lineHeight = 22.sp,
                maxLines = 1,
                textAlign = TextAlign.Right,
            )
        }
    }
}

@Composable
private fun DistanceRow(todayDistanceText: String, dark: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(if (dark) Color(0xFF27343F) else Color(0xFFF7F7F8), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SecondaryActionPill(
            text = "بازپخش مسیر",
            dark = dark,
            width = if (dark) 139.dp else 127.dp,
        )
        Text(
            text = "پیمایش امروز: $todayDistanceText",
            color = if (dark) Color.White else AppTextPrimary,
            fontSize = 12.sp,
            lineHeight = 22.sp,
            textAlign = TextAlign.Right,
            maxLines = 1,
        )
    }
}

@Composable
private fun ExpirationRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(Color(0xFFFADDDD), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SecondaryActionPill(text = "تمدید اعتبار", dark = false, width = 92.dp, primaryText = true)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "2 روز تا پایان اعتبار دستگاه",
                color = Color(0xFF7D2D2D),
                fontSize = 12.sp,
                lineHeight = 22.sp,
                textAlign = TextAlign.Right,
                maxLines = 1,
            )
            Spacer(modifier = Modifier.width(8.dp))
            WarningDotIcon()
        }
    }
}

@Composable
private fun SecondaryActionPill(
    text: String,
    dark: Boolean,
    width: androidx.compose.ui.unit.Dp,
    primaryText: Boolean = false,
) {
    Row(
        modifier = Modifier
            .width(width)
            .height(32.dp)
            .background(if (dark) Color.Transparent else Color.White, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            color = when {
                dark -> Color.White
                primaryText -> AppBlue
                else -> Color(0xFF384C5C)
            },
            fontSize = 12.sp,
            lineHeight = 21.sp,
            fontWeight = FontWeight.Medium,
        )
        Spacer(modifier = Modifier.width(6.dp))
        PlayCircleIcon(color = if (dark) Color.White else Color(0xFF384C5C), size = 16)
    }
}

@Composable
private fun ActionRow(onManageClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            CircleIconButton { DirectionIcon() }
            CircleIconButton { ShareIcon() }
        }
        PrimaryActionButton(
            text = "مدیریت دستگاه",
            onClick = onManageClick,
            width = 199.dp,
        )
    }
}

@Composable
private fun ChevronHandle(
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .width(107.dp)
            .height(30.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(0f, 23.5.dp.toPx()),
                size = androidx.compose.ui.geometry.Size(size.width, 6.5.dp.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(21.dp.toPx(), 21.dp.toPx()),
            )
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(23.5.dp.toPx(), 0f),
                size = androidx.compose.ui.geometry.Size(60.dp.toPx(), 28.dp.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(21.dp.toPx(), 21.dp.toPx()),
            )
        }
        ChevronIcon(up = !expanded)
    }
}

@Composable
private fun PrimaryActionButton(
    text: String,
    onClick: () -> Unit,
    width: androidx.compose.ui.unit.Dp,
) {
    Box(
        modifier = Modifier
            .width(width)
            .height(40.dp)
            .background(AppBlue, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ChevronSideIcon(left = false, color = Color.White, size = 20)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                color = Color.White,
                fontSize = 14.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(modifier = Modifier.width(8.dp))
            ChevronSideIcon(left = true, color = Color.White, size = 20)
        }
    }
}

@Composable
private fun CircleIconButton(content: @Composable () -> Unit) {
    IconButton(
        onClick = {},
        modifier = Modifier
            .size(40.dp)
            .background(Color(0xFFECF4FE), CircleShape),
    ) {
        content()
    }
}

@Composable
private fun SignalBlock(label: String, active: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        WifiIcon(color = if (active) Color(0xFF00C89B) else Color(0xFFE55353))
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            color = Color(0xFF676C70),
            fontSize = 10.sp,
            fontWeight = FontWeight.Light,
        )
    }
}

@Composable
private fun WarningDotIcon() {
    Canvas(modifier = Modifier.size(16.dp)) {
        drawCircle(Color(0xFFE55353), radius = 6.67.dp.toPx(), center = Offset(size.width / 2, size.height / 2))
        drawCircle(Color.White, radius = 1.dp.toPx(), center = Offset(size.width / 2, 11.dp.toPx()))
        drawLine(
            color = Color.White,
            start = Offset(size.width / 2, 4.dp.toPx()),
            end = Offset(size.width / 2, 8.dp.toPx()),
            strokeWidth = 1.5.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun WifiIcon(color: Color) {
    Canvas(modifier = Modifier.size(24.dp)) {
        val neutral = Color(0xFFBEC1C3)
        drawArc(neutral, 205f, 130f, false, Offset(1.dp.toPx(), 2.dp.toPx()), androidx.compose.ui.geometry.Size(22.dp.toPx(), 14.dp.toPx()), style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
        drawArc(color, 215f, 110f, false, Offset(5.dp.toPx(), 9.dp.toPx()), androidx.compose.ui.geometry.Size(14.dp.toPx(), 8.dp.toPx()), style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
        drawCircle(color, radius = 1.5.dp.toPx(), center = Offset(12.dp.toPx(), 19.dp.toPx()))
    }
}

@Composable
private fun ChevronIcon(up: Boolean) {
    Canvas(modifier = Modifier.size(20.dp)) {
        val path = Path().apply {
            if (up) {
                moveTo(4.dp.toPx(), 12.dp.toPx())
                lineTo(10.dp.toPx(), 6.dp.toPx())
                lineTo(16.dp.toPx(), 12.dp.toPx())
            } else {
                moveTo(4.dp.toPx(), 8.dp.toPx())
                lineTo(10.dp.toPx(), 14.dp.toPx())
                lineTo(16.dp.toPx(), 8.dp.toPx())
            }
        }
        drawPath(path, AppBlue, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
    }
}

@Composable
private fun ChevronSideIcon(left: Boolean, color: Color, size: Int) {
    Canvas(modifier = Modifier.size(size.dp)) {
        val path = Path().apply {
            if (left) {
                moveTo(12.dp.toPx(), 4.dp.toPx())
                lineTo(6.dp.toPx(), 10.dp.toPx())
                lineTo(12.dp.toPx(), 16.dp.toPx())
            } else {
                moveTo(8.dp.toPx(), 4.dp.toPx())
                lineTo(14.dp.toPx(), 10.dp.toPx())
                lineTo(8.dp.toPx(), 16.dp.toPx())
            }
        }
        drawPath(path, color, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
    }
}

@Composable
private fun DirectionIcon() {
    Canvas(modifier = Modifier.size(24.dp)) {
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(AppBlue, radius = 10.dp.toPx(), center = Offset(12.dp.toPx(), 12.dp.toPx()), style = stroke)
        drawLine(AppBlue, Offset(8.dp.toPx(), 16.dp.toPx()), Offset(16.dp.toPx(), 8.dp.toPx()), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
    }
}

@Composable
private fun ShareIcon() {
    Canvas(modifier = Modifier.size(24.dp)) {
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        val points = listOf(Offset(7.dp.toPx(), 12.dp.toPx()), Offset(16.dp.toPx(), 7.dp.toPx()), Offset(16.dp.toPx(), 17.dp.toPx()))
        drawLine(AppBlue, points[0], points[1], strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
        drawLine(AppBlue, points[0], points[2], strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
        points.forEach { drawCircle(AppBlue, radius = 2.5.dp.toPx(), center = it, style = stroke) }
    }
}

@Composable
private fun PlayCircleIcon(color: Color, size: Int) {
    Canvas(modifier = Modifier.size(size.dp)) {
        val stroke = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(color, radius = (size / 2 - 1).dp.toPx(), center = Offset(this.size.width / 2, this.size.height / 2), style = stroke)
        val path = Path().apply {
            moveTo(7.dp.toPx(), 5.dp.toPx())
            lineTo(11.dp.toPx(), 8.dp.toPx())
            lineTo(7.dp.toPx(), 11.dp.toPx())
            close()
        }
        drawPath(path, color)
    }
}
