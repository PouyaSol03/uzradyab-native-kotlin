package com.example.uzradyab.presentation.map

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
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
import com.example.uzradyab.R
import androidx.compose.ui.res.stringResource
import com.example.uzradyab.ui.theme.UzradyabTheme
import com.example.uzradyab.ui.theme.themedColor
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape

@Composable
fun AppMenuDialog(
    onDismiss: () -> Unit,
    onLogoutClick: () -> Unit,
    onAddDeviceClick: () -> Unit,
    onReportsClick: () -> Unit = {},
    onGeofencesClick: () -> Unit = {},
    onMaintenanceClick: () -> Unit = {},
    onAlertsSettingsClick: () -> Unit = {},
    onAboutClick: () -> Unit = {},
    onContactSupportClick: () -> Unit = {},
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
                    .background(themedColor(light = Color.White, dark = Color(0xFF27343F)), RoundedCornerShape(10.dp))
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Right side: Title
                Text(
                    text = stringResource(R.string.str_ace11fd5),
                    color = themedColor(light = Color(0xFF333638), dark = Color.White),
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
                        text = stringResource(R.string.str_66030b73),
                        color = themedColor(light = Color(0xFF333638), dark = Color.White),
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
                    label = stringResource(R.string.str_f4268cbb),
                    icon = Icons.Default.Add,
                    onClick = {
                        onAddDeviceClick()
                        onDismiss()
                    }
                )
                MenuCardItem(
                    label = stringResource(R.string.str_c2d21116),
                    icon = Icons.Default.Assignment,
                    onClick = {
                        onMaintenanceClick()
                        onDismiss()
                    }
                )
                MenuCardItem(
                    label = stringResource(R.string.str_036f8b42),
                    icon = Icons.Default.Description,
                    onClick = {
                        onReportsClick()
                        onDismiss()
                    }
                )
                MenuCardItem(
                    label = stringResource(R.string.str_6dca01e2),
                    icon = Icons.Default.Layers,
                    onClick = {
                        onGeofencesClick()
                        onDismiss()
                    }
                )
                MenuCardItem(
                    label = stringResource(R.string.str_34260011),
                    icon = Icons.Default.Assignment,
                    onClick = {
                        onAlertsSettingsClick()
                        onDismiss()
                    }
                )
            }

            // Group 3: Support Items (5-6) + Logout + Debug (if debug build)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                MenuCardItem(
                    label = stringResource(R.string.str_8105a2a5),
                    icon = Icons.Default.Business,
                    onClick = {
                        onAboutClick()
                        onDismiss()
                    }
                )
                MenuCardItem(
                    label = stringResource(R.string.str_1626c15e),
                    icon = Icons.Default.HeadsetMic,
                    onClick = {
                        onContactSupportClick()
                        onDismiss()
                    }
                )
                MenuCardItem(
                    label = stringResource(R.string.str_15e25a13),
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
    color: Color = themedColor(light = Color(0xFF333638), dark = Color.White),
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(themedColor(light = Color.White, dark = Color(0xFF27343F)), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (color == themedColor(light = Color(0xFFE55353), dark = Color(0xFF6F1111))) color else themedColor(light = Color(0xFFAEB1B4), dark = Color.White),
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
    val white = themedColor(light = Color.White, dark = Color(0xFF27343F))
    val green = themedColor(light = Color(0xFF239F40), dark = Color(0xFF82E398))
    val red = themedColor(light = Color(0xFFDA0000), dark = Color(0xFFF43232))
    val strokeColor = themedColor(light = Color(0xFF333638), dark = Color(0xFFAFB3B6))
    
    Canvas(modifier = Modifier.size(width = 28.dp, height = 18.dp)) {
        drawRoundRect(
            color = white,
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
        )
        drawRect(
            color = green,
            size = androidx.compose.ui.geometry.Size(size.width, size.height / 3f),
        )
        drawRect(
            color = red,
            topLeft = Offset(0f, size.height * 2f / 3f),
            size = androidx.compose.ui.geometry.Size(size.width, size.height / 3f),
        )
        drawRoundRect(
            color = strokeColor,
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
            .background(themedColor(light = Color(0xFF1C2128), dark = Color(0xFFC3CBD5)), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Bug emoji badge
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(themedColor(light = Color(0xFF30363D), dark = Color(0xFFA9B2BC)), RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text("🐛", fontSize = 14.sp)
        }

        Text(
            text = stringResource(R.string.str_24b95075),
            color = themedColor(light = Color(0xFF58A6FF), dark = Color(0xFF003C80)),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Right,
            modifier = Modifier
                .weight(1f)
                .padding(start = 6.dp),
        )
    }
}

