package com.example.uzradyab.presentation.reports

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.uzradyab.core.designsystem.SkeletonBox
import com.example.uzradyab.presentation.map.AppMenuDialog
import com.example.uzradyab.presentation.map.AppTopToolbar
import com.example.uzradyab.presentation.map.BackButton
import com.example.uzradyab.presentation.map.DeviceSelectDialog
import com.example.uzradyab.presentation.map.MenuGridButton

@Composable
fun DeviceStatusRoute(
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onAddDeviceClick: () -> Unit,
    onTraveledPathsClick: () -> Unit,
    viewModel: DeviceStatusViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    DeviceStatusScreen(
        state = uiState,
        onBackClick = onBackClick,
        onLogoutClick = onLogoutClick,
        onAddDeviceClick = onAddDeviceClick,
        onDeviceSelect = viewModel::selectDevice,
        onTraveledPathsClick = onTraveledPathsClick,
        onExportClick = { /* TODO: Export logic */ }
    )
}

@Composable
fun DeviceStatusScreen(
    state: DeviceStatusUiState,
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onAddDeviceClick: () -> Unit,
    onDeviceSelect: (Long) -> Unit,
    onTraveledPathsClick: () -> Unit,
    onExportClick: () -> Unit
) {
    val figmaBackground = Color(0xFFF3F4F6)
    var menuOpen by remember { mutableStateOf(false) }
    var deviceSelectorOpen by remember { mutableStateOf(false) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                AppTopToolbar(
                    startContent = { BackButton(onClick = onBackClick) },
                    centerContent = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "گزارشات",
                                color = Color(0xFF676C70),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                            )
                            Icon(
                                imageVector = Icons.Default.EventNote,
                                contentDescription = null,
                                tint = Color(0xFF676C70),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    endContent = { MenuGridButton(onClick = { menuOpen = true }) },
                    modifier = Modifier
                        .statusBarsPadding()
                        .height(64.dp)
                )
            },
            containerColor = figmaBackground,
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(figmaBackground)
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        val selectedDevice = state.devices.firstOrNull { it.id == state.selectedDeviceId }
                        DeviceSelectTrigger(
                            text = selectedDevice?.name ?: "انتخاب دستگاه",
                            onClick = { deviceSelectorOpen = true },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF9B26B6), RoundedCornerShape(12.dp))
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "وضعیت جاری دستگاه",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsCar,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = state.deviceStatusText,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        LocationCard(
                            title = "موقعیت",
                            address = state.currentAddress,
                            icon = Icons.Default.GpsFixed,
                            isLoading = state.isLoading && state.currentAddress == "در حال دریافت..."
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            DetailStatCard(
                                modifier = Modifier.weight(1f),
                                title = "اولین زمان روشن شدن",
                                value = state.firstIgnitionTime,
                                unit = "",
                                icon = Icons.Default.FlashOn,
                                isLoading = state.isLoading && state.firstIgnitionTime == "- : -"
                            )
                            DetailStatCard(
                                modifier = Modifier.weight(1f),
                                title = "مدت روشن بودن دستگاه",
                                value = state.ignitionDuration,
                                unit = "",
                                icon = Icons.Default.AccessTime,
                                isLoading = state.isLoading && state.ignitionDuration.contains("- ساعت")
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            DetailStatCard(
                                modifier = Modifier.weight(1f),
                                title = "میانگین سرعت",
                                value = state.averageSpeed,
                                unit = "کیلومتر",
                                icon = Icons.Default.Speed,
                                isLoading = state.isLoading && state.averageSpeed == "۰"
                            )
                            DetailStatCard(
                                modifier = Modifier.weight(1f),
                                title = "مصرف سوخت",
                                value = state.spentFuel,
                                unit = "لیتر",
                                icon = Icons.Default.LocalGasStation,
                                isLoading = state.isLoading && state.spentFuel == "۰"
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            DetailStatCard(
                                modifier = Modifier.weight(1f),
                                title = "شروع کیلومترشمار",
                                value = state.startOdometer,
                                unit = "کیلومتر",
                                icon = Icons.Outlined.Timer,
                                isLoading = state.isLoading && state.startOdometer == "۰"
                            )
                            DetailStatCard(
                                modifier = Modifier.weight(1f),
                                title = "پایان کیلومترشمار",
                                value = state.endOdometer,
                                unit = "کیلومتر",
                                icon = Icons.Outlined.Timer,
                                isLoading = state.isLoading && state.endOdometer == "۰"
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Row(
                    //     modifier = Modifier
                    //         .fillMaxWidth()
                    //         .background(figmaBackground)
                    //         .padding(16.dp),
                    //     horizontalArrangement = Arrangement.spacedBy(12.dp)
                    // ) {
                    //     OutlinedButton(
                    //         onClick = onExportClick,
                    //         modifier = Modifier
                    //             .weight(1f)
                    //             .height(52.dp),
                    //         shape = RoundedCornerShape(12.dp),
                    //         colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF307EF3)),
                    //         border = BorderStroke(1.dp, Color(0xFF307EF3))
                    //     ) {
                    //         Text("خروجی", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    //     }

                    //     Button(
                    //         onClick = onTraveledPathsClick,
                    //         modifier = Modifier
                    //             .weight(2f)
                    //             .height(52.dp),
                    //         shape = RoundedCornerShape(12.dp),
                    //         colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF307EF3))
                    //     ) {
                    //         Text("مسیرهای پیموده شده", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    //     }
                    // }
                }

                if (menuOpen) {
                    AppMenuDialog(
                        onDismiss = { menuOpen = false },
                        onLogoutClick = onLogoutClick,
                        onAddDeviceClick = onAddDeviceClick
                    )
                }

                if (deviceSelectorOpen) {
                    DeviceSelectDialog(
                        devices = state.devices,
                        selectedDeviceId = state.selectedDeviceId,
                        onDeviceClick = { deviceId ->
                            onDeviceSelect(deviceId)
                            deviceSelectorOpen = false
                        },
                        onDismiss = { deviceSelectorOpen = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun LocationCard(title: String, address: String, icon: ImageVector, isLoading: Boolean = false) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE3E8EE)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF307EF3),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = title,
                    color = Color(0xFF8F99A3),
                    fontSize = 12.sp
                )
            }
            if (isLoading) {
                SkeletonBox(modifier = Modifier.fillMaxWidth().height(18.dp))
            } else {
                Text(
                    text = address,
                    color = Color(0xFF333638),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun DetailStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    unit: String,
    icon: ImageVector,
    isLoading: Boolean = false
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE3E8EE)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF307EF3),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = title,
                    color = Color(0xFF8F99A3),
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (isLoading) {
                SkeletonBox(modifier = Modifier.width(60.dp).height(16.dp).align(Alignment.End))
            } else {
                val displayText = if (unit.isNotEmpty()) "$value $unit" else value
                Text(
                    text = displayText,
                    color = Color(0xFF333638),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun DeviceSelectTrigger(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(44.dp)
            .shadow(18.dp, RoundedCornerShape(8.dp), clip = false)
            .background(Color.White, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.DirectionsCar,
            contentDescription = "Car",
            tint = Color.Black,
            modifier = Modifier.size(width = 20.dp, height = 16.dp)
        )
        Text(
            text = text,
            color = Color.Black,
            fontSize = 14.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Right,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
        )
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = "Chevron Down",
            tint = Color(0xFF1C262E),
            modifier = Modifier.size(24.dp)
        )
    }
}
