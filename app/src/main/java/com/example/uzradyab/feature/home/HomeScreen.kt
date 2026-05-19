package com.example.uzradyab.feature.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.uzradyab.core.model.Device
import com.example.uzradyab.core.model.Position
import com.example.uzradyab.ui.theme.AppBackground
import com.example.uzradyab.ui.theme.AppTextBody
import com.example.uzradyab.ui.theme.AppTextMuted
import com.example.uzradyab.ui.theme.AppTextPrimary

@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
    onSignedOut: () -> Unit,
    viewModel: HomeViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.signedOut) {
        if (state.signedOut) {
            onSignedOut()
        }
    }

    HomeScreen(
        state = state,
        onRefreshClick = viewModel::refresh,
        onSignOutClick = viewModel::signOut,
        onDeviceClick = viewModel::selectDevice,
        modifier = modifier,
    )
}

@Composable
fun HomeScreen(
    state: HomeUiState,
    onRefreshClick: () -> Unit,
    onSignOutClick: () -> Unit,
    onDeviceClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = AppBackground,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            HomeHeader(
                userName = state.user?.name.orEmpty(),
                isSigningOut = state.isSigningOut,
                onRefreshClick = onRefreshClick,
                onSignOutClick = onSignOutClick,
            )

            if (state.errorMessage != null) {
                Text(
                    text = state.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            val selectedDevice = state.devices.firstOrNull { it.id == state.selectedDeviceId }
            val selectedPosition = state.positions[state.selectedDeviceId]
            MapSummaryCard(
                device = selectedDevice,
                position = selectedPosition,
                isLoading = state.isLoading,
            )

            Text(
                text = "دستگاه‌ها",
                color = AppTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Right,
            )

            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(state.devices, key = { it.id }) { device ->
                        DeviceRowCard(
                            device = device,
                            position = state.positions[device.id],
                            selected = device.id == state.selectedDeviceId,
                            onClick = { onDeviceClick(device.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(
    userName: String,
    isSigningOut: Boolean,
    onRefreshClick: () -> Unit,
    onSignOutClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = onSignOutClick,
                enabled = !isSigningOut,
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(if (isSigningOut) "خروج..." else "خروج")
            }
            OutlinedButton(
                onClick = onRefreshClick,
                shape = RoundedCornerShape(8.dp),
            ) {
                Text("به‌روزرسانی")
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "خانه",
                color = AppTextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            )
            if (userName.isNotBlank()) {
                Text(
                    text = userName,
                    color = AppTextMuted,
                    fontSize = 12.sp,
                )
            }
        }
    }
}

@Composable
private fun MapSummaryCard(
    device: Device?,
    position: Position?,
    isLoading: Boolean,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(18.dp),
                horizontalAlignment = Alignment.End,
            ) {
                Text(
                    text = device?.name ?: "نقشه زنده",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = when {
                        isLoading -> "در حال دریافت دستگاه‌ها..."
                        position != null -> "آخرین موقعیت دریافت شد"
                        device != null -> "موقعیت فعلی این دستگاه موجود نیست"
                        else -> "هنوز دستگاهی برای نمایش وجود ندارد"
                    },
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f),
                    fontSize = 13.sp,
                )
            }

            if (position != null) {
                CoordinatePanel(
                    position = position,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp),
                )
            }
        }
    }
}

@Composable
private fun CoordinatePanel(
    position: Position,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "%.6f, %.6f".format(position.latitude, position.longitude),
                    color = AppTextBody,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "speed: %.1f kn".format(position.speed),
                    color = AppTextMuted,
                    fontSize = 11.sp,
                )
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
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        border = if (selected) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        } else {
            null
        },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatusBadge(status = device.status)
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End,
            ) {
                Text(
                    text = device.name,
                    color = AppTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = position?.serverTime ?: device.lastUpdate ?: device.uniqueId,
                    color = AppTextMuted,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val color = when (status) {
        "online" -> MaterialTheme.colorScheme.primary
        "offline" -> MaterialTheme.colorScheme.error
        else -> AppTextMuted
    }
    OutlinedButton(
        onClick = {},
        enabled = false,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.outlinedButtonColors(disabledContentColor = color),
        border = BorderStroke(1.dp, color.copy(alpha = 0.35f)),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
    ) {
        Text(
            text = when (status) {
                "online" -> "آنلاین"
                "offline" -> "آفلاین"
                else -> "نامشخص"
            },
            fontSize = 11.sp,
        )
    }
}
