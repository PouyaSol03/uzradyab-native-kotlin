package com.example.uzradyab.presentation.map

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import androidx.compose.ui.res.stringResource

@Composable
fun SelectedDeviceStatusCard(
    device: Device,
    position: Position?,
    todayDistanceText: String,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onManageClick: () -> Unit,
    onReplayClick: () -> Unit,
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
    val extraRowsAlpha by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "extraRowsAlpha",
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
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .offset(y = 16.dp),
                    )
                    
                    DistanceRow(
                        todayDistanceText = todayDistanceText,
                        dark = false,
                        onReplayClick = onReplayClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .offset(y = 79.dp)
                            .alpha(extraRowsAlpha),
                    )
                    if (hasExpirationWarning) {
                        ExpirationRow(
                            daysRemaining = daysRemaining,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .offset(y = 135.dp)
                                .alpha(extraRowsAlpha),
                        )
                    }
                    
                    ActionRow(
                        onManageClick = onManageClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .offset(y = actionY),
                    )
                }
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
        Column(
            modifier = Modifier.weight(1f).padding(start = 16.dp),
            horizontalAlignment = Alignment.End
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showWarningIcon) {
                    WarningDotIcon()
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = device.name,
                    modifier = Modifier.weight(1f, fill = false),
                    color = Color(0xFF333638),
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Right,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
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
    onReplayClick: () -> Unit,
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
        SecondaryActionPill(
            text = stringResource(R.string.str_f6eb9984), 
            dark = dark, 
            onClick = onReplayClick
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
        SecondaryActionPill(
            text = stringResource(R.string.str_b3da1036), 
            dark = false, 
            primaryText = true,
            onClick = { /* TODO */ }
        )
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
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    primaryText: Boolean = false,
) {
    Row(
        modifier = modifier
            .height(32.dp)
            .background(if (dark) Color.Transparent else Color.White, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
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
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.height(40.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PrimaryActionButton(
            text = stringResource(R.string.str_a6b9c52a),
            onClick = onManageClick,
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.width(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            CircleIconButton { DirectionIcon() }
            CircleIconButton { ShareIcon() }
        }
    }
}

@Composable
private fun ChevronHandle(
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = 260),
        label = "chevronRotation"
    )
    Box(
        modifier = modifier
            .width(107.dp)
            .height(30.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(id = R.drawable.selected_device_card_handle),
            contentDescription = null,
        )
        Icon(
            imageVector = Icons.Default.KeyboardArrowUp,
            contentDescription = null,
            tint = AppBlue,
            modifier = Modifier.rotate(rotation).offset(y = (-3).dp).size(22.dp)
        )
    }
}

@Composable
private fun PrimaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
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
    Icon(
        imageVector = Icons.Default.Error,
        contentDescription = "Warning",
        tint = Color(0xFFE55353),
        modifier = Modifier.size(16.dp)
    )
}

@Composable
private fun WifiIcon(color: Color) {
    Icon(
        imageVector = Icons.Default.Wifi,
        contentDescription = "Wifi",
        tint = color,
        modifier = Modifier.size(24.dp)
    )
}

@Composable
private fun ChevronSideIcon(left: Boolean, color: Color, size: Int) {
    Icon(
        imageVector = if (left) Icons.Default.ChevronLeft else Icons.Default.ChevronRight,
        contentDescription = "Chevron",
        tint = color,
        modifier = Modifier.size(size.dp)
    )
}

@Composable
private fun DirectionIcon() {
    Icon(
        imageVector = Icons.Default.NearMe,
        contentDescription = "Directions",
        tint = AppBlue,
        modifier = Modifier.size(24.dp)
    )
}

@Composable
private fun ShareIcon() {
    Icon(
        imageVector = Icons.Default.Share,
        contentDescription = "Share",
        tint = AppBlue,
        modifier = Modifier.size(24.dp)
    )
}

@Composable
private fun PlayCircleIcon(color: Color, size: Int) {
    Icon(
        imageVector = Icons.Default.PlayCircleOutline,
        contentDescription = "Play",
        tint = color,
        modifier = Modifier.size(size.dp)
    )
}

private fun String.attributeInt(key: String): Int? {
    return runCatching {
        if (JSONObject(this).has(key)) JSONObject(this).optInt(key) else null
    }.getOrNull()
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
