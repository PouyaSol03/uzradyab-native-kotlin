package com.example.uzradyab.presentation.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.uzradyab.ui.theme.AppBackground

@Composable
fun HomeMapRoute(
    onSignedOut: () -> Unit,
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
        onToggleDeviceCard = viewModel::toggleDeviceCard,
        onManageDeviceClick = viewModel::openDeviceManagement,
        onMapTabClick = viewModel::closeDeviceManagement,
        onLogoutClick = viewModel::logout,
    )
}

@Composable
fun HomeMapScreen(
    state: HomeMapUiState,
    onDeviceClick: (Long) -> Unit,
    onToggleDevices: () -> Unit,
    onToggleDeviceCard: () -> Unit,
    onManageDeviceClick: () -> Unit,
    onMapTabClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedDevice = state.devices.firstOrNull { it.id == state.selectedDeviceId }
    val selectedPosition = state.latestPositions[state.selectedDeviceId]

    Surface(
        modifier = modifier.fillMaxSize(),
        color = AppBackground,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            TrackingMap(
                devices = state.devices,
                latestPositions = state.latestPositions,
                selectedDeviceId = state.selectedDeviceId,
                modifier = Modifier.fillMaxSize(),
            )
            MapTopToolbar(
                onMenuClick = onLogoutClick,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 16.dp),
            )
            if (state.devicesOpen) {
                DeviceListSheet(
                    devices = state.devices,
                    latestPositions = state.latestPositions,
                    selectedDeviceId = state.selectedDeviceId,
                    onDeviceClick = onDeviceClick,
                    modifier = Modifier.align(Alignment.CenterEnd),
                )
            }
            if (selectedDevice != null) {
                if (state.deviceManagementOpen) {
                    DeviceManagementPanel(
                        device = selectedDevice,
                        position = selectedPosition,
                        todayDistanceText = state.todayDistanceText,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .navigationBarsPadding()
                            .padding(bottom = 92.dp),
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
                            .padding(bottom = 92.dp),
                    )
                }
            }
            HomeBottomMenu(
                selectedItem = if (state.deviceManagementOpen) HomeBottomItem.Management else HomeBottomItem.Map,
                onEventsClick = {},
                onManagementClick = onManageDeviceClick,
                onMapClick = onMapTabClick,
                onAccountClick = {},
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 18.dp),
            )
        }
    }
}
