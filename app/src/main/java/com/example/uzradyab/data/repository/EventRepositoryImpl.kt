package com.example.uzradyab.data.repository

import com.example.uzradyab.core.debug.AppLogger
import com.example.uzradyab.core.debug.LogLevel
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
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

private const val NOTIFICATION_BASE_URL = "https://notification.uzradyab.ir/"

class EventRepositoryImpl @Inject constructor(
    private val eventDao: EventDao,
    private val traccarApi: TraccarApi,
    private val client: OkHttpClient,
    private val gson: Gson,
) : EventRepository {
    override fun observeRecentEvents(limit: Int): Flow<List<Event>> {
        return eventDao.observeRecentEvents(limit).map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun fetchLatestDeviceEvents(deviceId: Long): Result<List<LatestNotificationEvent>> = withContext(Dispatchers.IO) {
        runCatching {
            val encodedId = URLEncoder.encode(deviceId.toString(), StandardCharsets.UTF_8.name())
            val request = Request.Builder()
                .url("${NOTIFICATION_BASE_URL}handle_events/latest/$encodedId/")
                .header("Accept", "application/json")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.code == 404) return@runCatching emptyList()
                check(response.isSuccessful) { response.body?.string().orEmpty() }
                parseNotificationBody(response.body?.string().orEmpty()).take(1)
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
            to = to
        ).map { it.toDomain() }
        AppLogger.log(LogLevel.RESPONSE, "EventReport", "Received ${result.size} events")
        result
    }.onFailure { e ->
        AppLogger.log(LogLevel.ERROR, "EventReport", "Failed to fetch events: ${e.message}")
    }

    private fun parseNotificationBody(body: String): List<LatestNotificationEvent> {
        if (body.isBlank()) return emptyList()
        return runCatching {
            normalizeLatestEvents(JsonParser.parseString(body))
        }.getOrElse {
            listOf(LatestNotificationEvent(id = body, text = body, time = null))
        }
    }

    private fun normalizeLatestEvents(element: JsonElement): List<LatestNotificationEvent> {
        return when {
            element.isJsonArray -> element.asJsonArray.mapNotNull(::latestEventFromJson)
            element.isJsonObject -> {
                val obj = element.asJsonObject
                val list = listOf("results", "events", "items", "data", "latest")
                    .firstNotNullOfOrNull { key -> obj.get(key)?.takeIf { it.isJsonArray } }
                if (list != null) list.asJsonArray.mapNotNull(::latestEventFromJson) else listOfNotNull(latestEventFromJson(obj))
            }
            element.isJsonPrimitive -> listOf(
                LatestNotificationEvent(
                    id = element.asString,
                    text = element.asString,
                    time = null,
                ),
            )
            else -> emptyList()
        }
    }

    private fun latestEventFromJson(element: JsonElement): LatestNotificationEvent? {
        if (element.isJsonPrimitive) {
            return LatestNotificationEvent(id = element.asString, text = element.asString, time = null)
        }
        if (!element.isJsonObject) return null
        val obj = element.asJsonObject
        val text = obj.firstString("title", "message", "description", "text", "name")
            ?: obj.firstString("type")?.let(::formatEventType)
            ?: return null
        val time = obj.firstString("eventTime", "time", "timestamp", "createdAt", "created_at")
        return LatestNotificationEvent(
            id = obj.firstString("id", "eventId") ?: "$text-${time.orEmpty()}",
            text = text,
            time = time,
        )
    }

    private fun JsonObject.firstString(vararg keys: String): String? {
        return keys.firstNotNullOfOrNull { key ->
            get(key)?.takeIf { !it.isJsonNull }?.let { element ->
                runCatching { gson.fromJson(element, String::class.java) }.getOrNull()
            }
        }?.takeIf { it.isNotBlank() }
    }

    private fun formatEventType(type: String): String {
        return when (type) {
            "deviceOnline" -> "دستگاه آنلاین شد"
            "deviceOffline" -> "دستگاه آفلاین شد"
            "deviceUnknown" -> "وضعیت دستگاه نامشخص شد"
            "ignitionOn" -> "روشن شدن موتور"
            "ignitionOff" -> "خاموش شدن موتور"
            "geofenceEnter" -> "ورود به محدوده جغرافیایی"
            "geofenceExit" -> "خروج از محدوده جغرافیایی"
            "deviceOverspeed" -> "سرعت غیر مجاز"
            "alarm" -> "هشدار دستگاه"
            else -> type
        }
    }
}
