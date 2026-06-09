package com.example.uzradyab.presentation.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.uzradyab.ui.theme.AppBackground

@Composable
fun HomeMapRoute(
    onSignedOut: () -> Unit,
    onEventsClick: () -> Unit,
    onAddDeviceClick: () -> Unit,
    onDeviceSpecsClick: (Long) -> Unit,
    onDeviceSettingsClick: (Long) -> Unit,
    onReplayTripClick: (Long) -> Unit,
    onCommandsClick: (Long) -> Unit,
    onReportsClick: () -> Unit,
    onAlertsSettingsClick: () -> Unit,
    onDebugLogsClick: (() -> Unit)? = null,
    viewModel: MapViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.signedOut) {
        if (state.signedOut) {
            onSignedOut()
        }
    }

    HomeMapScreen(
        state = state,
        onDeviceClick = viewModel::selectDevice,
        onToggleDevices = viewModel::toggleDevices,
        onOpenMapSettings = viewModel::openMapSettings,
        onCloseMapSettings = viewModel::closeMapSettings,
        onToggleDeviceCard = viewModel::toggleDeviceCard,
        onManageDeviceClick = viewModel::openDeviceManagement,
        onCloseDeviceManagement = viewModel::closeDeviceManagement,
        onEventsClick = onEventsClick,
        onLogoutClick = viewModel::logout,
        onAddDeviceClick = onAddDeviceClick,
        onDeviceSpecsClick = onDeviceSpecsClick,
        onDeviceSettingsClick = onDeviceSettingsClick,
        onReplayTripClick = onReplayTripClick,
        onCommandsClick = onCommandsClick,
        onReportsClick = onReportsClick,
        onAlertsSettingsClick = onAlertsSettingsClick,
        onDebugLogsClick = onDebugLogsClick,
    )
}

@Composable
fun HomeMapScreen(
    state: HomeMapUiState,
    onDeviceClick: (Long) -> Unit,
    onToggleDevices: () -> Unit,
    onOpenMapSettings: () -> Unit,
    onCloseMapSettings: () -> Unit,
    onToggleDeviceCard: () -> Unit,
    onManageDeviceClick: () -> Unit,
    onCloseDeviceManagement: () -> Unit,
    onEventsClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onAddDeviceClick: () -> Unit,
    onDeviceSpecsClick: (Long) -> Unit,
    onDeviceSettingsClick: (Long) -> Unit,
    onReplayTripClick: (Long) -> Unit,
    onCommandsClick: (Long) -> Unit,
    onReportsClick: () -> Unit,
    onAlertsSettingsClick: () -> Unit,
    onDebugLogsClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val selectedDevice = state.devices.firstOrNull { it.id == state.selectedDeviceId }
    val selectedPosition = state.latestPositions[state.selectedDeviceId]
    var menuOpen by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = AppBackground,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
        ) {
            MapTopToolbar(
                onMenuClick = { menuOpen = true },
                modifier = Modifier
                    .statusBarsPadding()
                    .height(64.dp),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(MaterialTheme.colorScheme.background),
            ) {
                val targetMapPadding = when {
                    state.deviceManagementOpen -> 446.dp
                    selectedDevice != null -> if (state.deviceCardExpanded) 240.dp else 163.dp
                    else -> 0.dp
                }
                val mapBottomPadding by androidx.compose.animation.core.animateDpAsState(
                    targetValue = targetMapPadding,
                    animationSpec = androidx.compose.animation.core.tween(durationMillis = 260),
                    label = "mapBottomPadding",
                )

                TrackingMap(
                    devices = state.devices,
                    latestPositions = state.latestPositions,
                    selectedDeviceId = state.selectedDeviceId,
                    mapBottomPadding = mapBottomPadding,
                    onMapClick = {
                        if (state.deviceManagementOpen) {
                            onCloseDeviceManagement()
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                )
                MapTopControls(
                    devices = state.devices,
                    selectedDeviceId = state.selectedDeviceId,
                    latestEvent = state.latestEvent,
                    onDeviceSelectorClick = onToggleDevices,
                    onSettingsClick = onOpenMapSettings,
                    onEventsClick = onEventsClick,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp),
                )
                if (state.devicesOpen) {
                    DeviceSelectDialog(
                        devices = state.devices,
                        selectedDeviceId = state.selectedDeviceId,
                        onDeviceClick = onDeviceClick,
                        onDismiss = onToggleDevices,
                    )
                }
                if (state.mapSettingsOpen) {
                    MapSettingsDialog(onDismiss = onCloseMapSettings)
                }
                if (menuOpen) {
                    AppMenuDialog(
                        onDismiss = { menuOpen = false },
                        onLogoutClick = onLogoutClick,
                        onAddDeviceClick = onAddDeviceClick,
                        onReportsClick = onReportsClick,
                        onAlertsSettingsClick = onAlertsSettingsClick,
                        onDebugLogsClick = onDebugLogsClick,
                    )
                }
                if (selectedDevice != null) {
                    if (state.deviceManagementOpen) {
                        DeviceManagementPanel(
                            device = selectedDevice,
                            position = selectedPosition,
                            todayDistanceText = state.todayDistanceText,
                            onDeviceSpecsClick = { onDeviceSpecsClick(selectedDevice.id) },
                            onDeviceSettingsClick = { onDeviceSettingsClick(selectedDevice.id) },
                            onReplayTripClick = { onReplayTripClick(selectedDevice.id) },
                            onCommandsClick = { onCommandsClick(selectedDevice.id) },
                            onReportsClick = onReportsClick,
                            onAlertsSettingsClick = onAlertsSettingsClick,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .navigationBarsPadding(),
                        )
                        
                        // Bottom Navigation
                        AppBottomNavigation(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .navigationBarsPadding()
                                .padding(bottom = 8.dp),
                            onItemSelected = { item ->
                                when (item) {
                                    BottomNavItem.ALARM -> onEventsClick()
                                    BottomNavItem.MANAGEMENT -> {} // Currently here
                                    BottomNavItem.MAP -> {
                                        if (state.deviceManagementOpen) {
                                            onCloseDeviceManagement()
                                        }
                                    }
                                    BottomNavItem.ACCOUNT -> {} // TODO
                                }
                            }
                        )
                    } else {
                        SelectedDeviceStatusCard(
                            device = selectedDevice,
                            position = selectedPosition,
                            todayDistanceText = state.todayDistanceText,
                            expanded = state.deviceCardExpanded,
                            onToggleExpanded = onToggleDeviceCard,
                            onManageClick = onManageDeviceClick,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .navigationBarsPadding()
                                .padding(bottom = 16.dp),
                        )
                    }
                }
            }
        }
    }
}
