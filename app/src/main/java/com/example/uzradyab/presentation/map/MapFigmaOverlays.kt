package com.example.uzradyab.presentation.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.uzradyab.R
import androidx.compose.ui.res.stringResource
import com.example.uzradyab.ui.theme.UzradyabTheme
import com.example.uzradyab.ui.theme.themedColor

@Composable
fun MapSettingsButton(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .height(32.dp)
            .shadow(18.dp, RoundedCornerShape(8.dp), clip = false)
            .background(UzradyabTheme.colors.primary, RoundedCornerShape(8.dp))
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingsGearIcon()
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.str_c9c1b29c),
            color = themedColor(light = Color.White, dark = Color.White),
            fontSize = 14.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
fun DeviceMapMarker(
    speedKmh: Int?,
    modifier: Modifier = Modifier,
) {
    val isStopped = speedKmh == null || speedKmh <= 0
    Box(
        modifier = modifier
            .width(72.dp)
            .height(106.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        DeviceMarkerIcon(modifier = Modifier.align(Alignment.BottomCenter))
        SpeedBubble(
            text = if (isStopped) "متوقف" else "${speedKmh.toString().toPersianDigits()} km",
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

@Composable
private fun SpeedBubble(
    text: String,
    modifier: Modifier = Modifier,
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = modifier
                .size(width = 52.dp, height = 33.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            val primaryColor = themedColor(light = Color(0xFFA12887), dark = Color(0xFFE184CD))
            Canvas(
                modifier = Modifier
                    .matchParentSize(),
            ) {
                val radius = 13.dp.toPx()
                drawRoundRect(
                    color = primaryColor,
                    size = androidx.compose.ui.geometry.Size(width = size.width, height = 26.dp.toPx()),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius),
                )
                val path = Path().apply {
                    moveTo(26.1962f / 52f * size.width, 32.5f / 33f * size.height)
                    lineTo(31.3923f / 52f * size.width, 25.5f / 33f * size.height)
                    lineTo(21f / 52f * size.width, 25.5f / 33f * size.height)
                    close()
                }
                drawPath(path, primaryColor)
            }
            Text(
                text = text,
                color = themedColor(light = Color.White, dark = Color.White),
                fontSize = if (text == "متوقف") 11.sp else 10.sp,
                lineHeight = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier
                    .width(52.dp)
                    .height(26.dp),
            )
        }
    }
}

@Composable
private fun DeviceMarkerIcon(modifier: Modifier = Modifier) {
    val innerRing = themedColor(light = Color(0x14C8009D), dark = Color(0x14FF66DE))
    val midRing = themedColor(light = Color(0x3DC8009D), dark = Color(0x3DFF66DE))
    val primaryColor = themedColor(light = Color(0xFFA22887), dark = Color(0xFFE184CC))
    val whiteColor = themedColor(light = Color.White, dark = Color(0xFF27343F))

    Canvas(modifier = modifier.size(72.dp)) {
        drawCircle(innerRing, radius = 36.dp.toPx(), center = Offset(36.dp.toPx(), 36.dp.toPx()))
        drawCircle(midRing, radius = 30.dp.toPx(), center = Offset(36.dp.toPx(), 36.dp.toPx()))
        drawCircle(primaryColor, radius = 24.dp.toPx(), center = Offset(36.dp.toPx(), 36.dp.toPx()))

        scale(scale = size.width / 72f, pivot = Offset.Zero) {
            val car = Path().apply {
                moveTo(21.5698f, 41.7749f)
                cubicTo(20.6279f, 41.7749f, 20f, 41.1439f, 20f, 40.1974f)
                lineTo(20f, 35.4649f)
                cubicTo(20f, 34.0452f, 21.0989f, 32.7832f, 22.3547f, 32.4677f)
                cubicTo(25.1803f, 31.679f, 29.4187f, 30.7325f, 29.4187f, 30.7325f)
                cubicTo(29.4187f, 30.7325f, 31.4595f, 28.524f, 32.8723f, 27.1042f)
                cubicTo(33.6572f, 26.4732f, 34.5991f, 26f, 35.6979f, 26f)
                lineTo(46.6865f, 26f)
                cubicTo(47.6283f, 26f, 48.4132f, 26.631f, 48.8842f, 27.4197f)
                lineTo(51.0819f, 31.9944f)
                cubicTo(51.2897f, 32.6037f, 51.3958f, 33.2434f, 51.3958f, 33.8874f)
                lineTo(51.3958f, 40.1974f)
                cubicTo(51.3958f, 41.1439f, 50.7679f, 41.7749f, 49.826f, 41.7749f)
                lineTo(47.1056f, 41.7749f)
                cubicTo(47.1062f, 41.7497f, 47.1065f, 41.7245f, 47.1065f, 41.6992f)
                cubicTo(47.1065f, 39.9992f, 45.7285f, 38.6211f, 44.0285f, 38.6211f)
                cubicTo(42.3286f, 38.6211f, 40.9505f, 39.9992f, 40.9505f, 41.6992f)
                cubicTo(40.9505f, 41.7245f, 40.9508f, 41.7497f, 40.9514f, 41.7749f)
                lineTo(31.4848f, 41.7749f)
                cubicTo(31.4854f, 41.7497f, 31.4857f, 41.7245f, 31.4857f, 41.6991f)
                cubicTo(31.4857f, 39.9992f, 30.1076f, 38.6211f, 28.4077f, 38.6211f)
                cubicTo(26.7077f, 38.6211f, 25.3297f, 39.9992f, 25.3297f, 41.6992f)
                cubicTo(25.3297f, 41.7245f, 25.33f, 41.7497f, 25.3306f, 41.7749f)
                lineTo(21.5698f, 41.7749f)
                close()
            }
            drawPath(car, whiteColor)

            val leftWindow = Path().apply {
                moveTo(34.7535f, 27.5036f)
                cubicTo(34.8686f, 27.3501f, 35.0492f, 27.2598f, 35.2411f, 27.2598f)
                lineTo(39.203f, 27.2598f)
                cubicTo(39.5396f, 27.2598f, 39.8125f, 27.5327f, 39.8125f, 27.8693f)
                lineTo(39.8125f, 30.3074f)
                cubicTo(39.8125f, 30.6441f, 39.5396f, 30.917f, 39.203f, 30.917f)
                lineTo(33.4125f, 30.917f)
                cubicTo(32.9102f, 30.917f, 32.6235f, 30.3435f, 32.9249f, 29.9417f)
                lineTo(34.7535f, 27.5036f)
                close()
            }
            drawPath(leftWindow, primaryColor)
            drawRoundRect(
                color = primaryColor,
                topLeft = Offset(41.3363f, 27.2598f),
                size = androidx.compose.ui.geometry.Size(5.4857f, 3.6572f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(0.6095f, 0.6095f),
            )
            drawCircle(whiteColor, radius = 2.4624f, center = Offset(44.0282f, 41.6958f))
            drawCircle(whiteColor, radius = 2.4624f, center = Offset(28.411f, 41.6958f))
            drawLine(whiteColor, Offset(43.8931f, 45.8361f), Offset(48.8931f, 45.8361f), strokeWidth = 1f, cap = StrokeCap.Round)
            drawLine(whiteColor, Offset(29.8931f, 45.8361f), Offset(34.8931f, 45.8361f), strokeWidth = 1f, cap = StrokeCap.Round)
        }
    }
}

@Composable
private fun SettingsGearIcon() {
    Icon(
        imageVector = Icons.Default.Settings,
        contentDescription = "Settings",
        tint = themedColor(light = Color.White, dark = Color.White),
        modifier = Modifier.size(16.dp)
    )
}

private fun String.toPersianDigits(): String {
    val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    return buildString(length) {
        this@toPersianDigits.forEach { char ->
            append(if (char in '0'..'9') persianDigits[char - '0'] else char)
        }
    }
}
