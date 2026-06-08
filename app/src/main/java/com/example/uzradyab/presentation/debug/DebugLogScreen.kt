package com.example.uzradyab.presentation.debug

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uzradyab.core.debug.AppLogger
import com.example.uzradyab.core.debug.LogEntry
import com.example.uzradyab.core.debug.LogLevel
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// Colors
// ─────────────────────────────────────────────────────────────────────────────
private val BgDark = Color(0xFF0D1117)
private val BgCard = Color(0xFF161B22)
private val BgCardAlt = Color(0xFF1C2128)
private val Border = Color(0xFF30363D)
private val TextPrimary = Color(0xFFE6EDF3)
private val TextSecondary = Color(0xFF8B949E)
private val TextMono = Color(0xFFCDD9E5)

private val ColorRequest = Color(0xFF58A6FF)  // blue — outgoing
private val ColorResponse = Color(0xFF3FB950) // green — success
private val ColorError = Color(0xFFF85149)    // red — error / 4xx/5xx
private val ColorInfo = Color(0xFFD2A8FF)     // purple — misc info

private val LevelFilters = listOf(null, LogLevel.REQUEST, LogLevel.RESPONSE, LogLevel.ERROR)

// ─────────────────────────────────────────────────────────────────────────────
// Screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun DebugLogScreen(onBackClick: () -> Unit) {
    val allLogs by AppLogger.logs.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedLevel by remember { mutableStateOf<LogLevel?>(null) }
    var autoScroll by remember { mutableStateOf(true) }

    val filtered by remember(allLogs, searchQuery, selectedLevel) {
        derivedStateOf {
            allLogs.filter { entry ->
                (selectedLevel == null || entry.level == selectedLevel) &&
                    (searchQuery.isBlank() ||
                        entry.tag.contains(searchQuery, ignoreCase = true) ||
                        entry.message.contains(searchQuery, ignoreCase = true))
            }
        }
    }

    // Auto-scroll to bottom when new entries arrive
    LaunchedEffect(filtered.size) {
        if (autoScroll && filtered.isNotEmpty()) {
            listState.animateScrollToItem(filtered.lastIndex)
        }
    }

    // Detect manual scroll up → disable auto-scroll
    val isAtBottom by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()
            last == null || last.index >= (filtered.size - 2)
        }
    }
    LaunchedEffect(isAtBottom) { autoScroll = isAtBottom }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BgDark),
        ) {
            // ── Top bar ───────────────────────────────────────────────────────
            DebugTopBar(
                count = filtered.size,
                onBackClick = onBackClick,
                onClearClick = { AppLogger.clear() },
            )

            // ── Filter chips ──────────────────────────────────────────────────
            LevelFilterRow(
                selected = selectedLevel,
                onSelect = { selectedLevel = it },
            )

            // ── Search ────────────────────────────────────────────────────────
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
            )

            // ── Log list ──────────────────────────────────────────────────────
            Box(modifier = Modifier.weight(1f)) {
                if (filtered.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📡", fontSize = 40.sp)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = if (allLogs.isEmpty()) "Waiting for network calls…" else "No entries match filter",
                                color = TextSecondary,
                                fontSize = 14.sp,
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        items(filtered, key = { it.id }) { entry ->
                            LogEntryCard(entry)
                        }
                    }
                }

                // Scroll-to-bottom FAB
                ScrollToBottomFab(
                    visible = !autoScroll,
                    onClick = {
                        scope.launch {
                            if (filtered.isNotEmpty()) listState.animateScrollToItem(filtered.lastIndex)
                            autoScroll = true
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp),
                )
            }

            Spacer(Modifier.navigationBarsPadding())
        }
    }
}

@Composable
private fun ScrollToBottomFab(
    visible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .shadow(6.dp, CircleShape)
                .background(ColorRequest, CircleShape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.ArrowDownward,
                contentDescription = "Scroll to bottom",
                tint = Color.White,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Top bar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DebugTopBar(count: Int, onBackClick: () -> Unit, onClearClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgCard)
            .statusBarsPadding()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Back button
        Box(
            modifier = Modifier
                .size(40.dp)
                .clickable(onClick = onBackClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = "Back",
                tint = TextPrimary,
                modifier = Modifier.size(24.dp),
            )
        }

        Spacer(Modifier.width(4.dp))

        // Title + live dot
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
        ) {
            // Pulsing live dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(ColorResponse, CircleShape),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Live Network Logs",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .background(Border, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            ) {
                Text(
                    text = "$count",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                )
            }
        }

        // Clear button
        Box(
            modifier = Modifier
                .size(40.dp)
                .clickable(onClick = onClearClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Clear logs",
                tint = ColorError,
                modifier = Modifier.size(20.dp),
            )
        }
    }
    Divider()
}

// ─────────────────────────────────────────────────────────────────────────────
// Filter chips
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun LevelFilterRow(selected: LogLevel?, onSelect: (LogLevel?) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgCard)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LevelFilters.forEach { level ->
            val label = level?.name ?: "ALL"
            val isSelected = selected == level
            val color = when (level) {
                LogLevel.REQUEST -> ColorRequest
                LogLevel.RESPONSE -> ColorResponse
                LogLevel.ERROR -> ColorError
                LogLevel.INFO -> ColorInfo
                null -> TextSecondary
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isSelected) color.copy(alpha = 0.15f) else Color.Transparent)
                    .border(1.dp, if (isSelected) color else Border, RoundedCornerShape(6.dp))
                    .clickable { onSelect(level) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    color = if (isSelected) color else TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                )
            }
        }
    }
    Divider()
}

// ─────────────────────────────────────────────────────────────────────────────
// Search bar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgCard)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(8.dp))
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            textStyle = TextStyle(
                color = TextPrimary,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
            ),
            cursorBrush = SolidColor(ColorRequest),
            decorationBox = { inner ->
                if (query.isEmpty()) {
                    Text("Search tag or message…", color = TextSecondary, fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace)
                }
                inner()
            },
        )
        if (query.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onQueryChange("") },
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextSecondary,
                    modifier = Modifier.size(14.dp))
            }
        }
    }
    Divider()
}

// ─────────────────────────────────────────────────────────────────────────────
// Log entry card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun LogEntryCard(entry: LogEntry) {
    var expanded by remember { mutableStateOf(false) }

    val levelColor = when (entry.level) {
        LogLevel.REQUEST -> ColorRequest
        LogLevel.RESPONSE -> ColorResponse
        LogLevel.ERROR -> ColorError
        LogLevel.INFO -> ColorInfo
    }

    val levelIcon = when (entry.level) {
        LogLevel.REQUEST -> "→"
        LogLevel.RESPONSE -> "←"
        LogLevel.ERROR -> "✗"
        LogLevel.INFO -> "ℹ"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (expanded) BgCardAlt else BgDark)
            .clickable { expanded = !expanded }
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        // ── Header row ────────────────────────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Level indicator dot
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .background(levelColor, CircleShape),
            )

            // Icon + tag
            Text(
                text = "$levelIcon ${entry.tag}",
                color = levelColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            // Status code badge
            if (entry.statusCode != null) {
                Box(
                    modifier = Modifier
                        .background(
                            if (entry.statusCode in 200..299) ColorResponse.copy(alpha = 0.15f)
                            else ColorError.copy(alpha = 0.15f),
                            RoundedCornerShape(4.dp),
                        )
                        .padding(horizontal = 5.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = "${entry.statusCode}",
                        color = if (entry.statusCode in 200..299) ColorResponse else ColorError,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            // Duration badge
            if (entry.durationMs != null) {
                Text(
                    text = "${entry.durationMs}ms",
                    color = if (entry.durationMs < 500) ColorResponse else if (entry.durationMs < 2000) Color(0xFFF0B429) else ColorError,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                )
            }

            // Timestamp
            Text(
                text = entry.timestamp,
                color = TextSecondary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
            )
        }

        // ── Message preview ───────────────────────────────────────────────────
        Spacer(Modifier.height(4.dp))
        Text(
            text = entry.message,
            color = TextMono,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            lineHeight = 16.sp,
            maxLines = if (expanded) Int.MAX_VALUE else 2,
            overflow = if (expanded) TextOverflow.Clip else TextOverflow.Ellipsis,
        )
    }

    // Row separator
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Border),
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Tiny divider
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun Divider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Border),
    )
}
