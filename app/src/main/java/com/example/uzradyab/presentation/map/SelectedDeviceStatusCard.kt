package com.example.uzradyab.presentation.map

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uzradyab.R
import com.example.uzradyab.domain.model.Device
import com.example.uzradyab.domain.model.Position
import com.example.uzradyab.ui.theme.AppBlue
import com.example.uzradyab.ui.theme.AppTextPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.ceil
import org.json.JSONObject

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
    val daysRemaining = daysUntilExpiration(device.expirationTime)
    val hasExpirationWarning = daysRemaining != null && daysRemaining < 10
    val targetCardHeight = when {
        expanded && hasExpirationWarning -> 273.dp
        expanded -> 214.dp
        else -> 163.dp
    }
    val cardHeight by animateDpAsState(
        targetValue = targetCardHeight,
        animationSpec = tween(durationMillis = 260),
        label = "selectedDeviceCardHeight",
    )
    val bodyHeight = cardHeight - 23.dp
    val targetActionY = when {
        expanded && hasExpirationWarning -> 191.dp
        expanded -> 135.dp
        else -> 83.dp
    }
    val actionY by animateDpAsState(
        targetValue = targetActionY,
        animationSpec = tween(durationMillis = 260),
        label = "selectedDeviceCardActionY",
    )

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
                .height(bodyHeight),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Box(modifier = Modifier.fillMaxWidth().height(bodyHeight)) {
                    DeviceHeader(
                        device = device,
                        position = position,
                        showWarningIcon = expanded && hasExpirationWarning,
                        modifier = Modifier
                            .offset(x = 16.dp, y = 16.dp)
                            .width(311.dp),
                    )
                    if (expanded) {
                        DistanceRow(
                            todayDistanceText = todayDistanceText,
                            dark = false,
                            modifier = Modifier
                                .offset(x = 16.dp, y = 79.dp)
                                .width(311.dp),
                        )
                        if (hasExpirationWarning) {
                            ExpirationRow(
                                daysRemaining = daysRemaining,
                                modifier = Modifier
                                    .offset(x = 16.dp, y = 135.dp)
                                    .width(311.dp),
                            )
                        }
                    }
                    ActionRow(
                        onManageClick = onManageClick,
                        expanded = expanded,
                        modifier = Modifier
                            .offset(x = 16.dp, y = actionY)
                            .width(311.dp),
                    )
                }
            }
        }
        ChevronHandle(
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
    modifier: Modifier = Modifier,
) {
    val gpsActive = (position?.attributesJson?.attributeInt("sat") ?: 0) >= 6
    val gsmActive = position != null

    Row(
        modifier = modifier.height(48.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SignalBlock(label = "GSM", active = gsmActive)
            SignalBlock(label = "GPS", active = gpsActive)
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
                text = "آخرین بروزرسانی: ${formatRelativeTime(position?.serverTime ?: device.lastUpdate)}",
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
private fun DistanceRow(
    todayDistanceText: String,
    dark: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(40.dp)
            .background(if (dark) Color(0xFF27343F) else Color(0xFFF7F7F8), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SecondaryActionPill(text = "بازپخش مسیر", dark = dark, width = if (dark) 139.dp else 127.dp)
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
private fun ExpirationRow(
    daysRemaining: Int?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(40.dp)
            .background(Color(0xFFFADDDD), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SecondaryActionPill(text = "تمدید اعتبار", dark = false, width = 92.dp, primaryText = true)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = expirationText(daysRemaining),
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
private fun ActionRow(
    onManageClick: () -> Unit,
    expanded: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.height(40.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PrimaryActionButton(
            text = "مدیریت دستگاه",
            onClick = onManageClick,
            width = if (expanded) 189.dp else 199.dp,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(if (expanded) 21.dp else 16.dp)) {
            CircleIconButton { DirectionIcon() }
            CircleIconButton { ShareIcon() }
        }
    }
}

@Composable
private fun ChevronHandle(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Image(
        painter = painterResource(id = R.drawable.selected_device_card_handle),
        contentDescription = null,
        modifier = modifier
            .width(107.dp)
            .height(30.dp)
            .clickable(onClick = onClick),
    )
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
            ChevronSideIcon(left = true, color = Color.White, size = 20)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                color = Color.White,
                fontSize = 14.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Medium,
            )
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
        drawArc(
            color = neutral,
            startAngle = 205f,
            sweepAngle = 130f,
            useCenter = false,
            topLeft = Offset(1.dp.toPx(), 2.dp.toPx()),
            size = androidx.compose.ui.geometry.Size(22.dp.toPx(), 14.dp.toPx()),
            style = Stroke(2.dp.toPx(), cap = StrokeCap.Round),
        )
        drawArc(
            color = color,
            startAngle = 215f,
            sweepAngle = 110f,
            useCenter = false,
            topLeft = Offset(5.dp.toPx(), 9.dp.toPx()),
            size = androidx.compose.ui.geometry.Size(14.dp.toPx(), 8.dp.toPx()),
            style = Stroke(2.dp.toPx(), cap = StrokeCap.Round),
        )
        drawCircle(color, radius = 1.5.dp.toPx(), center = Offset(12.dp.toPx(), 19.dp.toPx()))
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

private fun String.attributeInt(key: String): Int? {
    return runCatching {
        if (JSONObject(this).has(key)) JSONObject(this).optInt(key) else null
    }.getOrNull()
}

private fun daysUntilExpiration(value: String?): Int? {
    val expiration = parseServerDate(value) ?: return null
    return ceil((expiration.time - Date().time) / 86_400_000.0).toInt()
}

private fun expirationText(daysRemaining: Int?): String {
    return when {
        daysRemaining == null -> "اعتبار دستگاه نامشخص است"
        daysRemaining >= 0 -> "${daysRemaining.toString().toPersianDigits()} روز تا پایان اعتبار دستگاه"
        else -> "اعتبار دستگاه به پایان رسیده است"
    }
}

private fun formatRelativeTime(value: String?): String {
    val date = parseServerDate(value) ?: return "نامشخص"
    val seconds = abs(Date().time - date.time) / 1_000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    return when {
        seconds < 60 -> "${seconds.toString().toPersianDigits()} ثانیه قبل"
        minutes < 60 -> "${minutes.toString().toPersianDigits()} دقیقه قبل"
        hours < 24 -> "${hours.toString().toPersianDigits()} ساعت قبل"
        else -> "${days.toString().toPersianDigits()} روز قبل"
    }
}

private fun parseServerDate(value: String?): Date? {
    if (value.isNullOrBlank()) return null
    return listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSX",
        "yyyy-MM-dd'T'HH:mm:ssX",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
    ).firstNotNullOfOrNull { pattern ->
        runCatching {
            SimpleDateFormat(pattern, Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.parse(value)
        }.getOrNull()
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
