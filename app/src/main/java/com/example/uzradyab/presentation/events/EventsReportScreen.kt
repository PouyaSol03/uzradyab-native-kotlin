package com.example.uzradyab.presentation.events

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.uzradyab.presentation.map.AppTopToolbar
import com.example.uzradyab.presentation.map.BackButton
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import com.example.uzradyab.R
import androidx.compose.ui.res.stringResource
import com.example.uzradyab.ui.theme.UzradyabTheme
import com.example.uzradyab.ui.theme.themedColor

@Composable
fun EventsReportRoute(
    onBackClick: () -> Unit,
    onNotificationSettingsClick: () -> Unit = {},
    viewModel: EventsReportViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    EventsReportScreen(
        state = state,
        onBackClick = onBackClick,
        onFilterChange = viewModel::setDateFilter,
        onCustomFilterChange = viewModel::setCustomDateFilter,
        onClearError = viewModel::clearError,
        onLoadMore = viewModel::loadNextPage,
        onNotificationSettingsClick = onNotificationSettingsClick
    )
}

@Composable
fun EventsReportScreen(
    state: EventsReportUiState,
    onBackClick: () -> Unit,
    onFilterChange: (EventDateFilter) -> Unit,
    onCustomFilterChange: (Long, Long) -> Unit = { _, _ -> },
    onClearError: () -> Unit,
    onLoadMore: () -> Unit,
    onNotificationSettingsClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = androidx.compose.runtime.remember { SnackbarHostState() }
    var showFilterSheet by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    /*
    androidx.compose.runtime.LaunchedEffect(state.error) {
        state.error?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            onClearError()
        }
    }
    */

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
                                text = stringResource(R.string.str_f58f20ca),
                                color = themedColor(light = Color(0xFF676C70), dark = Color(0xFF929292)),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                            )
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = themedColor(light = Color(0xFF676C70), dark = Color(0xFF929292)),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    modifier = Modifier
                        .statusBarsPadding()
                        .height(64.dp)
                )
            },
            containerColor = UzradyabTheme.colors.background,
        ) { innerPadding ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
        DateFilters(
            selectedFilter = state.dateFilter,
            onFilterChange = onFilterChange
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(themedColor(light = Color.White, dark = Color(0xFF27343F)), RoundedCornerShape(12.dp))
                .border(1.dp, UzradyabTheme.colors.textPrimary, RoundedCornerShape(12.dp))
                .clickable { showFilterSheet = true },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.str_04945136),
                color = UzradyabTheme.colors.textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Text(
                text = state.filterText,
                color = UzradyabTheme.colors.textPrimary,
                fontSize = 12.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Right,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
                    .background(themedColor(light = Color(0xFFE7EEF5), dark = Color(0xFF121F2B)), RoundedCornerShape(12.dp))
                    .padding(vertical = 6.dp, horizontal = 12.dp)
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (state.isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = UzradyabTheme.colors.primary)
                    }
                }
            } else if (state.events.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.str_41ea99ff), color = UzradyabTheme.colors.textMuted, fontSize = 14.sp)
                    }
                }
            } else {
                itemsIndexed(state.events.items, key = { _, it -> it.id }) { index, event ->
                    EventCard(event = event)
                    
                    if (index == state.events.items.lastIndex && state.hasMore) {
                        androidx.compose.runtime.LaunchedEffect(Unit) {
                            onLoadMore()
                        }
                    }
                }
                
                if (state.hasMore) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = UzradyabTheme.colors.primary, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        NotificationSettingsRow(onClick = onNotificationSettingsClick)
        
        Spacer(modifier = Modifier.height(16.dp))
            }

            // Custom Error Snackbar
            /*
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
            ) {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.align(Alignment.TopCenter),
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
        }
    }

    if (showFilterSheet) {
        com.example.uzradyab.presentation.replay.TimeFilterBottomSheet(
            onDismiss = { showFilterSheet = false },
            onApplyQuickRange = { range ->
                showFilterSheet = false
                val filter = when (range) {
                    "امروز" -> EventDateFilter.Today
                    "دیروز" -> EventDateFilter.Yesterday
                    "هفته جاری" -> EventDateFilter.CurrentWeek
                    "ماه جاری" -> EventDateFilter.CurrentMonth
                    else -> EventDateFilter.Today
                }
                onFilterChange(filter)
            },
            onApplyCustomRange = { start, end ->
                showFilterSheet = false
                if (start != null) {
                    val startCal = com.example.uzradyab.core.utils.JalaliUtils.jalaliToGregorian(start.year, start.month, start.day)
                    
                    val sCal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Tehran")).apply {
                        set(startCal[0], startCal[1] - 1, startCal[2], start.hour, start.minute, 0)
                    }
                    val eCal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Tehran"))
                    if (end != null) {
                        val endCal = com.example.uzradyab.core.utils.JalaliUtils.jalaliToGregorian(end.year, end.month, end.day)
                        eCal.set(endCal[0], endCal[1] - 1, endCal[2], end.hour, end.minute, 59)
                    }
                    onCustomFilterChange(sCal.timeInMillis, eCal.timeInMillis)
                }
            }
        )
    }
}

@Composable
private fun NotificationSettingsRow(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = themedColor(light = Color(0xFF307EF3), dark = Color(0xFF5F98EC))
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, themedColor(light = Color(0xFF307EF3), dark = Color(0xFF5F98EC)))
    ) {
        Text(
            text = stringResource(R.string.str_2c0a74b8),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun EventCard(event: EventUiModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(74.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = themedColor(light = Color.White, dark = Color(0xFF27343F))),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(themedColor(light = Color(0xFFE5B850), dark = Color(0xFF6F5210)), CircleShape),
            )
            Spacer(modifier = Modifier.size(10.dp))
            Column(horizontalAlignment = Alignment.Start, modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    color = UzradyabTheme.colors.textPrimary,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = event.description,
                    color = themedColor(light = Color(0xFF8F99A3), dark = Color(0xFFA6A6A6)),
                    fontSize = 12.sp,
                    lineHeight = 20.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Text(
                text = event.time,
                color = themedColor(light = Color.Black, dark = Color(0xFFE0E0E0)),
                fontSize = 12.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Left,
            )
        }
    }
}

@Composable
private fun DateFilters(
    selectedFilter: EventDateFilter,
    onFilterChange: (EventDateFilter) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        val filters = listOf(
            EventDateFilter.CurrentMonth to "ماه جاری",
            EventDateFilter.CurrentWeek to "هفته جاری",
            EventDateFilter.Yesterday to "دیروز",
            EventDateFilter.Today to "امروز"
        )
        
        filters.forEach { (filter, label) ->
            val isSelected = selectedFilter == filter
            Box(
                modifier = Modifier
                    .height(32.dp)
                    .background(if (isSelected) UzradyabTheme.colors.primary else themedColor(light = Color.White, dark = Color(0xFF27343F)), RoundedCornerShape(8.dp))
                    .border(1.dp, if (isSelected) UzradyabTheme.colors.primary else themedColor(light = Color(0xFFE3E8EE), dark = Color(0xFF171E26)), RoundedCornerShape(8.dp))
                    .clickable { onFilterChange(filter) }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    color = if (isSelected) themedColor(light = Color.White, dark = Color(0xFF27343F)) else UzradyabTheme.colors.textPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                )
            }
        }
    }
}

