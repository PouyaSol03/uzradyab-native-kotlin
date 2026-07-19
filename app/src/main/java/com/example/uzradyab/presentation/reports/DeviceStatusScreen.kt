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
import com.example.uzradyab.core.utils.toImmutable
import com.example.uzradyab.R
import androidx.compose.ui.res.stringResource
import com.example.uzradyab.ui.theme.themedColor

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
    val figmaBackground = themedColor(light = Color(0xFFF3F4F6), dark = Color(0xFF1A1D23))
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
                                text = stringResource(R.string.str_3d9b4b4a),
                                color = themedColor(light = Color(0xFF676C70), dark = Color(0xFF929292)),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                            )
                            Icon(
                                imageVector = Icons.Default.EventNote,
                                contentDescription = null,
                                tint = themedColor(light = Color(0xFF676C70), dark = Color(0xFF929292)),
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
                        val selectedDevice = remember(state.devices, state.selectedDeviceId) {
                            state.devices.firstOrNull { it.id == state.selectedDeviceId }
                        }
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
                                .background(themedColor(light = Color(0xFF9B26B6), dark = Color(0xFFB959CF)), RoundedCornerShape(12.dp))
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.str_cf441594),
                                color = themedColor(light = Color.White, dark = Color.White),
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
                                    tint = themedColor(light = Color.White, dark = Color.White),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = state.deviceStatusText,
                                    color = themedColor(light = Color.White, dark = Color.White),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        LocationCard(
                            title = stringResource(R.string.str_6ba39ff9),
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
                                title = stringResource(R.string.str_21c081b4),
                                value = state.firstIgnitionTime,
                                unit = "",
                                icon = Icons.Default.FlashOn,
                                isLoading = state.isLoading && state.firstIgnitionTime == "- : -"
                            )
                            DetailStatCard(
                                modifier = Modifier.weight(1f),
                                title = stringResource(R.string.str_3d6f575f),
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
                                title = stringResource(R.string.str_0ddc0842),
                                value = state.averageSpeed,
                                unit = "کیلومتر",
                                icon = Icons.Default.Speed,
                                isLoading = state.isLoading && state.averageSpeed == "۰"
                            )
                            DetailStatCard(
                                modifier = Modifier.weight(1f),
                                title = stringResource(R.string.str_1b7dc619),
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
                                title = stringResource(R.string.str_57396a22),
                                value = state.startOdometer,
                                unit = "کیلومتر",
                                icon = Icons.Outlined.Timer,
                                isLoading = state.isLoading && state.startOdometer == "۰"
                            )
                            DetailStatCard(
                                modifier = Modifier.weight(1f),
                                title = stringResource(R.string.str_675d2703),
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
                    //         colors = ButtonDefaults.outlinedButtonColors(contentColor = themedColor(light = Color(0xFF307EF3), dark = Color(0xFF5F98EC))),
                    //         border = BorderStroke(1.dp, themedColor(light = Color(0xFF307EF3), dark = Color(0xFF5F98EC)))
                    //     ) {
                    //         Text(stringResource(R.string.str_f792dea4), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    //     }

                    //     Button(
                    //         onClick = onTraveledPathsClick,
                    //         modifier = Modifier
                    //             .weight(2f)
                    //             .height(52.dp),
                    //         shape = RoundedCornerShape(12.dp),
                    //         colors = ButtonDefaults.buttonColors(containerColor = themedColor(light = Color(0xFF307EF3), dark = Color(0xFF5F98EC)))
                    //     ) {
                    //         Text(stringResource(R.string.str_fbcc4761), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themedColor(light = Color.White, dark = Color.White))
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
fun LocationCard(title: String, address: String, icon: ImageVector, isLoading: Boolean = false) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = themedColor(light = Color.White, dark = Color(0xFF27343F))),
        border = BorderStroke(1.dp, themedColor(light = Color(0xFFE3E8EE), dark = Color(0xFF171E26))),
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
                    tint = themedColor(light = Color(0xFF307EF3), dark = Color(0xFF5F98EC)),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = title,
                    color = themedColor(light = Color(0xFF8F99A3), dark = Color(0xFFA6A6A6)),
                    fontSize = 12.sp
                )
            }
            if (isLoading) {
                SkeletonBox(modifier = Modifier.fillMaxWidth().height(18.dp))
            } else {
                Text(
                    text = address,
                    color = themedColor(light = Color(0xFF333638), dark = Color(0xFFAFB3B6)),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun DetailStatCard(
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
        colors = CardDefaults.cardColors(containerColor = themedColor(light = Color.White, dark = Color(0xFF27343F))),
        border = BorderStroke(1.dp, themedColor(light = Color(0xFFE3E8EE), dark = Color(0xFF171E26))),
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
                    tint = themedColor(light = Color(0xFF307EF3), dark = Color(0xFF5F98EC)),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = title,
                    color = themedColor(light = Color(0xFF8F99A3), dark = Color(0xFFA6A6A6)),
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
                    color = themedColor(light = Color(0xFF333638), dark = Color(0xFFAFB3B6)),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}


