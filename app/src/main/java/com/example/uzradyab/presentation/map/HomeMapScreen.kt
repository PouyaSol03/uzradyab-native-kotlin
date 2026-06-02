package com.example.uzradyab.presentation.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
        onLogoutClick = viewModel::logout,
    )
}

@Composable
fun HomeMapScreen(
    state: HomeMapUiState,
    onDeviceClick: (Long) -> Unit,
    onToggleDevices: () -> Unit,
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
                devicesOpen = state.devicesOpen,
                connectionState = state.connectionState,
                onToggleDevices = onToggleDevices,
                onLogoutClick = onLogoutClick,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 18.dp),
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
                SelectedDeviceStatusCard(
                    device = selectedDevice,
                    position = selectedPosition,
                    todayDistanceText = state.todayDistanceText,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 92.dp),
                )
            }
            HomeBottomMenu(
                onDevicesClick = onToggleDevices,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 18.dp),
            )
        }
    }
}
