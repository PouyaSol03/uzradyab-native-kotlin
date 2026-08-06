package com.example.uzradyab.presentation.map

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.ui.input.pointer.pointerInput
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

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.ceil
import org.json.JSONObject
import androidx.compose.ui.res.stringResource
import com.example.uzradyab.ui.theme.UzradyabTheme
import com.example.uzradyab.ui.theme.themedColor

@Composable
fun SelectedDeviceStatusCard(
    device: Device,
    position: Position?,
    todayDistanceText: String,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onManageClick: () -> Unit,
    onRenewClick: () -> Unit,
    onReplayClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val daysRemaining = daysUntilExpiration(device.expirationTime)
    val hasExpirationWarning = daysRemaining != null && daysRemaining < 10
    val targetCardHeight = when {
        expanded && hasExpirationWarning -> 294.dp
        expanded -> 230.dp
        else -> 170.dp
    }
    val cardHeight by animateDpAsState(
        targetValue = targetCardHeight,
        animationSpec = tween(durationMillis = 260),
        label = "selectedDeviceCardHeight",
    )
    val bodyHeight = cardHeight - 23.dp
    val targetActionY = when {
        expanded && hasExpirationWarning -> 207.dp
        expanded -> 143.dp
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
            .height(cardHeight)
            .pointerInput(expanded) {
                var totalDrag = 0f
                detectVerticalDragGestures(
                    onDragStart = { totalDrag = 0f },
                    onDragEnd = {
                        if (totalDrag < -40f && !expanded) {
                            onToggleExpanded()
                        } else if (totalDrag > 40f && expanded) {
                            onToggleExpanded()
                        }
                    },
                    onDragCancel = { totalDrag = 0f },
                    onVerticalDrag = { change, dragAmount ->
                        totalDrag += dragAmount
                    }
                )
            },
        contentAlignment = Alignment.BottomCenter,
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(bodyHeight),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = themedColor(light = Color.White, dark = Color(0xFF27343F))),
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
                            onRenewClick = onRenewClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .offset(y = 143.dp)
                                .alpha(extraRowsAlpha),
                        )
                    }
                    
                    ActionRow(
                        onManageClick = onManageClick,
                        onDirectionClick = {
                            position?.let {
                                val uri = android.net.Uri.parse("geo:${it.latitude},${it.longitude}?q=${it.latitude},${it.longitude}(${android.net.Uri.encode(device.name)})")
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                                runCatching { context.startActivity(intent) }
                            }
                        },
                        onShareClick = {
                            position?.let {
                                val url = "https://maps.google.com/?q=${it.latitude},${it.longitude}"
                                val sendIntent = android.content.Intent().apply {
                                    action = android.content.Intent.ACTION_SEND
                                    putExtra(android.content.Intent.EXTRA_TEXT, "موقعیت ${device.name}:\n$url")
                                    type = "text/plain"
                                }
                                val shareIntent = android.content.Intent.createChooser(sendIntent, "اشتراک‌گذاری موقعیت")
                                runCatching { context.startActivity(shareIntent) }
                            }
                        },
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
                    color = themedColor(light = Color(0xFF333638), dark = Color(0xFFAFB3B6)),
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
                color = themedColor(light = Color(0xFF9DA2A5), dark = Color(0xFF97ADBF)),
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
            .height(48.dp)
            .background(if (dark) themedColor(light = Color(0xFF27343F), dark = Color(0xFFA0B4C4)) else themedColor(light = Color(0xFFF7F7F8), dark = Color(0xFF1D1D21)), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
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
            color = if (dark) themedColor(light = Color.White, dark = Color(0xFF27343F)) else UzradyabTheme.colors.textPrimary,
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
    onRenewClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(48.dp)
            .background(themedColor(light = Color(0xFFFADDDD), dark = Color(0xFF350808)), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SecondaryActionPill(
            text = stringResource(R.string.str_b3da1036), 
            dark = false, 
            primaryText = true,
            showIcon = false,
            onClick = onRenewClick
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = expirationText(daysRemaining),
                color = themedColor(light = Color(0xFF7D2D2D), dark = Color(0xFFD68E8E)),
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
    showIcon: Boolean = true,
) {
    Row(
        modifier = modifier
            .height(36.dp)
            .background(if (dark) Color.Transparent else themedColor(light = Color.White, dark = Color(0xFF27343F)), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            color = when {
                dark -> themedColor(light = Color.White, dark = Color.White)
                primaryText -> UzradyabTheme.colors.primary
                else -> themedColor(light = Color(0xFF384C5C), dark = Color(0xFFA0B5C5))
            },
            fontSize = 12.sp,
            lineHeight = 21.sp,
            fontWeight = FontWeight.Medium,
        )
        if (showIcon) {
            Spacer(modifier = Modifier.width(6.dp))
            PlayCircleIcon(color = if (dark) themedColor(light = Color.White, dark = Color.White) else themedColor(light = Color(0xFF384C5C), dark = Color(0xFFA0B5C5)), size = 16)
        }
    }
}

@Composable
private fun ActionRow(
    onManageClick: () -> Unit,
    onDirectionClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.height(48.dp),
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
            CircleIconButton(onClick = onDirectionClick) { DirectionIcon() }
            CircleIconButton(onClick = onShareClick) { ShareIcon() }
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
            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(themedColor(light = Color.White, dark = Color(0xFF27343F)))
        )
        Icon(
            imageVector = Icons.Default.KeyboardArrowUp,
            contentDescription = null,
            tint = UzradyabTheme.colors.primary,
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
    androidx.compose.material3.Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(8.dp),
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = themedColor(light = Color(0xFF3B82F6), dark = Color(0xFF5D94EE)))
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ChevronSideIcon(left = true, color = themedColor(light = Color.White, dark = Color.White), size = 20)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = themedColor(light = Color.White, dark = Color.White)
            )
        }
    }
}

@Composable
private fun CircleIconButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(48.dp)
            .background(themedColor(light = Color(0xFFECF4FE), dark = UzradyabTheme.colors.primary.copy(alpha = 0.12f)), CircleShape),
    ) {
        content()
    }
}

@Composable
private fun SignalBlock(label: String, active: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        WifiIcon(color = if (active) themedColor(light = Color(0xFF00C89B), dark = Color(0xFF00C89B)) else themedColor(light = Color(0xFFE55353), dark = Color(0xFFE55353)))
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            color = themedColor(light = Color(0xFF676C70), dark = Color(0xFF6A8BA5)),
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
        tint = themedColor(light = Color(0xFFE55353), dark = Color(0xFF6F1111)),
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
        painter = painterResource(id = R.drawable.ic_custom_location),
        contentDescription = "Directions",
        tint = themedColor(light = androidx.compose.ui.graphics.Color.Unspecified, dark = UzradyabTheme.colors.primary),
        modifier = Modifier.size(24.dp)
    )
}

@Composable
private fun ShareIcon() {
    Icon(
        painter = painterResource(id = R.drawable.ic_custom_share),
        contentDescription = "Share",
        tint = themedColor(light = androidx.compose.ui.graphics.Color.Unspecified, dark = UzradyabTheme.colors.primary),
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
        minutes <= 10 -> "به تازگی"
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
