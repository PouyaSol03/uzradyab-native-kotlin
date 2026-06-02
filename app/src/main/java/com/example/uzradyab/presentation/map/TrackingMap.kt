package com.example.uzradyab.presentation.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uzradyab.domain.model.Device
import com.example.uzradyab.domain.model.Position

@Composable
fun TrackingMap(
    devices: List<Device>,
    latestPositions: Map<Long, Position>,
    selectedDeviceId: Long?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFE8F0F6)),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridColor = Color(0xFFD1DEE8)
            val roadColor = Color.White.copy(alpha = 0.86f)
            val step = 72.dp.toPx()
            var x = -step
            while (x < size.width + step) {
                drawLine(gridColor, Offset(x, 0f), Offset(x + size.height, size.height), 1.dp.toPx())
                x += step
            }
            var y = 0f
            while (y < size.height) {
                drawLine(gridColor, Offset(0f, y), Offset(size.width, y), 1.dp.toPx())
                y += step
            }
            drawLine(
                color = roadColor,
                start = Offset(size.width * 0.08f, size.height * 0.62f),
                end = Offset(size.width * 0.92f, size.height * 0.36f),
                strokeWidth = 18.dp.toPx(),
                cap = StrokeCap.Round,
            )
            drawLine(
                color = roadColor,
                start = Offset(size.width * 0.18f, size.height * 0.16f),
                end = Offset(size.width * 0.72f, size.height * 0.86f),
                strokeWidth = 14.dp.toPx(),
            )
        }
        devices.forEachIndexed { index, device ->
            val selected = device.id == selectedDeviceId
            val position = latestPositions[device.id]
            MapMarker(
                label = device.name.take(2),
                selected = selected,
                hasPosition = position != null,
                modifier = Modifier
                    .align(markerAlignment(index))
                    .padding(24.dp),
            )
        }
        Text(
            text = "نقشه زنده",
            color = MaterialTheme.colorScheme.secondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.Center)
                .background(Color.White.copy(alpha = 0.82f), MaterialTheme.shapes.small)
                .padding(horizontal = 14.dp, vertical = 8.dp),
        )
    }
}

private fun markerAlignment(index: Int): Alignment {
    return when (index % 5) {
        0 -> Alignment.Center
        1 -> Alignment.TopStart
        2 -> Alignment.TopEnd
        3 -> Alignment.BottomStart
        else -> Alignment.BottomEnd
    }
}

@Composable
private fun MapMarker(
    label: String,
    selected: Boolean,
    hasPosition: Boolean,
    modifier: Modifier = Modifier,
) {
    val fill = when {
        selected -> MaterialTheme.colorScheme.primary
        hasPosition -> Color(0xFF22A566)
        else -> Color(0xFF8A98A8)
    }
    Text(
        text = label.ifBlank { "خ" },
        color = Color.White,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = modifier
            .background(fill, MaterialTheme.shapes.extraLarge)
            .padding(horizontal = 12.dp, vertical = 9.dp),
    )
}
