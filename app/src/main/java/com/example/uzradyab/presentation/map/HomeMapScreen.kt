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
    onEditDeviceClick: (Long) -> Unit,
    onReportsClick: () -> Unit,
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
        onEventsClick = onEventsClick,
        onLogoutClick = viewModel::logout,
        onAddDeviceClick = onAddDeviceClick,
        onEditDeviceClick = onEditDeviceClick,
        onReportsClick = onReportsClick,
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
    onEventsClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onAddDeviceClick: () -> Unit,
    onEditDeviceClick: (Long) -> Unit,
    onReportsClick: () -> Unit,
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
                TrackingMap(
                    devices = state.devices,
                    latestPositions = state.latestPositions,
                    selectedDeviceId = state.selectedDeviceId,
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
                        onDebugLogsClick = onDebugLogsClick,
                    )
                }
                if (selectedDevice != null) {
                    if (state.deviceManagementOpen) {
                        DeviceManagementPanel(
                            device = selectedDevice,
                            position = selectedPosition,
                            todayDistanceText = state.todayDistanceText,
                            onEditDeviceClick = { onEditDeviceClick(selectedDevice.id) },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .navigationBarsPadding()
                                .padding(bottom = 16.dp),
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
