package com.example.uzradyab.presentation.replay

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.uzradyab.presentation.map.AppTopToolbar

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.Calendar
import com.example.uzradyab.R
import androidx.compose.ui.res.stringResource
import com.example.uzradyab.ui.theme.UzradyabTheme
import com.example.uzradyab.ui.theme.themedColor

@Composable
fun ReplayTripRoute(
    onBackClick: () -> Unit,
    viewModel: ReplayViewModel = hiltViewModel()
) {
    ReplayTripScreen(onBackClick = onBackClick, viewModel = viewModel)
}

@Composable
fun ReplayTripScreen(
    onBackClick: () -> Unit,
    viewModel: ReplayViewModel
) {
    val state by viewModel.state.collectAsState()

    var showNodes by remember { mutableStateOf(false) }
    var selectedPosition by remember { mutableStateOf<com.example.uzradyab.domain.model.Position?>(null) }
    var showFilterSheet by remember { mutableStateOf(false) }

    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }
    var showDetailsSheet by remember { mutableStateOf(false) }

    /*
    LaunchedEffect(state.error) {
        state.error?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = androidx.compose.material3.SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }
    */

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(themedColor(light = Color.White, dark = Color(0xFF27343F)))
        ) {
            // Top Toolbar Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                // Top Toolbar (AppTopToolbar handles LTR internally, so we pass start/end carefully)
                AppTopToolbar(
                    modifier = Modifier.height(64.dp),
                    startContent = {
                        com.example.uzradyab.presentation.map.BackButton(onClick = onBackClick)
                    },
                    centerContent = {
                        Text(
                            text = stringResource(R.string.str_1deb7f66),
                            color = themedColor(light = Color(0xFF676C70), dark = Color(0xFF929292)),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                )

                // Time Filter Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Date label
                    Text(
                        text = state.dateFilterText,
                        color = themedColor(light = Color(0xFF333638), dark = Color(0xFFAFB3B6)),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Box(
                        modifier = Modifier
                            .border(1.dp, UzradyabTheme.colors.primary, RoundedCornerShape(8.dp))
                            .background(themedColor(light = Color.White, dark = Color(0xFF27343F)), RoundedCornerShape(8.dp))
                            .clickable { showFilterSheet = true }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FilterAlt,
                                contentDescription = null,
                                tint = UzradyabTheme.colors.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.str_9150c5be),
                                color = UzradyabTheme.colors.primary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Map Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(themedColor(light = Color(0xFFF7F9FA), dark = Color(0xFF182126)))
            ) {
                ReplayMap(
                    positions = state.positions,
                    currentIndex = state.currentIndex,
                    mapStyle = state.mapStyle,
                    playSpeed = state.playSpeed,
                    onNodeClick = { selectedPosition = it },
                    mapBottomPadding = 200.dp, // Space for the bottom panel
                    modifier = Modifier.fillMaxSize()
                )

                // Loading Overlay
                androidx.compose.animation.AnimatedVisibility(
                    visible = state.isLoading,
                    enter = androidx.compose.animation.fadeIn(),
                    exit = androidx.compose.animation.fadeOut(),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(themedColor(light = Color(0xFFE8F0F6), dark = Color(0xFF11212C)).copy(alpha = 0.6f))
                            .clickable(enabled = false) {}, // Intercept clicks
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = themedColor(light = Color.White, dark = Color(0xFF27343F))),
                            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(horizontal = 40.dp, vertical = 32.dp)
                            ) {
                                androidx.compose.material3.CircularProgressIndicator(
                                    color = UzradyabTheme.colors.primary,
                                    strokeWidth = 4.dp,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                Text(
                                    text = stringResource(R.string.str_9a8bd9a1),
                                    color = themedColor(light = Color(0xFF2C3E50), dark = Color(0xFF9CB2C9)),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = stringResource(R.string.str_3f246adf),
                                    color = themedColor(light = Color(0xFF6A8BA5), dark = Color(0xFF99A7B3)),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Overlay for Selected Node Detail
                androidx.compose.animation.AnimatedVisibility(
                    visible = selectedPosition != null,
                    enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.slideInVertically(),
                    exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.slideOutVertically(),
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp, start = 16.dp, end = 16.dp)
                ) {
                    selectedPosition?.let { pos ->
                        NodeDetailCard(position = pos, onClose = { selectedPosition = null })
                    }
                }

                // Custom Error Snackbar
                /*
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                ) {
                    androidx.compose.material3.SnackbarHost(
                        hostState = snackbarHostState,
                        snackbar = { data ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = themedColor(light = Color(0xFFFDECEA), dark = Color(0xFF380B05))),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = themedColor(light = Color(0xFFE55353), dark = Color(0xFF6F1111))
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = data.visuals.message,
                                        color = themedColor(light = Color(0xFFE55353), dark = Color(0xFF6F1111)),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    )
                }
                */
                // Bottom Panel
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(bottom = 16.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    ReplayBottomPanel(
                        state = state, 
                        viewModel = viewModel, 
                        onShowFilter = { showFilterSheet = true },
                        onShowDetails = { showDetailsSheet = true }
                    )
                }
            }
        }
    }

    if (showFilterSheet) {
        TimeFilterBottomSheet(
            onDismiss = { showFilterSheet = false },
            onApplyQuickRange = { range ->
                showFilterSheet = false
                viewModel.applyQuickRange(range)
            },
            onApplyCustomRange = { start, end ->
                showFilterSheet = false  
                viewModel.applyCustomRange(start, end)
            }
        )
    }

    if (showDetailsSheet) {
        TripDetailsBottomSheet(
            state = state,
            onDismiss = { showDetailsSheet = false }
        )
    }
}

@Composable
fun ReplayBottomPanel(
    state: ReplayUiState,
    viewModel: ReplayViewModel,
    onShowFilter: () -> Unit,
    onShowDetails: () -> Unit
) {
    Card(
        modifier = Modifier
            .widthIn(max = 343.dp)
            .fillMaxWidth(0.9f),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = themedColor(light = Color.White, dark = Color(0xFF27343F))),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ReplayTimelineInfo(state = state, onShowFilter = onShowFilter)

            Spacer(modifier = Modifier.height(8.dp))

            if (state.isPlaying || state.currentIndex > 0) {
                PlayingControls(
                    state = state,
                    onIndexChange = { viewModel.setIndex(it) },
                    onStopPlayback = { viewModel.stopPlayback() },
                    onTogglePlayback = { viewModel.togglePlayback() },
                    onToggleSpeed = { viewModel.toggleSpeed() },
                )
            } else {
                DefaultControls(
                    onTogglePlayback = { viewModel.togglePlayback() },
                    onDetailsClick = onShowDetails
                )
            }
        }
    }
}

@Composable
fun ReplayTimelineInfo(state: ReplayUiState, onShowFilter: () -> Unit) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Dotted line from icon centers
            val baseLineColor = themedColor(light = Color(0xFFC0CDD8), dark = Color(0xFF31414F))
            val progressLineColor = themedColor(light = Color(0xFFA12887), dark = Color(0xFFE184CD))
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxWidth().height(24.dp).padding(top = 12.dp)) {
                val y = 0f
                val startX = 24.dp.toPx()
                val endX = size.width - 24.dp.toPx()
                
                drawLine(
                    color = baseLineColor,
                    start = androidx.compose.ui.geometry.Offset(startX, y),
                    end = androidx.compose.ui.geometry.Offset(endX, y),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
                )
                
                if (state.currentIndex > 0) {
                    val progress = state.currentIndex.toFloat() / state.positions.lastIndex.coerceAtLeast(1)
                    val currentX = startX + (endX - startX) * progress
                    drawLine(
                        color = progressLineColor,
                        start = androidx.compose.ui.geometry.Offset(startX, y),
                        end = androidx.compose.ui.geometry.Offset(currentX, y),
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Start
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = themedColor(light = Color(0xFF00C89B), dark = Color(0xFF66FFDD))
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        TimeBalloon(
                            time = formatTimeOnly(state.positions.firstOrNull()?.fixTime),
                            date = formatDateOnly(state.positions.firstOrNull()?.fixTime)
                        )
                    }
                }

                // Distance
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(33.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .background(themedColor(light = Color(0xFF6A8BA5), dark = Color(0xFF99A7B3)), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.str_b19fc85c),
                                    color = themedColor(light = Color(0xFFEFF3F5), dark = Color(0xFF182126)),
                                    fontSize = 10.sp
                                )
//                                Spacer(modifier = Modifier.height(1.dp))
                                Text(
                                    text = state.totalDistanceText,
                                    color = themedColor(light = Color.White, dark = Color.White),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // End
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = themedColor(light = Color(0xFFE55353), dark = Color(0xFF6F1111))
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        TimeBalloon(
                            time = formatTimeOnly(state.positions.items.lastOrNull()?.fixTime),
                            date = formatDateOnly(state.positions.items.lastOrNull()?.fixTime)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DefaultControls(onTogglePlayback: () -> Unit, onDetailsClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .width(102.dp)
                .height(40.dp)
                .border(1.dp, themedColor(light = Color(0xFF307EF3), dark = Color(0xFF5F98EC)), RoundedCornerShape(8.dp))
                .clickable(onClick = onDetailsClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.str_d65b37fd),
                color = themedColor(light = Color(0xFF307EF3), dark = Color(0xFF5F98EC)),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
        
        Box(
            modifier = Modifier
                .weight(1f)
                .height(40.dp)
                .border(1.dp, themedColor(light = Color(0xFF307EF3), dark = Color(0xFF5F98EC)), RoundedCornerShape(8.dp))
                .clickable(onClick = onTogglePlayback),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.str_7083cfdf),
                    color = themedColor(light = Color(0xFF307EF3), dark = Color(0xFF5F98EC)),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Outlined.PlayCircleOutline,
                    contentDescription = null,
                    tint = themedColor(light = Color(0xFF307EF3), dark = Color(0xFF5F98EC)),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun PlayingControls(
    state: ReplayUiState,
    onIndexChange: (Int) -> Unit,
    onStopPlayback: () -> Unit,
    onTogglePlayback: () -> Unit,
    onToggleSpeed: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Right: 2x
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(themedColor(light = Color(0xFFA12887), dark = Color(0xFFE184CD)).copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                .clickable(onClick = onToggleSpeed),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${state.playSpeed}x",
                color = themedColor(light = Color(0xFFA12887), dark = Color(0xFFE184CD)),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Middle: Scrubbable Progress
        val progress = if (state.positions.items.isEmpty()) 0f else state.currentIndex.toFloat() / state.positions.items.lastIndex.coerceAtLeast(1)
        ScrubbableProgress(
            progress = progress,
            isPlaying = state.isPlaying,
            onProgressChange = { p ->
                val max = state.positions.items.lastIndex.coerceAtLeast(0)
                onIndexChange((p * max).toInt())
            },
            onTogglePlayback = onTogglePlayback,
            modifier = Modifier.weight(1f)
        )

        // Left: Stop
        Box(
            modifier = Modifier
                .width(54.dp)
                .height(40.dp)
                .border(1.dp, themedColor(light = Color(0xFF676C70), dark = Color(0xFF929292)), RoundedCornerShape(8.dp))
                .clickable(onClick = onStopPlayback),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.StopCircle,
                contentDescription = "Stop",
                tint = themedColor(light = Color(0xFF676C70), dark = Color(0xFF929292)),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun ScrubbableProgress(
    progress: Float,
    isPlaying: Boolean,
    onProgressChange: (Float) -> Unit,
    onTogglePlayback: () -> Unit,
    modifier: Modifier = Modifier
) {
    var width by remember { mutableStateOf(1f) }
    
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(
            modifier = modifier
                .height(40.dp)
                .border(1.dp, themedColor(light = Color(0xFFA12887), dark = Color(0xFFE184CD)), RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { offset ->
                            val p = (offset.x / width).coerceIn(0f, 1f)
                            onProgressChange(p)
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        val p = (change.position.x / width).coerceIn(0f, 1f)
                        onProgressChange(p)
                    }
                }
                .onSizeChanged { width = it.width.toFloat().coerceAtLeast(1f) }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .background(themedColor(light = Color(0xFFA12887), dark = Color(0xFFE184CD)).copy(alpha = 0.14f))
            )
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(onClick = onTogglePlayback),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                    contentDescription = null,
                    tint = themedColor(light = Color(0xFFA12887), dark = Color(0xFFE184CD)),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun TimeBalloon(time: String, date: String? = null) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val balloonColor = themedColor(light = Color(0xFFEFF3F5), dark = Color(0xFF182126))
        androidx.compose.foundation.Canvas(modifier = Modifier.size(8.dp, 7.dp)) {
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(size.width / 2f, 0f)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(path, balloonColor)
        }
        Column(
            modifier = Modifier
                .width(if (date != null) 72.dp else 49.dp)
                .height(if (date != null) 40.dp else 28.dp)
                .background(themedColor(light = Color(0xFFEFF3F5), dark = Color(0xFF182126)), RoundedCornerShape(8.dp)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = time,
                color = themedColor(light = Color(0xFF384C5C), dark = Color(0xFFA0B5C5)),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            if (date != null) {
                Text(
                    text = date,
                    color = themedColor(light = Color(0xFF6A8BA5), dark = Color(0xFF99A7B3)),
                    fontSize = 9.sp,
                    modifier = Modifier.offset(y = (-2).dp)
                )
            }
        }
    }
}

@Composable
fun NodeDetailCard(position: com.example.uzradyab.domain.model.Position, onClose: () -> Unit) {
    val speedKmh = (position.speed * 1.852).toInt()
    val isMoving = speedKmh > 0
    val statusColor = themedColor(light = Color(0xFFA12887), dark = Color(0xFFE184CD)) // Uniform purple color
    val statusText = if (isMoving) "در حال حرکت" else "توقف"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = themedColor(light = Color.White, dark = Color(0xFF27343F))),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Status indicator with glow
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(statusColor, androidx.compose.foundation.shape.CircleShape)
                        .border(2.dp, statusColor.copy(alpha = 0.3f), androidx.compose.foundation.shape.CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.titleMedium,
                        color = themedColor(light = Color(0xFF384C5C), dark = Color(0xFFA0B5C5)),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "سرعت: ${speedKmh.toString().toPersianDigits()} کیلومتر بر ساعت",
                        style = MaterialTheme.typography.bodyMedium,
                        color = themedColor(light = Color(0xFF6A8BA5), dark = Color(0xFF99A7B3))
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "زمان: ${formatTimeOnly(position.fixTime)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = themedColor(light = Color(0xFF6A8BA5), dark = Color(0xFF99A7B3))
                    )
                }
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(32.dp)
                    .background(themedColor(light = Color(0xFFEFF3F5), dark = Color(0xFF182126)), androidx.compose.foundation.shape.CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = themedColor(light = Color(0xFF6A8BA5), dark = Color(0xFF99A7B3)),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

private fun formatTimeOnly(isoString: String?): String {
    if (isoString == null) return "--:--"
    val date = parseServerDate(isoString) ?: return "--:--"
    val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tehran")).apply { time = date }
    val h = cal.get(Calendar.HOUR_OF_DAY).toString().padStart(2, '0')
    val m = cal.get(Calendar.MINUTE).toString().padStart(2, '0')
    return com.example.uzradyab.core.utils.JalaliUtils.run { "$h:$m".toPersianDigits() }
}

private fun formatDateOnly(isoString: String?): String? {
    if (isoString == null) return null
    val date = parseServerDate(isoString) ?: return null
    val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tehran")).apply { time = date }
    val gY = cal.get(Calendar.YEAR)
    val gM = cal.get(Calendar.MONTH) + 1
    val gD = cal.get(Calendar.DAY_OF_MONTH)
    val jDate = com.example.uzradyab.core.utils.JalaliUtils.gregorianToJalali(gY, gM, gD)
    val y = jDate[0]
    val m = jDate[1].toString().padStart(2, '0')
    val d = jDate[2].toString().padStart(2, '0')
    return com.example.uzradyab.core.utils.JalaliUtils.run { "$y/$m/$d".toPersianDigits() }
}

private fun parseServerDate(value: String?): Date? {
    if (value.isNullOrBlank()) return null
    return listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSX",
        "yyyy-MM-dd'T'HH:mm:ssX",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
    ).firstNotNullOfOrNull { pattern ->
        runCatching {
            SimpleDateFormat(pattern, Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.parse(value)
        }.getOrNull()
    }
}

private fun String.toPersianDigits(): String {
    val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    return buildString(length) {
        this@toPersianDigits.forEach { char ->
            append(if (char in '0'..'9') persianDigits[char - '0'] else char)
        }
    }
}
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun TimeFilterBottomSheet(
    onDismiss: () -> Unit,
    onApplyQuickRange: (String) -> Unit,
    onApplyCustomRange: (com.example.uzradyab.presentation.components.JalaliDateTime?, com.example.uzradyab.presentation.components.JalaliDateTime?) -> Unit
) {
    val sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedRange by remember { mutableStateOf("امروز") }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    var customStart by remember { mutableStateOf<com.example.uzradyab.presentation.components.JalaliDateTime?>(null) }
    var customEnd by remember { mutableStateOf<com.example.uzradyab.presentation.components.JalaliDateTime?>(null) }
    
    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = themedColor(light = Color(0xFFF7F9FA), dark = Color(0xFF182126))
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            if (showStartPicker) {
                com.example.uzradyab.presentation.components.JalaliDateTimePicker(
                    title = stringResource(R.string.str_6bc54204),
                    initialDateTime = customStart,
                    onConfirm = { 
                        customStart = it
                        showStartPicker = false
                    },
                    onCancel = { showStartPicker = false }
                )
            } else if (showEndPicker) {
                com.example.uzradyab.presentation.components.JalaliDateTimePicker(
                    title = stringResource(R.string.str_694c9923),
                    initialDateTime = customEnd,
                    onConfirm = { 
                        customEnd = it
                        showEndPicker = false
                    },
                    onCancel = { showEndPicker = false }
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 24.dp), // Reduced top & bottom padding
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.str_04945136),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = themedColor(light = Color(0xFF384C5C), dark = Color(0xFFA0B5C5)),
                        modifier = Modifier.padding(bottom = 24.dp) // Reduced bottom padding
                    )
                    
                    val ranges = listOf("امروز", "دیروز", "هفته جاری", "ماه جاری")
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            QuickRangeButton(text = ranges[0], isSelected = selectedRange == ranges[0], onClick = { selectedRange = ranges[0]; customStart = null; customEnd = null }, modifier = Modifier.weight(1f))
                            QuickRangeButton(text = ranges[1], isSelected = selectedRange == ranges[1], onClick = { selectedRange = ranges[1]; customStart = null; customEnd = null }, modifier = Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            QuickRangeButton(text = ranges[2], isSelected = selectedRange == ranges[2], onClick = { selectedRange = ranges[2]; customStart = null; customEnd = null }, modifier = Modifier.weight(1f))
                            QuickRangeButton(text = ranges[3], isSelected = selectedRange == ranges[3], onClick = { selectedRange = ranges[3]; customStart = null; customEnd = null }, modifier = Modifier.weight(1f))
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                        Text(stringResource(R.string.str_ece143b5), fontSize = 14.sp, color = themedColor(light = Color(0xFF6A8BA5), dark = Color(0xFF99A7B3)), modifier = Modifier.padding(bottom = 8.dp))
                        OutlinedButton(
                            onClick = { showStartPicker = true },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = themedColor(light = Color.White, dark = Color(0xFF27343F)), contentColor = themedColor(light = Color(0xFF384C5C), dark = Color(0xFFA0B5C5))),
                            border = androidx.compose.foundation.BorderStroke(1.dp, themedColor(light = Color(0xFFC0CDD8), dark = Color(0xFF31414F)))
                        ) {
                            Text(if (customStart != null) "${customStart!!.year}/${customStart!!.month.toString().padStart(2, '0')}/${customStart!!.day.toString().padStart(2, '0')} ${customStart!!.hour.toString().padStart(2, '0')}:${customStart!!.minute.toString().padStart(2, '0')}".toPersianDigits() else "انتخاب کنید")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                        Text(stringResource(R.string.str_9e0476de), fontSize = 14.sp, color = themedColor(light = Color(0xFF6A8BA5), dark = Color(0xFF99A7B3)), modifier = Modifier.padding(bottom = 8.dp))
                        OutlinedButton(
                            onClick = { showEndPicker = true },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = themedColor(light = Color.White, dark = Color(0xFF27343F)), contentColor = themedColor(light = Color(0xFF384C5C), dark = Color(0xFFA0B5C5))),
                            border = androidx.compose.foundation.BorderStroke(1.dp, themedColor(light = Color(0xFFC0CDD8), dark = Color(0xFF31414F)))
                        ) {
                            Text(if (customEnd != null) "${customEnd!!.year}/${customEnd!!.month.toString().padStart(2, '0')}/${customEnd!!.day.toString().padStart(2, '0')} ${customEnd!!.hour.toString().padStart(2, '0')}:${customEnd!!.minute.toString().padStart(2, '0')}".toPersianDigits() else "انتخاب کنید")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp)) // Reduced spacing before buttons
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = themedColor(light = Color(0xFFEFF3F5), dark = Color(0xFF182126)), contentColor = themedColor(light = Color(0xFF6A8BA5), dark = Color(0xFF99A7B3))),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(stringResource(R.string.str_c8d2a1fb), fontSize = 16.sp)
                        }
                        Button(
                            onClick = {
                                if (customStart != null) {
                                    onApplyCustomRange(customStart, customEnd)
                                } else {
                                    onApplyQuickRange(selectedRange)
                                }
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = UzradyabTheme.colors.primary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(stringResource(R.string.str_606f279a), fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickRangeButton(text: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) UzradyabTheme.colors.primary else themedColor(light = Color.White, dark = Color(0xFF27343F)),
            contentColor = if (isSelected) themedColor(light = Color.White, dark = Color(0xFF27343F)) else themedColor(light = Color(0xFF6A8BA5), dark = Color(0xFF99A7B3))
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, themedColor(light = Color(0xFFC0CDD8), dark = Color(0xFF31414F))),
        elevation = null
    ) {
        Text(text, fontSize = 12.sp)
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun TripDetailsBottomSheet(
    state: ReplayUiState,
    onDismiss: () -> Unit
) {
    val sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)
    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = themedColor(light = Color(0xFFF7F9FA), dark = Color(0xFF182126))
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "جزئیات مسیر پیموده شده",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = themedColor(light = Color(0xFF384C5C), dark = Color(0xFFA0B5C5)),
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DetailsCard(
                        title = "زمان شروع",
                        value = state.positions.items.firstOrNull()?.fixTime?.let { "${formatDateOnly(it)} - ${formatTimeOnly(it)}" } ?: "--:--",
                        modifier = Modifier.weight(1f)
                    )
                    DetailsCard(
                        title = "زمان پایان",
                        value = state.positions.items.lastOrNull()?.fixTime?.let { "${formatDateOnly(it)} - ${formatTimeOnly(it)}" } ?: "--:--",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                val startPos = state.positions.items.firstOrNull()
                LocationCard(
                    title = "مبدا",
                    address = state.startAddress ?: "در حال دریافت...",
                    lat = startPos?.latitude,
                    lon = startPos?.longitude
                )

                Spacer(modifier = Modifier.height(12.dp))

                val endPos = state.positions.items.lastOrNull()
                LocationCard(
                    title = "مقصد",
                    address = state.endAddress ?: "در حال دریافت...",
                    lat = endPos?.latitude,
                    lon = endPos?.longitude
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DetailsCard(
                        title = "تعداد توقف‌ها",
                        value = "۰".toPersianDigits(),
                        modifier = Modifier.weight(1f)
                    )
                    DetailsCard(
                        title = "مسافت پیموده شده",
                        value = state.totalDistanceText,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = themedColor(light = Color(0xFF307EF3), dark = Color(0xFF5F98EC)))
                ) {
                    Text("متوجه شدم", color = themedColor(light = Color.White, dark = Color.White), fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun DetailsCard(title: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .border(1.dp, themedColor(light = Color(0xFFC0CDD8), dark = Color(0xFF31414F)), RoundedCornerShape(12.dp))
            .background(themedColor(light = Color.White, dark = Color(0xFF27343F)), RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(title, fontSize = 12.sp, color = themedColor(light = Color.Gray, dark = Color(0xFFA0A0A0)))
        Spacer(modifier = Modifier.height(8.dp))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = themedColor(light = Color(0xFF384C5C), dark = Color(0xFFA0B5C5)), modifier = Modifier.align(Alignment.End))
    }
}

@Composable
private fun LocationCard(title: String, address: String, lat: Double?, lon: Double?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, themedColor(light = Color(0xFFC0CDD8), dark = Color(0xFF31414F)), RoundedCornerShape(12.dp))
            .background(themedColor(light = Color.White, dark = Color(0xFF27343F)), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(title, fontSize = 12.sp, color = themedColor(light = Color.Gray, dark = Color(0xFFA0A0A0)))
        Spacer(modifier = Modifier.height(8.dp))
        Text(address, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = themedColor(light = Color(0xFF384C5C), dark = Color(0xFFA0B5C5)))
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("مختصات", fontSize = 12.sp, color = themedColor(light = Color.Gray, dark = Color(0xFFA0A0A0)))
            val coordText = if (lat != null && lon != null) "${lat.toString().take(9)} | ${lon.toString().take(9)}" else "-- | --"
            Text(coordText.toPersianDigits(), fontSize = 12.sp, color = themedColor(light = Color(0xFF6A8BA5), dark = Color(0xFF99A7B3)), fontWeight = FontWeight.Medium)
        }
    }
}
