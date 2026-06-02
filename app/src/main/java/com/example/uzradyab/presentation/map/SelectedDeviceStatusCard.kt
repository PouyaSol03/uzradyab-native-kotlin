package com.example.uzradyab.presentation.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.uzradyab.presentation.common.UzradyabPrimaryButton
import com.example.uzradyab.ui.theme.AppBackground
import com.example.uzradyab.ui.theme.AppTextMuted
import com.example.uzradyab.ui.theme.AppTextPrimary

@Composable
fun SelectedDeviceStatusCard(
    device: Device,
    position: Position?,
    todayDistanceText: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth(0.92f)
            .widthIn(max = 343.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(107.dp)
                    .height(6.dp)
                    .background(Color(0xFFE6EEF5), RoundedCornerShape(99.dp)),
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SignalBlock(label = "GSM", active = position != null)
                    SignalBlock(label = "GPS", active = position != null)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = device.name,
                        color = AppTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Right,
                    )
                    Text(
                        text = "آخرین بروزرسانی: ${position?.serverTime ?: device.lastUpdate ?: "نامشخص"}",
                        color = AppTextMuted,
                        fontSize = 11.sp,
                        maxLines = 1,
                        textAlign = TextAlign.Right,
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppBackground, RoundedCornerShape(8.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "بازپخش مسیر",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                )
                Text(
                    text = "پیمایش امروز: $todayDistanceText",
                    color = AppTextPrimary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Right,
                )
            }
            device.expirationTime?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "وضعیت اعتبار دستگاه را بررسی کنید",
                    color = Color(0xFF9D3B00),
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFF0E6), RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    textAlign = TextAlign.Right,
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row {
                    IconButton(onClick = {}) { Text("↗", color = MaterialTheme.colorScheme.primary) }
                    IconButton(onClick = {}) { Text("⌖", color = MaterialTheme.colorScheme.primary) }
                }
                UzradyabPrimaryButton(
                    text = "مدیریت دستگاه",
                    onClick = {},
                    modifier = Modifier.width(180.dp),
                )
            }
        }
    }
}

@Composable
private fun SignalBlock(label: String, active: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = if (active) "▰▰▰" else "▱▱▱",
            color = if (active) Color(0xFF22A566) else AppTextMuted,
            fontSize = 11.sp,
        )
        Text(text = label, color = AppTextMuted, fontSize = 10.sp)
    }
}
