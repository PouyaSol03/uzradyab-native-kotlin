package com.example.uzradyab.presentation.geofence

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.uzradyab.domain.model.Device
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.uzradyab.R
import com.example.uzradyab.domain.model.Geofence
import com.example.uzradyab.presentation.map.AppTopToolbar
import com.example.uzradyab.presentation.map.BackButton
import com.example.uzradyab.ui.theme.UzradyabTheme
import com.example.uzradyab.ui.theme.themedColor
import kotlinx.coroutines.launch

@Composable
fun GeofenceRoute(
    onBackClick: () -> Unit,
    viewModel: GeofenceViewModel = hiltViewModel()
) {
    GeofenceScreen(onBackClick = onBackClick, viewModel = viewModel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeofenceScreen(
    onBackClick: () -> Unit,
    viewModel: GeofenceViewModel
) {
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val rootView = LocalView.current

    // Bottom edge of the map container, in root coordinates.
    var mapBottomPx by remember { mutableStateOf(0f) }

    // Top edge of the keyboard, in the same coordinate space. If the window is
    // already resized for the IME, these two are equal and the overlap is zero,
    // so we never pad twice — that double padding is what left the empty strip.
    val keyboardTopPx = rootView.height - WindowInsets.ime.getBottom(density)
    val keyboardOverlap = with(density) {
        (mapBottomPx - keyboardTopPx).coerceAtLeast(0f).toDp()
    }

    var showListSheet by remember { mutableStateOf(false) }
    var fabsExpanded by remember { mutableStateOf(true) }
    var addSheetExpanded by remember { mutableStateOf(false) }

    val listSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(3000)
        fabsExpanded = false
    }

    LaunchedEffect(state.addingMode) {
        if (!state.addingMode) addSheetExpanded = false
    }

    // Animate the sheet out first, then run the next action, so nothing "pops".
    fun closeListSheet(after: () -> Unit = {}) {
        scope.launch { listSheetState.hide() }.invokeOnCompletion {
            showListSheet = false
            viewModel.closeConnections()
            after()
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(themedColor(light = Color.White, dark = Color(0xFF27343F)))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                AppTopToolbar(
                    modifier = Modifier.height(64.dp),
                    startContent = {
                        BackButton(onClick = onBackClick)
                    },
                    centerContent = {
                        Text(
                            text = stringResource(R.string.str_9704c0ca),
                            color = themedColor(light = Color(0xFF676C70), dark = Color(0xFF929292)),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .onGloballyPositioned { mapBottomPx = it.boundsInRoot().bottom }
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(MaterialTheme.colorScheme.background)
            ) {
                GeofenceMap(
                    state = state,
                    onMapClick = { lat, lon ->
                        if (state.addingMode) {
                            viewModel.addDrawingPoint(lat, lon)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // FABs stay in the tree at all times. They are never removed on click,
                // the sheet simply draws on top of them.
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .navigationBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    if (state.geofences.isNotEmpty()) {
                        FloatingActionButton(
                            onClick = { if (!state.addingMode) showListSheet = true },
                            containerColor = themedColor(light = Color.White, dark = Color(0xFF27343F)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                androidx.compose.animation.AnimatedVisibility(
                                    visible = fabsExpanded,
                                    enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandHorizontally(),
                                    exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkHorizontally()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "نمایش لیست محدوده ها",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = themedColor(light = Color(0xFF676C70), dark = Color(0xFFE0E0E0))
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                }
                                Icon(
                                    Icons.Default.Layers,
                                    contentDescription = "List",
                                    tint = UzradyabTheme.colors.primary
                                )
                            }
                        }
                    }

                    FloatingActionButton(
                        onClick = {
                            if (state.addingMode) return@FloatingActionButton
                            if (showListSheet) {
                                closeListSheet { viewModel.toggleAddingMode() }
                            } else {
                                viewModel.toggleAddingMode()
                            }
                        },
                        containerColor = UzradyabTheme.colors.primary,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            androidx.compose.animation.AnimatedVisibility(
                                visible = fabsExpanded,
                                enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandHorizontally(),
                                exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkHorizontally()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "ساخت محدوده جدید",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                            }
                            Icon(
                                Icons.Rounded.Add,
                                contentDescription = "Add Geofence",
                                tint = Color.White
                            )
                        }
                    }
                }

                // Add mode: a real bottom sheet look, but NOT a ModalBottomSheet,
                // because the map underneath has to stay tappable for drawing points.
                androidx.compose.animation.AnimatedVisibility(
                    visible = state.addingMode,
                    modifier = Modifier.align(Alignment.BottomCenter),
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    BottomSheetSurface(
                        expanded = addSheetExpanded,
                        onExpandedChange = { addSheetExpanded = it },
                        keyboardOverlap = keyboardOverlap
                    ) {
                        AddGeofencePanel(
                            name = state.newGeofenceName,
                            radius = state.newGeofenceRadius,
                            drawMode = state.drawMode,
                            pointsCount = state.activeDrawingPoints.items.size,
                            onNameChange = { viewModel.updateNewGeofenceName(it) },
                            onRadiusChange = { viewModel.updateNewGeofenceRadius(it) },
                            onCancel = { viewModel.toggleAddingMode() },
                            onSave = { viewModel.saveNewGeofence() },
                            onModeSelect = { viewModel.setDrawMode(it) },
                            onUndo = { viewModel.undoLastDrawingPoint() },
                            onClear = { viewModel.clearDrawingPoints() }
                        )
                    }
                }
            }
        }

        // List sheet. Height follows the content and grows up to 92% of the screen,
        // then the list itself scrolls inside.
        if (showListSheet && !state.addingMode && state.geofences.isNotEmpty()) {
            val maxSheetHeight = (LocalConfiguration.current.screenHeightDp * 0.92f).dp

            ModalBottomSheet(
                onDismissRequest = {
                    showListSheet = false
                    viewModel.closeConnections()
                },
                sheetState = listSheetState,
                containerColor = themedColor(light = Color.White, dark = Color(0xFF27343F)),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                BackHandler(enabled = state.connectionsGeofence != null) {
                    viewModel.closeConnections()
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = maxSheetHeight)
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 24.dp)
                        .navigationBarsPadding()
                ) {
                    AnimatedContent(
                        targetState = state.connectionsGeofence,
                        transitionSpec = {
                            if (targetState != null) {
                                (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                                    slideOutHorizontally { width -> width } + fadeOut()
                                )
                            } else {
                                (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                    slideOutHorizontally { width -> -width } + fadeOut()
                                )
                            }
                        },
                        label = "SheetContentTransition"
                    ) { targetGeofence ->
                        if (targetGeofence == null) {
                            GeofenceListPanel(
                                geofences = state.geofences,
                                onAddClick = { closeListSheet { viewModel.toggleAddingMode() } },
                                onDeleteClick = { viewModel.deleteGeofence(it) },
                                onItemClick = { id ->
                                    viewModel.selectGeofence(id)
                                    closeListSheet()
                                },
                                onConnectionsClick = { geofence ->
                                    viewModel.openConnections(geofence)
                                }
                            )
                        } else {
                            GeofenceConnectionsPanel(
                                geofence = targetGeofence,
                                state = state,
                                onBackClick = { viewModel.closeConnections() },
                                onToggleDevice = { deviceId, shouldLink ->
                                    viewModel.toggleDeviceConnection(targetGeofence.id, deviceId, shouldLink)
                                },
                                onToggleAll = { linkAll ->
                                    viewModel.toggleAllDeviceConnections(targetGeofence.id, linkAll)
                                },
                                onSearchQueryChange = { viewModel.updateConnectionsSearchQuery(it) },
                                onRefresh = { viewModel.refreshConnections() },
                                onLoadMore = { viewModel.loadMoreConnectionsDevices() }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * A non-modal bottom sheet surface: same shape, handle and elevation as
 * [ModalBottomSheet], but without the scrim, so the map behind it stays interactive.
 * Drag or tap the handle to switch between the collapsed and expanded height.
 */
@Composable
private fun BottomSheetSurface(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    keyboardOverlap: Dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val keyboardOpen = keyboardOverlap > 0.dp
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val screenHeight = LocalConfiguration.current.screenHeightDp
    val maxFraction = 0.92f

    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        color = themedColor(light = Color.White, dark = Color(0xFF27343F)),
        shadowElevation = 16.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = (screenHeight * maxFraction).dp)
        ) {
            SheetDragHandle(
                onDrag = { delta ->
                    if (delta < -4f) onExpandedChange(true)
                    if (delta > 4f) onExpandedChange(false)
                },
                onClick = { onExpandedChange(!expanded) }
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 16.dp),
                content = content
            )
        }
    }
}

@Composable
private fun SheetDragHandle(
    onDrag: (Float) -> Unit,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .draggable(
                orientation = Orientation.Vertical,
                state = rememberDraggableState { delta -> onDrag(delta) }
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(36.dp)
                .height(4.dp)
                .clip(CircleShape)
                .background(themedColor(light = Color(0xFFD6DCE1), dark = Color(0xFF4A5A66)))
        )
    }
}

@Composable
fun AddGeofencePanel(
    name: String,
    radius: Double,
    drawMode: DrawMode,
    pointsCount: Int,
    onNameChange: (String) -> Unit,
    onRadiusChange: (Double) -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit,
    onModeSelect: (DrawMode) -> Unit,
    onUndo: () -> Unit,
    onClear: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            stringResource(R.string.str_82e6d85b),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = themedColor(light = Color(0xFF384C5C), dark = Color(0xFFA0B5C5))
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = drawMode == DrawMode.CIRCLE,
                onClick = { onModeSelect(DrawMode.CIRCLE) },
                label = { Text(stringResource(R.string.str_9ccd7059)) }
            )
            FilterChip(
                selected = drawMode == DrawMode.POLYGON,
                onClick = { onModeSelect(DrawMode.POLYGON) },
                label = { Text(stringResource(R.string.str_255a1d7a)) }
            )
            FilterChip(
                selected = drawMode == DrawMode.LINESTRING,
                onClick = { onModeSelect(DrawMode.LINESTRING) },
                label = { Text(stringResource(R.string.str_fa2660b3)) }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        val instructionText = when (drawMode) {
            DrawMode.CIRCLE -> "روی نقشه ضربه بزنید تا مرکز دایره مشخص شود."
            DrawMode.POLYGON -> "برای رسم چند ضلعی، حداقل ۳ نقطه روی نقشه انتخاب کنید."
            DrawMode.LINESTRING -> "برای رسم مسیر، نقاط را روی نقشه انتخاب کنید."
        }
        Text(
            instructionText,
            fontSize = 12.sp,
            color = themedColor(light = Color.Gray, dark = Color(0xFFA0A0A0))
        )

        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text(stringResource(R.string.str_237a1703)) },
            modifier = Modifier.fillMaxWidth()
        )

        if (drawMode == DrawMode.CIRCLE) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("شعاع: ${radius.toInt()} متر", fontSize = 14.sp)
            Slider(
                value = radius.toFloat(),
                onValueChange = { onRadiusChange(it.toDouble()) },
                valueRange = 50f..5000f,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(
                    onClick = onUndo,
                    enabled = pointsCount > 0,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Icon(Icons.Default.Undo, contentDescription = "Undo", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.str_a7976da7), fontSize = 13.sp)
                }
                OutlinedButton(
                    onClick = onClear,
                    enabled = pointsCount > 0,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.str_ae6ae380), fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(
                    containerColor = themedColor(light = Color(0xFFF0F2F5), dark = Color(0xFF303030))
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text(
                    stringResource(R.string.str_c8d2a1fb),
                    color = themedColor(light = Color(0xFF676C70), dark = Color(0xFFE0E0E0)),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            val isValid = when (drawMode) {
                DrawMode.CIRCLE -> pointsCount > 0
                DrawMode.POLYGON -> pointsCount >= 3
                DrawMode.LINESTRING -> pointsCount >= 2
            }

            Button(
                onClick = onSave,
                colors = ButtonDefaults.buttonColors(containerColor = UzradyabTheme.colors.primary),
                shape = RoundedCornerShape(12.dp),
                enabled = isValid,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text(
                    stringResource(R.string.str_9b860f70),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun GeofenceListPanel(
    geofences: List<Geofence>,
    onAddClick: () -> Unit,
    onDeleteClick: (Long) -> Unit,
    onItemClick: (Long) -> Unit,
    onConnectionsClick: (Geofence) -> Unit
) {
    val geofenceToDelete = remember { mutableStateOf<Geofence?>(null) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.str_94f14b96),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = themedColor(light = Color(0xFF384C5C), dark = Color(0xFFA0B5C5))
            )
            Box(
                modifier = Modifier
                    .height(36.dp)
                    .background(UzradyabTheme.colors.primary, RoundedCornerShape(8.dp))
                    .clickable(onClick = onAddClick)
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Geofence",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.str_4dde4e62),
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (geofences.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    stringResource(R.string.str_593c9a2b),
                    color = themedColor(light = Color.Gray, dark = Color(0xFFA0A0A0))
                )
            }
        } else {
            // fill = false -> the list takes only as much room as it needs, and stops
            // at whatever the parent allows (92% of the screen). Past that it scrolls.
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(
                    items = geofences,
                    key = { geofence -> geofence.id }
                ) { geofence ->
                    GeofenceItem(
                        geofence = geofence,
                        onClick = { onItemClick(geofence.id) },
                        onDeleteClick = { geofenceToDelete.value = geofence },
                        onConnectionsClick = { onConnectionsClick(geofence) }
                    )
                }
            }
        }
    }

    if (geofenceToDelete.value != null) {
        DeleteGeofenceDialog(
            geofence = geofenceToDelete.value!!,
            onDismiss = { geofenceToDelete.value = null },
            onConfirm = {
                onDeleteClick(geofenceToDelete.value!!.id)
                geofenceToDelete.value = null
            }
        )
    }
}

@Composable
fun GeofenceItem(
    geofence: Geofence,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onConnectionsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(themedColor(light = Color(0xFFF7F9FA), dark = Color(0xFF182126)))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Layers, contentDescription = null, tint = UzradyabTheme.colors.primary)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(geofence.name, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            if (geofence.isCircle) {
                Text(
                    "شعاع: ${geofence.radius?.toInt()} متر",
                    fontSize = 12.sp,
                    color = themedColor(light = Color.Gray, dark = Color(0xFFA0A0A0))
                )
            } else {
                Text(
                    stringResource(R.string.str_255a1d7a),
                    fontSize = 12.sp,
                    color = themedColor(light = Color.Gray, dark = Color(0xFFA0A0A0))
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onConnectionsClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Link,
                    contentDescription = "اتصال دستگاه‌ها",
                    tint = UzradyabTheme.colors.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = themedColor(light = Color.Red, dark = Color(0xFFEF5350)).copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun DeleteGeofenceDialog(
    geofence: Geofence,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = themedColor(light = Color.White, dark = Color(0xFF27343F))
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = themedColor(light = Color(0xFFE55353), dark = Color(0xFF6F1111))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "حذف محدوده جغرافیایی ${geofence.name}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = themedColor(light = Color(0xFF384C5C), dark = Color(0xFFA0B5C5))
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.str_885a32f4),
                    fontSize = 13.sp,
                    color = themedColor(light = Color.Gray, dark = Color(0xFFA0A0A0)),
                    textAlign = TextAlign.Right,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            stringResource(R.string.str_c8d2a1fb),
                            color = UzradyabTheme.colors.primary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.width(24.dp))
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = themedColor(light = Color(0xFFE55353), dark = Color(0xFF6F1111))
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(44.dp)
                            .widthIn(min = 120.dp)
                    ) {
                        Text(
                            stringResource(R.string.str_5e8fdd3b),
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GeofenceConnectionsPanel(
    geofence: Geofence,
    state: GeofenceState,
    onBackClick: () -> Unit,
    onToggleDevice: (Long, Boolean) -> Unit,
    onToggleAll: (Boolean) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit = {}
) {
    val allDevices = state.connectionsDevices.items
    val query = state.connectionsSearchQuery
    val filteredDevices = remember(allDevices, query) {
        if (query.isBlank()) allDevices
        else allDevices.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.uniqueId.contains(query, ignoreCase = true)
        }
    }
    val linkedCount = remember(allDevices, state.linkedDeviceIds) {
        allDevices.count { it.id in state.linkedDeviceIds }
    }
    val allSelected = allDevices.isNotEmpty() && linkedCount == allDevices.size

    var isLocallyLoading by remember { mutableStateOf(false) }
    LaunchedEffect(state.connectionsLoading, allDevices.size) {
        if (!state.connectionsLoading) {
            isLocallyLoading = false
        }
    }

    val listState = rememberLazyListState()

    LaunchedEffect(listState, query, allDevices.size, state.connectionsLoading, isLocallyLoading) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val total = layoutInfo.totalItemsCount
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            total >= 10 && lastVisible >= total - 3
        }.collect { nearBottom ->
            if (nearBottom && query.isBlank() && !state.connectionsLoading && !isLocallyLoading) {
                isLocallyLoading = true
                onLoadMore()
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 1. Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(themedColor(light = Color(0xFFF0F4F8), dark = Color(0xFF1E2830)))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "بازگشت",
                    tint = themedColor(light = Color(0xFF384C5C), dark = Color(0xFFA0B5C5)),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "اتصال دستگاه‌ها",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = themedColor(light = Color(0xFF384C5C), dark = Color(0xFFA0B5C5))
                )
                Text(
                    text = "محدوده: ${geofence.name}",
                    fontSize = 12.sp,
                    color = UzradyabTheme.colors.primary,
                    fontWeight = FontWeight.Medium
                )
            }

            IconButton(
                onClick = onRefresh,
                enabled = !state.connectionsLoading,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(themedColor(light = Color(0xFFF0F4F8), dark = Color(0xFF1E2830)))
            ) {
                if (state.connectionsLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = UzradyabTheme.colors.primary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "بروزرسانی",
                        tint = themedColor(light = Color(0xFF384C5C), dark = Color(0xFFA0B5C5)),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // 2. Search Box
        OutlinedTextField(
            value = query,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            placeholder = {
                Text(
                    text = "جستجوی نام یا شناسه دستگاه...",
                    fontSize = 13.sp,
                    color = themedColor(light = Color.Gray, dark = Color(0xFFA0A0A0))
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "جستجو",
                    tint = themedColor(light = Color.Gray, dark = Color(0xFFA0A0A0)),
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "پاک کردن",
                            tint = themedColor(light = Color.Gray, dark = Color(0xFFA0A0A0)),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = themedColor(light = Color(0xFFF7F9FA), dark = Color(0xFF182126)),
                unfocusedContainerColor = themedColor(light = Color(0xFFF7F9FA), dark = Color(0xFF182126)),
                focusedBorderColor = UzradyabTheme.colors.primary,
                unfocusedBorderColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 3. Stats & Quick Action Bar
        if (allDevices.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(UzradyabTheme.colors.primary.copy(alpha = 0.1f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "$linkedCount از ${allDevices.size} دستگاه متصل است",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = UzradyabTheme.colors.primary
                    )
                }

                TextButton(
                    onClick = { onToggleAll(!allSelected) },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (allSelected) "لغو همه" else "انتخاب همه",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = UzradyabTheme.colors.primary
                    )
                }
            }
        }

        // 4. Error Message Banner if any
        if (state.connectionsError != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFFEBEE))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = state.connectionsError,
                    fontSize = 12.sp,
                    color = Color(0xFFD32F2F),
                    lineHeight = 18.sp
                )
            }
        }

        // 5. Device Items / Loading / Empty
        if (state.connectionsLoading && allDevices.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 48.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = UzradyabTheme.colors.primary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "در حال دریافت دستگاه‌ها از سرور...",
                        fontSize = 13.sp,
                        color = themedColor(light = Color.Gray, dark = Color(0xFFA0A0A0))
                    )
                }
            }
        } else if (filteredDevices.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 36.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = themedColor(light = Color.LightGray, dark = Color(0xFF4A5568)),
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (query.isNotBlank()) "دستگاهی مطابق با جستجو یافت نشد" else "هیچ دستگاهی ثبت نشده است",
                        fontSize = 13.sp,
                        color = themedColor(light = Color.Gray, dark = Color(0xFFA0A0A0))
                    )
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                contentPadding = PaddingValues(top = 4.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                itemsIndexed(
                    items = filteredDevices,
                    key = { _, device -> device.id }
                ) { index, device ->
                    if (index >= filteredDevices.size - 3 && query.isBlank() && !state.connectionsLoading && !isLocallyLoading && filteredDevices.size >= 10) {
                        LaunchedEffect(filteredDevices.size) {
                            isLocallyLoading = true
                            onLoadMore()
                        }
                    }

                    val isLinked = device.id in state.linkedDeviceIds
                    val isPending = device.id in state.pendingDeviceIds

                    DeviceConnectionItem(
                        device = device,
                        isLinked = isLinked,
                        isPending = isPending,
                        onToggle = { shouldLink -> onToggleDevice(device.id, shouldLink) }
                    )
                }

                if ((state.connectionsLoading || isLocallyLoading) && allDevices.isNotEmpty()) {
                    item(key = "loading_more_connections_indicator") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                strokeWidth = 3.dp,
                                color = UzradyabTheme.colors.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeviceConnectionItem(
    device: Device,
    isLinked: Boolean,
    isPending: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val isOnline = device.status.equals("online", ignoreCase = true)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(themedColor(light = Color(0xFFF7F9FA), dark = Color(0xFF182126)))
            .clickable(enabled = !isPending) { onToggle(!isLinked) }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Vehicle icon box
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (isLinked) UzradyabTheme.colors.primary.copy(alpha = 0.12f)
                    else themedColor(light = Color(0xFFE9EDF0), dark = Color(0xFF27343F))
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.DirectionsCar,
                contentDescription = null,
                tint = if (isLinked) UzradyabTheme.colors.primary
                else themedColor(light = Color(0xFF676C70), dark = Color(0xFF929292)),
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Device info
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = device.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = themedColor(light = Color(0xFF1E2830), dark = Color(0xFFF0F4F8))
                )
                Spacer(modifier = Modifier.width(6.dp))
                // Online/Offline status dot
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(if (isOnline) Color(0xFF4CAF50) else Color(0xFF9E9E9E))
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Unique ID / Plate - forced LTR as per Persian RTL rules
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Text(
                    text = device.uniqueId,
                    fontSize = 12.sp,
                    color = themedColor(light = Color.Gray, dark = Color(0xFFA0A0A0))
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Switch or Progress Indicator
        if (isPending) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
                color = UzradyabTheme.colors.primary
            )
        } else {
            Switch(
                checked = isLinked,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = themedColor(light = Color.White, dark = Color.White),
                    checkedTrackColor = UzradyabTheme.colors.primary
                )
            )
        }
    }
}