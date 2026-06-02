package com.example.uzradyab.presentation.map

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uzradyab.domain.model.Device
import com.example.uzradyab.domain.model.Position
import com.example.uzradyab.ui.theme.AppTextMuted
import com.example.uzradyab.ui.theme.AppTextPrimary

@Composable
fun DeviceListSheet(
    devices: List<Device>,
    latestPositions: Map<Long, Position>,
    selectedDeviceId: Long?,
    onDeviceClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxHeight()
            .widthIn(max = 360.dp),
        color = Color.White.copy(alpha = 0.96f),
        shadowElevation = 8.dp,
    ) {
        Column(modifier = Modifier.padding(top = 80.dp, start = 12.dp, end = 12.dp)) {
            Text(
                text = "دستگاه‌ها",
                color = AppTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(devices, key = { it.id }) { device ->
                    DeviceRowCard(
                        device = device,
                        position = latestPositions[device.id],
                        selected = device.id == selectedDeviceId,
                        onClick = { onDeviceClick(device.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun DeviceRowCard(
    device: Device,
    position: Position?,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        border = if (selected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End,
            ) {
                Text(
                    text = device.name,
                    color = AppTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = position?.serverTime ?: device.lastUpdate ?: formatStatus(device.status),
                    color = AppTextMuted,
                    fontSize = 11.sp,
                    maxLines = 1,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Surface(
                shape = CircleShape,
                color = statusColor(device.status),
                modifier = Modifier.width(42.dp).height(42.dp),
            ) {
                Text(
                    text = device.name.take(1).ifBlank { "خ" },
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 10.dp),
                )
            }
        }
    }
}

private fun statusColor(status: String): Color = when (status) {
    "online" -> Color(0xFF22A566)
    "offline" -> Color(0xFFE45353)
    else -> Color(0xFF8A98A8)
}

private fun formatStatus(status: String): String = when (status) {
    "online" -> "دستگاه آنلاین"
    "offline" -> "دستگاه آفلاین"
    else -> "وضعیت نامشخص"
}
