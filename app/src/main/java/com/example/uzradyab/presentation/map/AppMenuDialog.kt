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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
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
import com.example.uzradyab.BuildConfig

@Composable
fun AppMenuDialog(
    onDismiss: () -> Unit,
    onLogoutClick: () -> Unit,
    onAddDeviceClick: () -> Unit,
    onReportsClick: () -> Unit = {},
    onDebugLogsClick: (() -> Unit)? = null,
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
                // Right side: Title
                Text(
                    text = "تنظیمات زبان برنامه",
                    color = Color(0xFF333638),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Right,
                )

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
            }

            // Group 2: Main Menu Items (1-4)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                MenuCardItem(
                    label = "افزودن دستگاه جدید",
                    icon = Icons.Default.Add,
                    onClick = {
                        onAddDeviceClick()
                        onDismiss()
                    }
                )
                MenuCardItem(
                    label = "سرویس‌های دوره‌ای",
                    icon = Icons.Default.Assignment,
                    onClick = onDismiss
                )
                MenuCardItem(
                    label = "گزارش‌ها",
                    icon = Icons.Default.Description,
                    onClick = {
                        onReportsClick()
                        onDismiss()
                    }
                )
                MenuCardItem(
                    label = "محدوده‌های جغرافیایی",
                    icon = Icons.Default.Layers,
                    onClick = onDismiss
                )
            }

            // Group 3: Support Items (5-6) + Logout + Debug (if debug build)
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
                    icon = Icons.AutoMirrored.Filled.ExitToApp,
                    onClick = {
                        onLogoutClick()
                        onDismiss()
                    }
                )
                if (BuildConfig.DEBUG && onDebugLogsClick != null) {
                    DebugLogsMenuItem(
                        onClick = {
                            onDebugLogsClick()
                            onDismiss()
                        }
                    )
                }
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
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (color == Color(0xFFE55353)) color else Color(0xFFAEB1B4),
            modifier = Modifier.size(24.dp)
        )

        Text(
            text = label,
            color = color,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Right,
            modifier = Modifier
                .weight(1f)
                .padding(start = 6.dp)
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

@Composable
private fun DebugLogsMenuItem(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(Color(0xFF1C2128), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Bug emoji badge
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(Color(0xFF30363D), RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text("🐛", fontSize = 14.sp)
        }

        Text(
            text = "لاگ‌های شبکه  [DEBUG]",
            color = Color(0xFF58A6FF),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Right,
            modifier = Modifier
                .weight(1f)
                .padding(start = 6.dp),
        )
    }
}
