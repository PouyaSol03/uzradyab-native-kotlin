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
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
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
import com.example.uzradyab.ui.theme.AppBlue
import com.example.uzradyab.ui.theme.AppTextPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.Calendar

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

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
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
                        // In LTR context of AppTopToolbar, start is Left, end is Right.
                        // For RTL app, Back button is usually on the Right, Logo/Menu on Left.
                        // Let's use standard BackButton
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            com.example.uzradyab.presentation.map.BackButton(onClick = onBackClick)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "مسیرهای پیموده",
                                color = Color(0xFF676C70),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    },
                    endContent = {
                        // Pill "۱۲ مسیر"
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF333638), RoundedCornerShape(32.dp))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "۱۲ مسیر", // Mocked count
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
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
                        text = "امروز | ۱۴ دی ۱۴۰۳", // Mocked date
                        color = Color(0xFF333638),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Box(
                        modifier = Modifier
                            .border(1.dp, AppBlue, RoundedCornerShape(8.dp))
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .clickable { /* TODO Date Picker */ }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FilterAlt,
                                contentDescription = null,
                                tint = AppBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "فیلتر زمان",
                                color = AppBlue,
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
                    .background(Color(0xFFF7F9FA))
            ) {
                ReplayMap(
                    positions = state.positions,
                    currentIndex = state.currentIndex,
                    mapBottomPadding = 200.dp, // Space for the bottom panel
                    modifier = Modifier.fillMaxSize()
                )

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
                        onIndexChange = { viewModel.setIndex(it) },
                        onTogglePlayback = { viewModel.togglePlayback() },
                        onStopPlayback = { viewModel.stopPlayback() },
                        onToggleSpeed = { viewModel.toggleSpeed() },
                    )
                }
            }
        }
    }
}

@Composable
fun ReplayBottomPanel(
    state: ReplayUiState,
    onIndexChange: (Int) -> Unit,
    onTogglePlayback: () -> Unit,
    onStopPlayback: () -> Unit,
    onToggleSpeed: () -> Unit,
) {
    Card(
        modifier = Modifier
            .widthIn(max = 343.dp)
            .fillMaxWidth(0.9f),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Row: Start/End/Distance/Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Start Balloon
                TimeBalloon(
                    label = "شروع",
                    time = formatTimeOnly(state.positions.firstOrNull()?.fixTime)
                )

                // Slider Area
                Column(
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF6A8BA5), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "مسافت: ${state.totalDistanceText}",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Slider(
                            value = state.currentIndex.toFloat(),
                            onValueChange = { onIndexChange(it.toInt()) },
                            valueRange = 0f..(state.positions.lastIndex.coerceAtLeast(1).toFloat()),
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFA12887),
                                activeTrackColor = Color(0xFFA12887),
                                inactiveTrackColor = Color(0xFFA12887).copy(alpha = 0.2f)
                            )
                        )
                    }
                }

                // End Balloon
                TimeBalloon(
                    label = "پایان",
                    time = formatTimeOnly(state.positions.lastOrNull()?.fixTime)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Row: Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (state.isPlaying) {
                    // Playing State Controls
                    // 2x button on the right in RTL
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFA12887).copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .clickable(onClick = onToggleSpeed),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${state.playSpeed}x",
                            color = Color(0xFFA12887),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Pause
                        Box(
                            modifier = Modifier
                                .height(40.dp)
                                .border(1.dp, Color(0xFFA12887), RoundedCornerShape(8.dp))
                                .clickable(onClick = onTogglePlayback)
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "توقف مسیر",
                                    color = Color(0xFFA12887),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.Pause,
                                    contentDescription = "Pause",
                                    tint = Color(0xFFA12887)
                                )
                            }
                        }

                        // Stop
                        Box(
                            modifier = Modifier
                                .height(40.dp)
                                .border(1.dp, Color(0xFF676C70), RoundedCornerShape(8.dp))
                                .clickable(onClick = onStopPlayback)
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "پایان مسیر",
                                    color = Color(0xFF676C70),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = "Stop",
                                    tint = Color(0xFF676C70)
                                )
                            }
                        }
                    }
                } else {
                    // Default State Controls
                    Box(
                        modifier = Modifier
                            .height(40.dp)
                            .border(1.dp, AppBlue, RoundedCornerShape(8.dp))
                            .clickable { /* TODO */ }
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "جزئیات",
                                color = AppBlue,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = "Details",
                                tint = AppBlue
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .height(40.dp)
                            .border(1.dp, AppBlue, RoundedCornerShape(8.dp))
                            .clickable(onClick = onTogglePlayback)
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "بازپخش مسیر",
                                color = AppBlue,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = AppBlue
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimeBalloon(label: String, time: String) {
    Column(
        modifier = Modifier
            .background(Color(0xFFEFF3F5), RoundedCornerShape(8.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = Color(0xFF6A8BA5),
            fontSize = 10.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = time,
            color = Color(0xFF384C5C),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatTimeOnly(isoString: String?): String {
    if (isoString.isNullOrBlank()) return "--:--"
    val date = parseServerDate(isoString) ?: return "--:--"
    val cal = Calendar.getInstance()
    cal.time = date
    val hr = cal.get(Calendar.HOUR_OF_DAY)
    val mn = cal.get(Calendar.MINUTE)
    return "${hr.toString().padStart(2, '0')}:${mn.toString().padStart(2, '0')}".toPersianDigits()
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
