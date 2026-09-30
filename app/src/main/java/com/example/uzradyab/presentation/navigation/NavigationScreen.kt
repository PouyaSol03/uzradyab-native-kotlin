package com.example.uzradyab.presentation.navigation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.uzradyab.ui.theme.RoutingColors
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.style.layers.Property
import kotlin.math.roundToInt

private val LOCATION_PERMISSIONS = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION,
)

private fun Context.hasLocationPermission(): Boolean = LOCATION_PERMISSIONS.any {
    ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
}

/** Everything the screen can ask the ViewModel to do. */
data class NavigationActions(
    val onRouteSelected: (Int) -> Unit,
    val onOpenSteps: () -> Unit,
    val onCloseSteps: () -> Unit,
    val onStepFocused: (Int) -> Unit,
    val onStart: () -> Unit,
    val onEnd: () -> Unit,
    val onShowOverview: () -> Unit,
    val onReturnToRoute: () -> Unit,
    val onMapGesture: () -> Unit,
    val onSheetOpen: (Boolean) -> Unit,
    val onStepsListOpen: (Boolean) -> Unit,
    val onToggleVoice: () -> Unit,
    val onRetry: () -> Unit,
)

@Composable
fun NavigationRoute(
    onNavigateUp: () -> Unit,
    onManageDevice: (Long) -> Unit,
    viewModel: NavigationViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        viewModel.onLocationPermissionResult(result.values.any { it })
    }

    LaunchedEffect(Unit) {
        if (context.hasLocationPermission()) {
            viewModel.onLocationPermissionResult(true)
        } else {
            permissionLauncher.launch(LOCATION_PERMISSIONS)
        }
    }

    // Coming back from the system settings with the permission now granted.
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentState by rememberUpdatedState(state)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME &&
                currentState.permission == LocationPermission.DENIED &&
                context.hasLocationPermission()
            ) {
                viewModel.onLocationPermissionResult(true)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    NavigationScreen(
        state = state,
        actions = NavigationActions(
            onRouteSelected = viewModel::onRouteSelected,
            onOpenSteps = viewModel::openSteps,
            onCloseSteps = viewModel::closeSteps,
            onStepFocused = viewModel::onStepFocused,
            onStart = viewModel::startNavigation,
            onEnd = viewModel::endNavigation,
            onShowOverview = viewModel::showOverview,
            onReturnToRoute = viewModel::returnToRoute,
            onMapGesture = viewModel::onMapGesture,
            onSheetOpen = viewModel::setSheetOpen,
            onStepsListOpen = viewModel::setStepsListOpen,
            onToggleVoice = viewModel::toggleVoice,
            onRetry = viewModel::retry,
        ),
        onNavigateUp = onNavigateUp,
        onManageDevice = onManageDevice,
        onRequestPermission = {
            val activity = context as? android.app.Activity
            val canAsk = activity != null && LOCATION_PERMISSIONS.any { activity.shouldShowRequestPermissionRationale(it) }
            if (canAsk) {
                permissionLauncher.launch(LOCATION_PERMISSIONS)
            } else {
                // Denied for good: the system dialog will not show again, so open the app settings.
                context.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null)),
                )
            }
        },
    )
}

@Composable
fun NavigationScreen(
    state: NavigationUiState,
    actions: NavigationActions,
    onNavigateUp: () -> Unit,
    onManageDevice: (Long) -> Unit,
    onRequestPermission: () -> Unit,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val phase = state.phase

    // Map overlay insets, measured from the overlays of the current phase.
    var rootHeight by remember { mutableIntStateOf(0) }
    var topInset by remember { mutableIntStateOf(0) }
    var bottomInset by remember { mutableIntStateOf(0) }

    var fitToken by remember { mutableIntStateOf(0) }
    var northToken by remember { mutableIntStateOf(0) }
    var previewNorthUp by remember { mutableStateOf(false) }
    var drivingNorthUp by remember { mutableStateOf(false) }
    var stepTapped by remember(phase) { mutableStateOf(false) }

    val driving = phase is NavigationPhase.Navigating || phase == NavigationPhase.Rerouting
    val view = LocalView.current
    DisposableEffect(driving) {
        view.keepScreenOn = driving
        onDispose { view.keepScreenOn = false }
    }

    BackHandler(enabled = phase !is NavigationPhase.Preview && phase !is NavigationPhase.Loading && phase !is NavigationPhase.Failed) {
        when (phase) {
            is NavigationPhase.Steps -> actions.onCloseSteps()
            is NavigationPhase.Navigating -> when {
                phase.stepsOpen -> actions.onStepsListOpen(false)
                phase.sheetOpen -> actions.onSheetOpen(false)
                phase.camera != NavCamera.FOLLOW -> actions.onReturnToRoute()
                else -> actions.onEnd()
            }
            NavigationPhase.Rerouting -> actions.onEnd()
            NavigationPhase.Arrived -> onNavigateUp()
            else -> onNavigateUp()
        }
    }

    val scene = remember(state, phase) { buildScene(state) }
    val camera = buildCamera(state, fitToken, northToken, previewNorthUp, drivingNorthUp, stepTapped)
    val gap = with(density) { 16.dp.roundToPx() }
    val insets = MapInsets(
        top = topInset + gap,
        bottom = bottomInset + gap,
        side = with(density) { 32.dp.roundToPx() },
    )

    fun Modifier.measureTop() = onGloballyPositioned { topInset = it.boundsInRoot().bottom.roundToInt() }
    fun Modifier.measureBottom() = onGloballyPositioned {
        bottomInset = (rootHeight - it.boundsInRoot().top).roundToInt().coerceAtLeast(0)
    }

    val nowMillis = System.currentTimeMillis()
    val statusText = state.vehicle?.let { if (it.isStopped) "متوقف" else "در حرکت" }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(RoutingColors.mapBackground)
                .onSizeChanged { rootHeight = it.height },
        ) {
            NavigationMap(
                scene = scene,
                camera = camera,
                insets = insets,
                onUserGesture = actions.onMapGesture,
                modifier = Modifier.fillMaxSize(),
            )

            when {
                state.permission == LocationPermission.DENIED -> PermissionState(onRequestPermission, onNavigateUp)
                phase is NavigationPhase.Failed -> CenteredCard {
                    MessageCard(
                        title = phase.message,
                        message = null,
                        primaryAction = "تلاش دوباره" to actions.onRetry,
                        secondaryAction = "بازگشت" to onNavigateUp,
                    )
                }
                phase == NavigationPhase.Loading -> {
                    PreviewTopCard(
                        destinationName = state.destinationLabel,
                        statusText = statusText,
                        onBack = onNavigateUp,
                        modifier = Modifier.statusBarsPadding().padding(16.dp).measureTop(),
                    )
                    CenteredCard {
                        MessageCard(
                            title = if (state.userLocation == null) "در حال یافتن موقعیت شما…" else "در حال یافتن بهترین مسیر…",
                            message = null,
                            loading = true,
                        )
                    }
                }
                phase == NavigationPhase.Preview -> PreviewLayout(
                    state = state,
                    statusText = statusText,
                    nowMillis = nowMillis,
                    actions = actions,
                    onNavigateUp = onNavigateUp,
                    onFit = {
                        previewNorthUp = false
                        fitToken++
                    },
                    onNorth = {
                        previewNorthUp = true
                        northToken++
                    },
                    onShare = { shareRoute(context, state) },
                    topModifier = Modifier.measureTop(),
                    bottomModifier = Modifier.measureBottom(),
                )
                phase is NavigationPhase.Steps -> StepsLayout(
                    state = state,
                    fromNavigation = phase.fromNavigation,
                    nowMillis = nowMillis,
                    actions = actions,
                    onStepClick = { index ->
                        stepTapped = true
                        actions.onStepFocused(index)
                    },
                    onShare = { shareRoute(context, state) },
                    topModifier = Modifier.measureTop(),
                    bottomModifier = Modifier.measureBottom(),
                )
                phase is NavigationPhase.Navigating || phase == NavigationPhase.Rerouting -> DrivingLayout(
                    state = state,
                    phase = phase,
                    nowMillis = nowMillis,
                    northUp = drivingNorthUp,
                    stepsTop = with(density) { topInset.toDp() } + 12.dp,
                    actions = actions,
                    onToggleNorthUp = { drivingNorthUp = !drivingNorthUp },
                    onShareEta = { shareEta(context, state, nowMillis) },
                    topModifier = Modifier.measureTop(),
                    bottomModifier = Modifier.measureBottom(),
                )
                phase == NavigationPhase.Arrived -> ArrivedLayout(
                    state = state,
                    statusText = statusText,
                    nowMillis = nowMillis,
                    onFinish = onNavigateUp,
                    onManageDevice = state.deviceId?.let { id -> { onManageDevice(id) } },
                    topModifier = Modifier.measureTop(),
                    bottomModifier = Modifier.measureBottom(),
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Phase layouts
// ---------------------------------------------------------------------------

@Composable
private fun CenteredCard(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().navigationBarsPadding().padding(16.dp),
        contentAlignment = Alignment.BottomCenter,
    ) { content() }
}

@Composable
private fun PermissionState(onRequestPermission: () -> Unit, onNavigateUp: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(RoutingColors.scrim.copy(alpha = 0.32f)).padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        MessageCard(
            title = "دسترسی به موقعیت مکانی لازم است",
            message = "برای مسیریابی تا خودرو، اجازه‌ی دسترسی به موقعیت مکانی را بدهید.",
            primaryAction = "اجازه دسترسی" to onRequestPermission,
            secondaryAction = "بازگشت" to onNavigateUp,
        )
    }
}

@Composable
private fun PreviewLayout(
    state: NavigationUiState,
    statusText: String?,
    nowMillis: Long,
    actions: NavigationActions,
    onNavigateUp: () -> Unit,
    onFit: () -> Unit,
    onNorth: () -> Unit,
    onShare: () -> Unit,
    topModifier: Modifier,
    bottomModifier: Modifier,
) {
    val route = state.selectedRoute ?: return
    val guidance = state.guidance
    val fastest = state.fastestRouteIndex
    Column(Modifier.fillMaxSize()) {
        Box(Modifier.weight(1f).fillMaxWidth()) {
            PreviewTopCard(
                destinationName = state.destinationLabel,
                statusText = statusText,
                onBack = onNavigateUp,
                modifier = Modifier.statusBarsPadding().padding(16.dp).then(topModifier),
            )
            Column(
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                IconFab(NavigationIcons.FitRoute, "نمایش کل مسیر", onFit)
                CompassFab(rotation = 0f, onClick = onNorth)
            }
            MapCredits(Modifier.align(Alignment.BottomStart).padding(start = 16.dp, bottom = 8.dp))
        }
        PreviewSheet(
            cards = state.routes.mapIndexed { index, r ->
                RouteCardData(
                    name = "مسیر ${NavigationFormat.number(index + 1)}",
                    time = r.durationText.ifBlank { NavigationFormat.duration(r.durationSeconds.toDouble()) },
                    distance = r.distanceText.ifBlank { NavigationFormat.distance(r.distanceMeters.toDouble()) },
                    summary = r.summaryText,
                    fastest = state.routes.size > 1 && index == fastest,
                )
            },
            selectedIndex = state.selectedRouteIndex,
            etaText = NavigationFormat.eta(nowMillis, route.durationSeconds.toDouble()),
            routeInfoText = routeInfoText(guidance),
            onSelect = actions.onRouteSelected,
            onDetails = actions.onOpenSteps,
            onStart = actions.onStart,
            onShare = onShare,
            modifier = bottomModifier,
        )
    }
}

private fun routeInfoText(guidance: List<GuidanceStep>): String {
    val rotaries = guidance.rotaryCount()
    val base = if (rotaries == 0) "بدون میدان" else "${NavigationFormat.number(rotaries)} میدان در مسیر"
    val highway = guidance.any { it.roadName.startsWith("بزرگراه") }
    return if (highway) "$base · بزرگراه" else base
}

@Composable
private fun StepsLayout(
    state: NavigationUiState,
    fromNavigation: Boolean,
    nowMillis: Long,
    actions: NavigationActions,
    onStepClick: (Int) -> Unit,
    onShare: () -> Unit,
    topModifier: Modifier,
    bottomModifier: Modifier,
) {
    val route = state.selectedRoute ?: return
    val remainingSeconds = if (fromNavigation) state.progress?.remainingSeconds ?: route.durationSeconds.toDouble() else route.durationSeconds.toDouble()
    Column(Modifier.fillMaxSize()) {
        StepsTopBar(
            title = "جزئیات مسیر",
            subtitle = "مسیر ${NavigationFormat.number(state.selectedRouteIndex + 1)} · ${route.summaryText}",
            onBack = actions.onCloseSteps,
            onShare = onShare,
            modifier = Modifier.background(RoutingColors.surface).statusBarsPadding().then(topModifier),
        )
        Spacer(Modifier.weight(0.34f))
        StepsSheet(
            steps = state.guidance,
            focusedIndex = state.focusedStepIndex,
            distanceChip = route.distanceText.ifBlank { NavigationFormat.distance(route.distanceMeters.toDouble()) },
            durationChip = route.durationText.ifBlank { NavigationFormat.duration(route.durationSeconds.toDouble()) },
            rotaryChip = "${NavigationFormat.number(state.guidance.rotaryCount())} میدان",
            etaText = NavigationFormat.eta(nowMillis, remainingSeconds),
            actionText = if (fromNavigation) "ادامه مسیریابی" else "شروع مسیریابی",
            onStepClick = onStepClick,
            onAction = if (fromNavigation) actions.onCloseSteps else actions.onStart,
            modifier = Modifier.weight(0.66f).then(bottomModifier),
        )
    }
}

@Composable
private fun DrivingLayout(
    state: NavigationUiState,
    phase: NavigationPhase,
    nowMillis: Long,
    northUp: Boolean,
    /** Where the steps panel starts: just under the banner card. */
    stepsTop: Dp,
    actions: NavigationActions,
    onToggleNorthUp: () -> Unit,
    onShareEta: () -> Unit,
    topModifier: Modifier,
    bottomModifier: Modifier,
) {
    val route = state.selectedRoute ?: return
    val navigating = phase as? NavigationPhase.Navigating
    val rerouting = phase == NavigationPhase.Rerouting
    val camera = navigating?.camera ?: NavCamera.FOLLOW
    val progress = state.progress
    val remainingSeconds = progress?.remainingSeconds ?: route.durationSeconds.toDouble()
    val remainingMeters = progress?.remainingMeters ?: route.distanceMeters.toDouble()
    val remainingTime = NavigationFormat.duration(remainingSeconds)
    val remainingDetail = "${NavigationFormat.distance(remainingMeters)} · رسیدن ${NavigationFormat.eta(nowMillis, remainingSeconds)}"
    val bannerModifier = Modifier.statusBarsPadding().padding(start = 12.dp, end = 12.dp, top = 16.dp)
    val bannerStep = state.nextStep
    val bannerDistance = NavigationFormat.distance(progress?.distanceToNext ?: 0.0)
    val nextIndex = progress?.nextStepIndex
    val stepsOpen = navigating?.stepsOpen == true && bannerStep != null && nextIndex != null

    Column(Modifier.fillMaxSize()) {
        Box(Modifier.weight(1f).fillMaxWidth()) {
            if (camera == NavCamera.FOLLOW || rerouting) {
                Column(
                    modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    val rotation = if (northUp) 0f else -(state.userBearing ?: 0.0).toFloat()
                    CompassFab(rotation = rotation, onClick = onToggleNorthUp)
                    IconFab(
                        icon = if (state.voiceEnabled) NavigationIcons.VolumeOn else NavigationIcons.VolumeOff,
                        contentDescription = "راهنمای صوتی",
                        onClick = actions.onToggleVoice,
                    )
                }
                SpeedBadge(state.speedKmh, Modifier.align(Alignment.BottomStart).padding(start = 16.dp, bottom = 16.dp))
                MapCredits(Modifier.align(Alignment.BottomStart).padding(start = 88.dp, bottom = 20.dp))
            } else {
                ReturnToRouteButton(
                    onClick = actions.onReturnToRoute,
                    modifier = Modifier.align(Alignment.BottomStart).padding(start = 16.dp, bottom = 16.dp),
                )
            }
        }
        if (rerouting) {
            NavBottomBar(
                primaryText = "در حال محاسبه…",
                primaryColor = RoutingColors.warnMuted,
                secondaryText = "مسیر قبلی · رسیدن —",
                secondaryAlpha = 0.6f,
                onEnd = actions.onEnd,
                trailingIcon = null,
                trailingDescription = "",
                onTrailing = {},
                onOpenSheet = null,
                modifier = bottomModifier,
            )
        } else {
            val inFollow = camera == NavCamera.FOLLOW
            NavBottomBar(
                primaryText = remainingTime,
                primaryColor = RoutingColors.success,
                secondaryText = remainingDetail,
                onEnd = actions.onEnd,
                trailingIcon = if (inFollow) NavigationIcons.Route else NavigationIcons.ArrowRight,
                trailingDescription = if (inFollow) "نمای کلی مسیر" else "بازگشت به مسیر",
                onTrailing = if (inFollow) actions.onShowOverview else actions.onReturnToRoute,
                onOpenSheet = { actions.onSheetOpen(true) },
                modifier = bottomModifier,
            )
        }
    }

    if (navigating?.sheetOpen == true) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(RoutingColors.scrim.copy(alpha = 0.32f))
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                    actions.onSheetOpen(false)
                },
        )
        val next = progress?.nextStepIndex
        val upcoming = if (progress == null || next == null) {
            emptyList()
        } else {
            (next until minOf(next + 3, state.guidance.size)).map { index ->
                val step = state.guidance[index]
                val distance = if (index == next) progress.distanceToNext else state.guidance[index - 1].distanceMeters
                step to NavigationFormat.distance(distance)
            }
        }
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            NavOptionsSheet(
                remainingTime = remainingTime,
                remainingDetail = remainingDetail,
                destinationName = state.destinationLabel,
                upcoming = upcoming,
                totalSteps = state.guidance.size,
                voiceEnabled = state.voiceEnabled,
                onDetails = actions.onOpenSteps,
                onShareEta = onShareEta,
                onToggleVoice = actions.onToggleVoice,
                onEnd = actions.onEnd,
                onDismiss = { actions.onSheetOpen(false) },
            )
        }
    }

    // Remaining steps, opened by tapping the banner: the map fades to white and the list
    // unrolls downward from the banner's bottom edge.
    AnimatedVisibility(
        visible = stepsOpen,
        enter = fadeIn(tween(220)),
        exit = fadeOut(tween(220, delayMillis = 60)),
    ) {
        Box(Modifier.fillMaxSize().background(RoutingColors.surface))
    }
    AnimatedVisibility(
        visible = stepsOpen,
        modifier = Modifier.padding(top = stepsTop),
        enter = expandVertically(tween(340, easing = FastOutSlowInEasing), expandFrom = Alignment.Top) + fadeIn(tween(200)),
        exit = shrinkVertically(tween(260, easing = FastOutSlowInEasing), shrinkTowards = Alignment.Top) + fadeOut(tween(180)),
    ) {
        // Rows are built from the state at open time and kept while the panel animates away.
        val rows = remember(state.guidance, nextIndex) {
            val first = (nextIndex ?: return@remember emptyList<Pair<GuidanceStep, String>>()) + 1
            (first until state.guidance.size).map { index ->
                // Distance driven after the previous maneuver until this one
                state.guidance[index] to NavigationFormat.distance(state.guidance[index - 1].distanceMeters)
            }
        }
        UpcomingStepsPanel(
            rows = rows,
            remainingTime = remainingTime,
            remainingDetail = remainingDetail,
            onBackToMap = { actions.onStepsListOpen(false) },
            onEnd = actions.onEnd,
        )
    }

    // Banner last, so it stays above the options sheet's scrim and the steps panel.
    when {
        rerouting -> StatusBanner(
            color = RoutingColors.warnBanner,
            icon = NavigationIcons.Reroute,
            overline = "از مسیر خارج شدید",
            title = "در حال یافتن مسیر جدید…",
            subtitle = "لطفاً با احتیاط ادامه دهید",
            modifier = bannerModifier.then(topModifier),
        )
        bannerStep != null -> InstructionBanner(
            step = bannerStep,
            distanceText = bannerDistance,
            then = state.followingStep,
            onClick = if (navigating != null) ({ actions.onStepsListOpen(!stepsOpen) }) else null,
            showHandle = stepsOpen,
            showThen = !stepsOpen,
            modifier = bannerModifier,
            // Measure the card only, so the chip folding away does not move the panel.
            cardModifier = topModifier,
        )
        else -> StatusBanner(
            color = RoutingColors.banner,
            icon = NavigationIcons.NavigationArrow,
            overline = "در مسیر",
            title = state.destinationLabel,
            subtitle = "مسیر مشخص‌شده را دنبال کنید",
            modifier = bannerModifier.then(topModifier),
        )
    }
}

@Composable
private fun ArrivedLayout(
    state: NavigationUiState,
    statusText: String?,
    nowMillis: Long,
    onFinish: () -> Unit,
    onManageDevice: (() -> Unit)?,
    topModifier: Modifier,
    bottomModifier: Modifier,
) {
    val arrival = state.arrival
    val parked = state.vehicle?.isStopped != false
    Column(Modifier.fillMaxSize()) {
        StatusBanner(
            color = RoutingColors.success,
            icon = NavigationIcons.Check,
            overline = "به مقصد رسیدید",
            title = "${state.destinationLabel} اینجاست",
            subtitle = "خودرو ${arrival?.sideText ?: "در نزدیکی شما"} ${if (parked) "پارک است" else "است"}",
            modifier = Modifier.statusBarsPadding().padding(start = 12.dp, end = 12.dp, top = 16.dp).then(topModifier),
        )
        Spacer(Modifier.weight(1f))
        ArrivedSheet(
            vehicleName = state.vehicleName,
            lastUpdateText = NavigationFormat.relativeTime(state.vehicle?.lastUpdateMillis, nowMillis),
            statusText = statusText,
            traveled = NavigationFormat.distance(arrival?.traveledMeters ?: 0.0),
            tripDuration = NavigationFormat.duration(arrival?.tripSeconds ?: 0.0),
            distanceToVehicle = NavigationFormat.distance(arrival?.distanceToVehicle ?: 0.0),
            address = state.vehicle?.address ?: state.guidance.lastOrNull()?.roadName,
            onFinish = onFinish,
            onManageDevice = onManageDevice,
            modifier = bottomModifier,
        )
    }
}

// ---------------------------------------------------------------------------
// Map scene and camera per phase
// ---------------------------------------------------------------------------

private fun userDot(location: LatLng) = listOf(
    MapDot(location, radius = 20f, color = RoutingColors.primary, opacity = 0.16f),
    MapDot(location, radius = 6f, color = RoutingColors.primary, strokeColor = androidx.compose.ui.graphics.Color.White, strokeWidth = 3f),
)

private fun stepDot(location: LatLng) =
    MapDot(location, radius = 4f, color = androidx.compose.ui.graphics.Color.White, strokeColor = RoutingColors.primary, strokeWidth = 2.5f)

private fun vehiclePill(state: NavigationUiState, label: String) = MapMarker(
    icon = MarkerIcon.Pill(listOf(state.vehicleName, "·", label), PillStyle.VEHICLE),
    position = state.destination!!,
    anchor = Property.ICON_ANCHOR_BOTTOM,
    offset = 0f to -24f,
)

private fun buildScene(state: NavigationUiState): MapScene {
    val route = state.selectedRoute
    val user = state.userLocation
    val destination = state.destination
    val lines = mutableListOf<MapLine>()
    val dots = mutableListOf<MapDot>()
    val markers = mutableListOf<MapMarker>()

    fun vehicle(haloColor: androidx.compose.ui.graphics.Color = RoutingColors.vehicle, haloRadius: Float = 26f) {
        destination ?: return
        dots += MapDot(destination, radius = haloRadius, color = haloColor, opacity = 0.18f)
        markers += MapMarker(MarkerIcon.Vehicle, destination)
    }

    fun puck() {
        user ?: return
        dots += MapDot(user, radius = 30f, color = RoutingColors.primary, opacity = 0.14f)
        markers += MapMarker(MarkerIcon.Puck, user, rotation = (state.userBearing ?: 0.0).toFloat(), rotatesWithMap = true)
    }

    fun drivenLines(remainingDotted: Boolean = false) {
        val remaining = state.remainingPoints.ifEmpty { route?.points.orEmpty() }
        if (state.traveledPoints.size >= 2) {
            lines += MapLine(state.traveledPoints, RoutingColors.routeTraveled, width = 6f, casingWidth = 10f)
        }
        lines += if (remainingDotted) {
            MapLine(remaining, RoutingColors.routeTraveled, width = 6f, dotted = true)
        } else {
            MapLine(remaining, RoutingColors.primary, width = 7f, casingWidth = 11f)
        }
    }

    when (val phase = state.phase) {
        NavigationPhase.Loading, is NavigationPhase.Failed -> {
            user?.let { dots += userDot(it) }
            vehicle()
        }
        NavigationPhase.Preview -> {
            state.routes.forEachIndexed { index, r ->
                if (index != state.selectedRouteIndex) lines += MapLine(r.points, RoutingColors.routeAlternative, width = 4f)
            }
            route?.let { lines += MapLine(it.points, RoutingColors.primary, width = 6f, casingWidth = 10f) }
            user?.let { dots += userDot(it) }
            vehicle()
            if (destination != null) markers += vehiclePill(state, if (state.vehicle?.isStopped == false) "در حرکت" else "متوقف")
            if (state.routes.size > 1) {
                state.routes.forEachIndexed { index, r ->
                    if (r.points.size < 2) return@forEachIndexed
                    val selected = index == state.selectedRouteIndex
                    val at = r.points[((r.points.size - 1) * if (selected) 0.45 else 0.6).toInt()]
                    markers += MapMarker(
                        MarkerIcon.Pill(listOf(r.durationText), if (selected) PillStyle.DURATION_SELECTED else PillStyle.DURATION),
                        at,
                    )
                }
            }
        }
        is NavigationPhase.Steps -> {
            route?.let { lines += MapLine(it.points, RoutingColors.primary, width = 6f, casingWidth = 10f) }
            state.guidance.forEachIndexed { index, step ->
                if (index != state.focusedStepIndex && step.kind != ManeuverKind.ARRIVE) dots += stepDot(step.location)
            }
            vehicle()
            state.guidance.getOrNull(state.focusedStepIndex)?.let { dots += userDot(it.location) }
        }
        is NavigationPhase.Navigating -> {
            drivenLines()
            val overview = phase.camera != NavCamera.FOLLOW
            val next = state.progress?.nextStepIndex
            if (overview && next != null) {
                state.guidance.drop(next).forEach { if (it.kind != ManeuverKind.ARRIVE) dots += stepDot(it.location) }
                val remaining = state.guidance.drop(next)
                remaining.overviewLabels().forEach { (name, point) ->
                    markers += MapMarker(MarkerIcon.Pill(listOf(name), PillStyle.ROAD), point)
                }
            }
            vehicle()
            if (overview && destination != null) markers += vehiclePill(state, "مقصد")
            val step = state.nextStep
            if (!overview && step != null && step.kind != ManeuverKind.ARRIVE) {
                markers += if (step.kind == ManeuverKind.ROTARY && step.rotaryExit != null) {
                    MapMarker(MarkerIcon.Exit(step.rotaryExit), step.exitLocation ?: step.location)
                } else {
                    MapMarker(MarkerIcon.Maneuver(step.kind), step.location)
                }
            }
            puck()
        }
        NavigationPhase.Rerouting -> {
            drivenLines(remainingDotted = true)
            vehicle()
            puck()
        }
        NavigationPhase.Arrived -> {
            val driven = state.traveledPoints.ifEmpty { route?.points.orEmpty() }
            if (driven.size >= 2) lines += MapLine(driven, RoutingColors.routeTraveled, width = 6f, casingWidth = 10f)
            vehicle(haloColor = RoutingColors.success, haloRadius = 46f)
            destination?.let { dots += MapDot(it, radius = 26f, color = RoutingColors.vehicle, opacity = 0.18f) }
            puck()
        }
    }
    return MapScene(lines = lines, dots = dots, markers = markers)
}

private fun buildCamera(
    state: NavigationUiState,
    fitToken: Int,
    northToken: Int,
    previewNorthUp: Boolean,
    drivingNorthUp: Boolean,
    stepTapped: Boolean,
): MapCamera? {
    val user = state.userLocation
    val destination = state.destination
    val routesKey = System.identityHashCode(state.routes)
    return when (val phase = state.phase) {
        NavigationPhase.Loading, is NavigationPhase.Failed ->
            MapCamera.Fit(listOfNotNull(user, destination), key = "loading-${user != null}")
        NavigationPhase.Preview -> if (previewNorthUp) {
            MapCamera.NorthUp(key = northToken)
        } else {
            val points = state.routes.flatMap { it.points } + listOfNotNull(user, destination)
            MapCamera.Fit(points, key = "preview-$routesKey-$fitToken")
        }
        is NavigationPhase.Steps -> {
            val focused = state.guidance.getOrNull(state.focusedStepIndex)
            if (stepTapped && focused != null) {
                MapCamera.Center(focused.location, zoom = 16.5, key = "step-${state.focusedStepIndex}")
            } else {
                MapCamera.Fit(state.selectedRoute?.points.orEmpty(), key = "steps-$routesKey-${state.selectedRouteIndex}")
            }
        }
        is NavigationPhase.Navigating -> when (phase.camera) {
            NavCamera.FOLLOW -> user?.let {
                MapCamera.Follow(it, bearing = if (drivingNorthUp) 0.0 else state.userBearing ?: 0.0)
            }
            NavCamera.OVERVIEW -> MapCamera.Fit(
                state.remainingPoints.ifEmpty { state.selectedRoute?.points.orEmpty() } + listOfNotNull(user, destination),
                key = "overview-$routesKey",
            )
            NavCamera.FREE -> null
        }
        NavigationPhase.Rerouting -> user?.let {
            MapCamera.Follow(it, bearing = if (drivingNorthUp) 0.0 else state.userBearing ?: 0.0)
        }
        NavigationPhase.Arrived -> MapCamera.Fit(listOfNotNull(user, destination), key = "arrived", maxZoom = 17.5)
    }
}

// ---------------------------------------------------------------------------
// Sharing
// ---------------------------------------------------------------------------

private fun mapLink(point: LatLng) =
    "https://www.google.com/maps/search/?api=1&query=${point.latitude},${point.longitude}"

private fun shareText(context: Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    runCatching { context.startActivity(Intent.createChooser(intent, null)) }
}

private fun shareRoute(context: Context, state: NavigationUiState) {
    val route = state.selectedRoute ?: return
    val text = buildString {
        append("مسیر تا ${state.destinationLabel}: ${route.durationText}، ${route.distanceText}")
        if (route.summaryText.isNotBlank()) append(" از ${route.summaryText}")
        state.destination?.let { append("\n").append(mapLink(it)) }
    }
    shareText(context, text)
}

private fun shareEta(context: Context, state: NavigationUiState, nowMillis: Long) {
    val seconds = state.progress?.remainingSeconds ?: state.selectedRoute?.durationSeconds?.toDouble() ?: return
    val text = buildString {
        append("در راه ${state.destinationLabel} هستم. زمان تقریبی رسیدن: ${NavigationFormat.eta(nowMillis, seconds)}")
        state.destination?.let { append("\n").append(mapLink(it)) }
    }
    shareText(context, text)
}
