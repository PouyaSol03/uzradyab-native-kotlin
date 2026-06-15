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
                        text = formatEventType(latest.type ?: ""),
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
            "all" -> "همه رویدادها"
            "deviceOnline" -> "وضعیت آنلاین"
            "deviceUnknown" -> "وضعیت نامعلوم"
            "deviceOffline" -> "وضعیت آفلاین"
            "deviceInactive" -> "دستگاه غیرفعال"
            "queuedCommandSent" -> "Queued command sent"
            "deviceMoving" -> "حرکت دستگاه"
            "deviceStopped" -> "دستگاه متوقف شد"
            "deviceOverspeed" -> "سرعت از حد مجاز فراتر رفت"
            "deviceFuelDrop" -> "افت سوخت"
            "deviceFuelIncrease" -> "افزایش سوخت"
            "commandResult" -> "نتیجه ارسال دستور"
            "geofenceEnter" -> "ورود محدوده جغرافیایی"
            "geofenceExit" -> "خروج محدوده جغرافیایی"
            "alarm" -> "هشدار"
            "ignitionOn" -> "سویچ روشن"
            "ignitionOff" -> "سوئیچ خاموش"
            "maintenance" -> "نیاز به تعمیر"
            "textMessage" -> "پیامک دریافت شد"
            "driverChanged" -> "تعویض راننده"
            "media" -> "مدیا"
            else -> if (type.isBlank()) "رویداد جدید" else type
        }
    }
}
