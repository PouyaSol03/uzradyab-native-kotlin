package com.example.uzradyab.presentation.map

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.os.Build
import android.Manifest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.uzradyab.map.tile.TileHealthState
import kotlinx.coroutines.launch
import com.example.uzradyab.R
import androidx.compose.ui.res.stringResource

@Composable
fun HomeMapRoute(
    onSignedOut: () -> Unit,
    onEventsClick: (Long?) -> Unit,
    onDevicesClick: () -> Unit,
    onProfileClick: () -> Unit,
    onAddDeviceClick: () -> Unit,
    onDeviceSpecsClick: (Long) -> Unit,
    onDeviceSettingsClick: (Long) -> Unit,
    onReplayTripClick: (Long) -> Unit,
    onCommandsClick: (Long) -> Unit,
    onReportsClick: () -> Unit,
    onAlertsSettingsClick: () -> Unit,
    onAboutClick: () -> Unit,
    onContactSupportClick: () -> Unit,
    onGeofenceClick: (Long) -> Unit,
    onDebugLogsClick: (() -> Unit)? = null,
    viewModel: MapViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Permission launcher for Push Notifications (Android 13+)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Could log or handle permission denial if needed
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

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
        onMapStyleSelected = viewModel::setMapStyle,
        onToggleDeviceCard = viewModel::toggleDeviceCard,
        onManageDeviceClick = viewModel::openDeviceManagement,
        onCloseDeviceManagement = viewModel::closeDeviceManagement,
        onTileHealthErrorConsumed = viewModel::consumeTileHealthError,
        onClearInfoMessage = viewModel::clearInfoMessage,
        onEventsClick = { onEventsClick(state.selectedDeviceId) },
        onDevicesClick = onDevicesClick,
        onProfileClick = onProfileClick,
        onLogoutClick = viewModel::logout,
        onAddDeviceClick = onAddDeviceClick,
        onDeviceSpecsClick = onDeviceSpecsClick,
        onDeviceSettingsClick = onDeviceSettingsClick,
        onReplayTripClick = onReplayTripClick,
        onCommandsClick = onCommandsClick,
        onReportsClick = onReportsClick,
        onAlertsSettingsClick = onAlertsSettingsClick,
        onAboutClick = onAboutClick,
        onContactSupportClick = onContactSupportClick,
        onGeofenceClick = onGeofenceClick,
        onToggleMapLock = viewModel::toggleMapLock,
        onDismissServerDown = viewModel::dismissServerDown,
    )
}

@Composable
fun HomeMapScreen(
    state: HomeMapUiState,
    onDeviceClick: (Long) -> Unit,
    onToggleDevices: () -> Unit,
    onOpenMapSettings: () -> Unit,
    onCloseMapSettings: () -> Unit,
    onMapStyleSelected: (String) -> Unit,
    onToggleDeviceCard: () -> Unit,
    onManageDeviceClick: () -> Unit,
    onCloseDeviceManagement: () -> Unit,
    onTileHealthErrorConsumed: () -> Unit,
    onClearInfoMessage: () -> Unit,
    onEventsClick: () -> Unit,
    onDevicesClick: () -> Unit,
    onProfileClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onAddDeviceClick: () -> Unit,
    onDeviceSpecsClick: (Long) -> Unit,
    onDeviceSettingsClick: (Long) -> Unit,
    onReplayTripClick: (Long) -> Unit,
    onCommandsClick: (Long) -> Unit,
    onReportsClick: () -> Unit,
    onAlertsSettingsClick: () -> Unit,
    onAboutClick: () -> Unit,
    onContactSupportClick: () -> Unit,
    onGeofenceClick: (Long) -> Unit,
    onToggleMapLock: () -> Unit,
    onDebugLogsClick: (() -> Unit)? = null,
    onDismissServerDown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedDevice = remember(state.devices, state.selectedDeviceId) { 
        state.devices.firstOrNull { it.id == state.selectedDeviceId } 
    }
    
    androidx.compose.runtime.LaunchedEffect(Unit) {
        android.util.Log.d("LoginPerformance", "HomeMapScreen launched at ${System.currentTimeMillis()}")
    }
    
    val selectedPosition = state.latestPositions[state.selectedDeviceId]
    var menuOpen by remember { mutableStateOf(false) }

    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    var showLockWarning by remember { mutableStateOf(false) }
    LaunchedEffect(showLockWarning) {
        if (showLockWarning) {
            kotlinx.coroutines.delay(2000)
            showLockWarning = false
        }
    }

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
                val targetMapPadding = remember(state.deviceManagementOpen, state.deviceCardExpanded, selectedDevice) {
                    when {
                        state.deviceManagementOpen -> 446.dp
                        selectedDevice != null -> if (state.deviceCardExpanded) 240.dp else 163.dp
                        else -> 0.dp
                    }
                }
                val mapBottomPadding by androidx.compose.animation.core.animateDpAsState(
                    targetValue = targetMapPadding,
                    animationSpec = androidx.compose.animation.core.tween(durationMillis = 260),
                    label = "mapBottomPadding",
                )

                // Show snackbar when tile health monitor reports unreachable
                LaunchedEffect(state.tileHealth) {
                    if (state.tileHealth == TileHealthState.Unreachable) {
                        snackbarHostState.showSnackbar(
                            message = "دریافت نقشه با مشکل مواجه شد. لطفا اینترنت یا منبع نقشه را بررسی کنید.",
                            duration = androidx.compose.material3.SnackbarDuration.Long,
                            actionLabel = "باشه"
                        )
                        // بعد از نمایش پیام، باید وضعیت رو به حالت نرمال برگردونیم
                        // تا اگر کاربر اینترنت رو وصل کرد یا منبع نقشه درست شد، پیام دوباره تکرار نشه
                        onTileHealthErrorConsumed()
                    }
                }

                /*
                LaunchedEffect(state.infoMessage) {
                    state.infoMessage?.let { msg ->
                        snackbarHostState.showSnackbar(
                            message = msg,
                            duration = androidx.compose.material3.SnackbarDuration.Short
                        )
                        onClearInfoMessage()
                    }
                }
                */

                TrackingMap(
                    latestPositions = state.latestPositions,
                    selectedDeviceId = state.selectedDeviceId,
                    mapStyle = state.mapStyle,
                    activeTileSource = state.activeTileSource,
                    isMapLocked = state.isMapLocked,
                    onMapInteraction = {
                        if (state.deviceManagementOpen) {
                            onCloseDeviceManagement()
                        }
                        if (state.isMapLocked) {
                            showLockWarning = true
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                )
                MapTopControls(
                    devices = state.devices,
                    selectedDeviceId = state.selectedDeviceId,
                    latestEvent = state.latestEvent,
                    isMapLocked = state.isMapLocked,
                    showLockWarning = showLockWarning,
                    onDeviceSelectorClick = onToggleDevices,
                    onSettingsClick = onOpenMapSettings,
                    onEventsClick = onEventsClick,
                    onLockToggleClick = onToggleMapLock,
                    isSettingsEnabled = !state.isAlternativeMapForced,
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
                    MapSettingsDialog(
                        currentStyle = state.mapStyle,
                        onDismiss = onCloseMapSettings,
                        onSaveStyle = onMapStyleSelected,
                    )
                }
                if (menuOpen) {
                    AppMenuDialog(
                        onDismiss = { menuOpen = false },
                        onLogoutClick = onLogoutClick,
                        onAddDeviceClick = onAddDeviceClick,
                        onReportsClick = onReportsClick,
                        onAlertsSettingsClick = onAlertsSettingsClick,
                        onAboutClick = onAboutClick,
                        onContactSupportClick = onContactSupportClick,
                        onDebugLogsClick = onDebugLogsClick,
                    )
                }
                if (selectedDevice != null) {
                    BottomPanels(
                        deviceManagementOpen = state.deviceManagementOpen,
                        deviceCardExpanded = state.deviceCardExpanded,
                        todayDistanceText = state.todayDistanceText,
                        selectedDevice = selectedDevice,
                        selectedPosition = selectedPosition,
                        onDeviceSpecsClick = onDeviceSpecsClick,
                        onDeviceSettingsClick = onDeviceSettingsClick,
                        onReplayTripClick = onReplayTripClick,
                        onCommandsClick = onCommandsClick,
                        onReportsClick = onReportsClick,
                        onEventsClick = onEventsClick,
                        onDevicesClick = onDevicesClick,
                        onProfileClick = onProfileClick,
                        onAlertsSettingsClick = onAlertsSettingsClick,
                        onGeofenceClick = onGeofenceClick,
                        onCloseDeviceManagement = onCloseDeviceManagement,
                        onToggleDeviceCard = onToggleDeviceCard,
                        onManageDeviceClick = onManageDeviceClick,
                    )
                }

                androidx.compose.material3.SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = mapBottomPadding + 16.dp),
                    snackbar = { data ->
                        androidx.compose.material3.Snackbar(
                            modifier = Modifier.padding(16.dp),
                            containerColor = Color(0xFFF44336),
                            contentColor = Color.White,
                            actionColor = Color.White,
                            actionOnNewLine = false,
                            snackbarData = data
                        )
                    }
                )

                if (state.connectionError == ConnectionErrorType.SERVER_DOWN) {
                    ServerDownBottomSheet(
                        onDismiss = onDismissServerDown
                    )
                } else if (state.connectionError == ConnectionErrorType.NETWORK_UNREACHABLE) {
                    NetworkErrorBottomSheet(
                        onDismiss = onDismissServerDown
                    )
                }
            }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.BoxScope.BottomPanels(
    // به جای دریافت کل state، فقط ۳ فیلد مورد نیاز را می‌گیریم:
    deviceManagementOpen: Boolean,
    deviceCardExpanded: Boolean,
    todayDistanceText: String,
    selectedDevice: com.example.uzradyab.domain.model.Device,
    selectedPosition: com.example.uzradyab.domain.model.Position?,
    onDeviceSpecsClick: (Long) -> Unit,
    onDeviceSettingsClick: (Long) -> Unit,
    onReplayTripClick: (Long) -> Unit,
    onCommandsClick: (Long) -> Unit,
    onReportsClick: () -> Unit,
    onEventsClick: () -> Unit,
    onDevicesClick: () -> Unit,
    onProfileClick: () -> Unit,
    onAlertsSettingsClick: () -> Unit,
    onGeofenceClick: (Long) -> Unit,
    onCloseDeviceManagement: () -> Unit,
    onToggleDeviceCard: () -> Unit,
    onManageDeviceClick: () -> Unit,
) {
    AnimatedVisibility(
        visible = deviceManagementOpen,
        enter = slideInVertically(
            animationSpec = tween(durationMillis = 300),
            initialOffsetY = { it }
        ) + fadeIn(animationSpec = tween(300)),
        exit = slideOutVertically(
            animationSpec = tween(durationMillis = 300),
            targetOffsetY = { it }
        ) + fadeOut(animationSpec = tween(300)),
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
    ) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
            DeviceManagementPanel(
                device = selectedDevice,
                position = selectedPosition,
                todayDistanceText = todayDistanceText,
                onDeviceSpecsClick = { onDeviceSpecsClick(selectedDevice.id) },
                onDeviceSettingsClick = { onDeviceSettingsClick(selectedDevice.id) },
                onReplayTripClick = { onReplayTripClick(selectedDevice.id) },
                onCommandsClick = { onCommandsClick(selectedDevice.id) },
                onReportsClick = onReportsClick,
                onEventsClick = onEventsClick,
                onAlertsSettingsClick = onAlertsSettingsClick,
                onGeofenceClick = { onGeofenceClick(selectedDevice.id) },
                modifier = Modifier
                    .navigationBarsPadding()
            )
            
            AppBottomNavigation(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(bottom = 8.dp),
                onItemSelected = { item ->
                    when (item) {
                        BottomNavItem.ALARM -> onEventsClick()
                        BottomNavItem.DEVICES -> onDevicesClick()
                        BottomNavItem.ACCOUNT -> onProfileClick()
                        BottomNavItem.MAP -> {
                            if (deviceManagementOpen) {
                                onCloseDeviceManagement()
                            }
                        }
                    }
                }
            )
        }
    }

    AnimatedVisibility(
        visible = !deviceManagementOpen,
        enter = slideInVertically(
            animationSpec = tween(durationMillis = 300),
            initialOffsetY = { it }
        ) + fadeIn(animationSpec = tween(300)),
        exit = slideOutVertically(
            animationSpec = tween(durationMillis = 300),
            targetOffsetY = { it }
        ) + fadeOut(animationSpec = tween(300)),
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
    ) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
            SelectedDeviceStatusCard(
                device = selectedDevice,
                position = selectedPosition,
                todayDistanceText = todayDistanceText,
                expanded = deviceCardExpanded,
                onToggleExpanded = onToggleDeviceCard,
                onManageClick = onManageDeviceClick,
                onReplayClick = { onReplayTripClick(selectedDevice.id) },
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp),
            )
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ServerDownBottomSheet(
    onDismiss: () -> Unit,
) {
    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            androidx.compose.material3.Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Color(0xFFF44336),
                modifier = Modifier.size(64.dp)
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.str_b53c0371),
                style = MaterialTheme.typography.titleLarge
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.str_29612784),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(32.dp))
            
            androidx.compose.material3.Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.str_ed52d39d), fontSize = 16.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            }
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun NetworkErrorBottomSheet(
    onDismiss: () -> Unit
) {
    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp, top = 8.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Warning Icon in a circular badge
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(Color(0xFFFFF4E5), androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFE5B850),
                    modifier = Modifier.size(36.dp)
                )
            }
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(20.dp))
            
            // Centered Title
            Text(
                text = stringResource(R.string.str_c58b4a6c),
                fontSize = 20.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = com.example.uzradyab.ui.theme.AppTextPrimary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(12.dp))
            
            // Centered and slightly larger description
            Text(
                text = "ارتباط با سرور برقرار نشد.\nلطفاً اتصال اینترنت خود را بررسی کرده یا در صورت روشن بودن VPN، آن را خاموش کنید.",
                fontSize = 15.sp,
                color = com.example.uzradyab.ui.theme.AppTextPrimary.copy(alpha = 0.7f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 24.sp
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(32.dp))
            
            // Modern Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .background(com.example.uzradyab.ui.theme.AppBlue, androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                    .clickable { onDismiss() },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.str_ed52d39d),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                )
            }
        }
    }
}
