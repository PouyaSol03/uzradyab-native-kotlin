package com.example.uzradyab.presentation.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun AppMenuDialog(
    onDismiss: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = modifier
                .width(327.dp)
                .padding(vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Group 1: Language Settings
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(Color.White, RoundedCornerShape(10.dp))
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Left side: Language selection
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "فارسی",
                        color = Color(0xFF333638),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    IranFlagIcon()
                }

                // Right side: Title
                Text(
                    text = "تنظیمات زبان برنامه",
                    color = Color(0xFF333638),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Right,
                )
            }

            // Group 2: Main Menu Items (1-4)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                MenuCardItem(
                    label = "افزودن دستگاه جدید",
                    icon = Icons.Default.Add,
                    onClick = onDismiss
                )
                MenuCardItem(
                    label = "سرویس‌های دوره‌ای",
                    icon = Icons.Default.Assignment,
                    onClick = onDismiss
                )
                MenuCardItem(
                    label = "گزارش‌ها",
                    icon = Icons.Default.Description,
                    onClick = onDismiss
                )
                MenuCardItem(
                    label = "محدوده‌های جغرافیایی",
                    icon = Icons.Default.Layers,
                    onClick = onDismiss
                )
            }

            // Group 3: Support Items (5-6) + Logout (7)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                MenuCardItem(
                    label = "درباره اکسیر",
                    icon = Icons.Default.Business,
                    onClick = onDismiss
                )
                MenuCardItem(
                    label = "اطلاعات تماس و پشتیبانی",
                    icon = Icons.Default.HeadsetMic,
                    onClick = onDismiss
                )
                MenuCardItem(
                    label = "خروج از حساب کاربری",
                    icon = Icons.Default.ExitToApp,
                    color = Color(0xFFE55353),
                    onClick = {
                        onLogoutClick()
                        onDismiss()
                    }
                )
            }
        }
    }
}

@Composable
private fun MenuCardItem(
    label: String,
    icon: ImageVector,
    color: Color = Color(0xFF333638),
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(Color.White, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Right,
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp)
        )
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (color == Color(0xFFE55353)) color else Color(0xFFAEB1B4),
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun IranFlagIcon() {
    Canvas(modifier = Modifier.size(width = 28.dp, height = 18.dp)) {
        drawRoundRect(
            color = Color.White,
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
        )
        drawRect(
            color = Color(0xFF239F40),
            size = androidx.compose.ui.geometry.Size(size.width, size.height / 3f),
        )
        drawRect(
            color = Color(0xFFDA0000),
            topLeft = Offset(0f, size.height * 2f / 3f),
            size = androidx.compose.ui.geometry.Size(size.width, size.height / 3f),
        )
        drawRoundRect(
            color = Color(0xFF333638),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
            style = Stroke(width = 1.dp.toPx()),
        )
    }
}
