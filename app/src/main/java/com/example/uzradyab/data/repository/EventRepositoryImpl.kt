package com.example.uzradyab.data.repository

import com.example.uzradyab.core.debug.AppLogger
import com.example.uzradyab.core.debug.LogLevel
import com.example.uzradyab.core.utils.EventLabels
import com.example.uzradyab.data.local.dao.EventDao
import com.example.uzradyab.data.mapper.toDomain
import com.example.uzradyab.data.remote.api.TraccarApi
import com.example.uzradyab.domain.model.Event
import com.example.uzradyab.domain.model.LatestNotificationEvent
import com.example.uzradyab.domain.repository.EventRepository
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class EventRepositoryImpl @Inject constructor(
    private val eventDao: EventDao,
    private val traccarApi: TraccarApi,
    private val gson: Gson,
) : EventRepository {
    override fun observeRecentEvents(limit: Int): Flow<List<Event>> {
        return eventDao.observeRecentEvents(limit).map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun fetchLatestDeviceEvents(deviceId: Long): Result<List<LatestNotificationEvent>> = withContext(Dispatchers.IO) {
        runCatching {
            val format = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).apply {
                timeZone = java.util.TimeZone.getTimeZone("UTC")
            }
            val cal = java.util.Calendar.getInstance()
            val to = format.format(cal.time)
            cal.add(java.util.Calendar.DAY_OF_YEAR, -7)
            val from = format.format(cal.time)

            val events = traccarApi.getEventsReport(
                deviceId = deviceId,
                from = from,
                to = to,
                type = "allEvents"
            )

            val latest = events.maxByOrNull { it.eventTime ?: "" }

            if (latest != null) {
                listOf(
                    LatestNotificationEvent(
                        id = latest.id.toString(),
                        text = EventLabels.title(latest.type ?: "", latest.attributes),
                        time = latest.eventTime
                    )
                )
            } else {
                emptyList()
            }
        }
    }

    override suspend fun getEventsReport(
        deviceId: Long,
        from: String,
        to: String
    ): Result<List<Event>> = runCatching {
        AppLogger.log(LogLevel.REQUEST, "EventReport", "Fetching events for $deviceId from $from to $to")
        val result = traccarApi.getEventsReport(
            deviceId = deviceId,
            from = from,
            to = to,
            type = "allEvents"
        ).map { it.toDomain() }
        AppLogger.log(LogLevel.RESPONSE, "EventReport", "Received ${result.size} events")
        result
    }.onFailure { e ->
        AppLogger.log(LogLevel.ERROR, "EventReport", "Failed to fetch events: ${e.message}")
    }
}
