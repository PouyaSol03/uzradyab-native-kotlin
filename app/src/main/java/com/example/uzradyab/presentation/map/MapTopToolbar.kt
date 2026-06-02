package com.example.uzradyab.presentation.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uzradyab.domain.model.TrackingConnectionState
import com.example.uzradyab.ui.theme.AppTextPrimary

@Composable
fun MapTopToolbar(
    devicesOpen: Boolean,
    connectionState: TrackingConnectionState,
    onToggleDevices: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.94f))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "خروج",
            color = AppTextPrimary,
            fontSize = 12.sp,
            modifier = Modifier
                .background(Color(0xFFF1F5F9), RoundedCornerShape(10.dp))
                .clickable(onClick = onLogoutClick)
                .padding(horizontal = 12.dp, vertical = 8.dp),
        )
        Text(
            text = "اوزرادیاب",
            color = AppTextPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = if (devicesOpen) "نقشه" else "لیست",
            color = AppTextPrimary,
            fontSize = 12.sp,
            modifier = Modifier
                .background(Color(0xFFF1F5F9), RoundedCornerShape(10.dp))
                .clickable(onClick = onToggleDevices)
                .padding(horizontal = 12.dp, vertical = 8.dp),
        )
    }
}
