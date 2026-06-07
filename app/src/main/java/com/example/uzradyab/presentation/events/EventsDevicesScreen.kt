package com.example.uzradyab.presentation.events

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.uzradyab.domain.model.Event
import com.example.uzradyab.domain.repository.EventRepository
import com.example.uzradyab.ui.theme.AppBackground
import com.example.uzradyab.ui.theme.AppBlue
import com.example.uzradyab.ui.theme.AppTextMuted
import com.example.uzradyab.ui.theme.AppTextPrimary
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@Composable
fun EventsDevicesRoute(
    onBackClick: () -> Unit,
    viewModel: EventsDevicesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    EventsDevicesScreen(
        state = state,
        onBackClick = onBackClick,
    )
}

data class EventsDevicesUiState(
    val events: List<EventDeviceItem> = emptyList(),
)

data class EventDeviceItem(
    val id: Long,
    val title: String,
    val description: String,
    val time: String,
)

@HiltViewModel
class EventsDevicesViewModel @Inject constructor(
    eventRepository: EventRepository,
) : ViewModel() {
    val uiState: StateFlow<EventsDevicesUiState> = eventRepository.observeRecentEvents(limit = 50)
        .map { events ->
            EventsDevicesUiState(events = events.map(::eventToItem))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = EventsDevicesUiState(),
        )
}

@Composable
private fun EventsDevicesScreen(
    state: EventsDevicesUiState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .statusBarsPadding()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clickable(onClick = onBackClick),
                contentAlignment = Alignment.Center,
            ) {
                BackIcon()
            }
            Text(
                text = "گزارش رویدادها",
                color = AppTextPrimary,
                fontSize = 16.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        NotificationSettingsRow()
        Spacer(modifier = Modifier.height(18.dp))
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val events = state.events.ifEmpty { placeholderEvents() }
            items(events, key = { "${it.id}-${it.title}-${it.time}" }) { event ->
                EventCard(event = event)
            }
            item {
                Spacer(modifier = Modifier.height(10.dp))
                DateFilters()
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "دیروز | ۱۲ دی ۱۴۰۳",
                    color = AppTextPrimary,
                    fontSize = 12.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun NotificationSettingsRow() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "تنظیمات اعلان رویدادها",
                color = AppTextPrimary,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(modifier = Modifier.size(10.dp))
            SettingsIcon()
        }
    }
}

@Composable
private fun EventCard(event: EventDeviceItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(74.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = event.time,
                color = AppTextMuted,
                fontSize = 12.sp,
                lineHeight = 20.sp,
            )
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    color = AppTextPrimary,
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
                    color = Color(0xFF8F99A3),
                    fontSize = 12.sp,
                    lineHeight = 20.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(modifier = Modifier.size(10.dp))
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(Color(0xFFE5B850), CircleShape),
            )
        }
    }
}

@Composable
private fun DateFilters() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        listOf("ماه جاری", "هفته جاری", "دیروز", "امروز", "انتخاب تاریخ").forEach { label ->
            Box(
                modifier = Modifier
                    .height(32.dp)
                    .background(if (label == "دیروز") AppBlue else Color.White, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    color = if (label == "دیروز") Color.White else AppTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                )
            }
        }
    }
}



@Composable
private fun BackIcon() {
    Icon(
        imageVector = Icons.Default.ChevronRight,
        contentDescription = "Back",
        tint = AppTextPrimary,
        modifier = Modifier.size(24.dp)
    )
}

@Composable
private fun SettingsIcon() {
    Icon(
        imageVector = Icons.Default.Settings,
        contentDescription = "Settings",
        tint = AppBlue,
        modifier = Modifier.size(24.dp)
    )
}

private fun eventToItem(event: Event): EventDeviceItem {
    val title = eventTitle(event.type)
    return EventDeviceItem(
        id = event.id,
        title = title,
        description = eventDescription(event.type),
        time = event.eventTime?.let(::formatTime).orEmpty(),
    )
}

private fun eventTitle(type: String): String {
    return when (type) {
        "ignitionOn" -> "روشن شدن موتور"
        "ignitionOff" -> "خاموش شدن موتور"
        "geofenceExit" -> "خروج از محدوده جغرافیایی"
        "geofenceEnter" -> "ورود به محدوده جغرافیایی"
        "deviceOverspeed" -> "سرعت غیر مجاز"
        "alarm" -> "هشدار دستگاه"
        else -> if (type.isBlank()) "رویداد جدید" else type
    }
}

private fun eventDescription(type: String): String {
    return when (type) {
        "ignitionOn" -> "دستگاه روشن شده است."
        "ignitionOff" -> "دستگاه خاموش شده است."
        "geofenceExit" -> "دستگاه از محدوده جغرافیایی خارج شده است."
        "geofenceEnter" -> "دستگاه وارد محدوده جغرافیایی شده است."
        "deviceOverspeed" -> "سرعت دستگاه از میزان تنظیم شده عبور کرده است."
        "alarm" -> "هشدار جدید برای دستگاه ثبت شده است."
        else -> "رویداد دستگاه ثبت شده است."
    }
}

private fun placeholderEvents(): List<EventDeviceItem> {
    return listOf(
        EventDeviceItem(1, "روشن شدن موتور", "دستگاه روشن شده است.", "22:00"),
        EventDeviceItem(2, "خروج از محدوده جغرافیایی", "دستگاه از محدوده جغرافیایی 1 خارج شده است.", "23:10"),
        EventDeviceItem(3, "سرعت غیر مجاز", "سرعت دستگاه از میزان تنظیم شده عبور کرده است.", "23:20"),
    )
}

private fun formatTime(value: String): String {
    val parsed = listOf(
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
    } ?: return ""
    return SimpleDateFormat("HH:mm", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("Asia/Tehran")
    }.format(parsed).toPersianDigits()
}

private fun String.toPersianDigits(): String {
    val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    return buildString(length) {
        this@toPersianDigits.forEach { char ->
            append(if (char in '0'..'9') persianDigits[char - '0'] else char)
        }
    }
}
