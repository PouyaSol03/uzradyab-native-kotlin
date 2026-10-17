package com.example.uzradyab.core.utils

import com.google.gson.JsonObject
import com.google.gson.JsonParser

/**
 * Persian labels for Traccar event and alarm types.
 *
 * Mirrors the `event*` and `alarm*` keys of the web client's `fa.json` and its
 * `formatNotificationTitle` logic, so alarm events read e.g. "هشدار باطری کم"
 * instead of a bare "هشدار".
 */
object EventLabels {

    private const val UNKNOWN_EVENT = "رویداد جدید"

    private val eventTypes = mapOf(
        "all" to "همه رویدادها",
        "deviceOnline" to "وضعیت آنلاین",
        "deviceUnknown" to "وضعیت نامعلوم",
        "deviceOffline" to "وضعیت آفلاین",
        "deviceInactive" to "دستگاه غیرفعال",
        "queuedCommandSent" to "Queued command sent",
        "deviceMoving" to "حرکت دستگاه",
        "deviceStopped" to "دستگاه متوقف شد",
        "deviceOverspeed" to "سرعت از حد مجاز فراتر رفت",
        "deviceFuelDrop" to "افت سوخت",
        "deviceFuelIncrease" to "افزایش سوخت",
        "commandResult" to "نتیجه ارسال دستور",
        "geofenceEnter" to "ورود محدوده جغرافیایی",
        "geofenceExit" to "خروج محدوده جغرافیایی",
        "alarm" to "هشدار",
        "ignitionOn" to "سویچ روشن",
        "ignitionOff" to "سوئیچ خاموش",
        "maintenance" to "نیاز به تعمیر",
        "textMessage" to "پیامک دریافت شد",
        "driverChanged" to "تعویض راننده",
        "media" to "مدیا",
    )

    private val alarmTypes = mapOf(
        "general" to "اصلی",
        "sos" to "درخواست کمک",
        "vibration" to "لرزش",
        "movement" to "جابجایی",
        "lowspeed" to "سرعت کم",
        "overspeed" to "سرعت بالا",
        "fallDown" to "سقوط",
        "lowPower" to "انرژی کم",
        "lowBattery" to "باطری کم",
        "fault" to "نقص",
        "powerOff" to "خاموش",
        "powerOn" to "روشن",
        "door" to "درب",
        "lock" to "قفل",
        "unlock" to "باز کردن",
        "geofence" to "محدوده جغرافیایی",
        "geofenceEnter" to "ورود محدوده جغرافیایی",
        "geofenceExit" to "خروج محدوده جغرافیایی",
        "gpsAntennaCut" to "قطع آنتن GPS",
        "accident" to "تصادف",
        "tow" to "یدک کش",
        "idle" to "بیکار",
        "highRpm" to "دورموتور بالا",
        "hardAcceleration" to "شتاب تند",
        "hardBraking" to "ترمز تند",
        "hardCornering" to "زاویه شدید",
        "laneChange" to "تغییر مسیر",
        "fatigueDriving" to "رانندگی خسته",
        "powerCut" to "قطع ولتاژ",
        "powerRestored" to "برگشت ولتاژ",
        "jamming" to "مسدود",
        "temperature" to "دما",
        "parking" to "پارکینگ",
        "bonnet" to "صندوق",
        "footBrake" to "پدال ترمز",
        "fuelLeak" to "نشت باک",
        "tampering" to "دستکاری",
        "removing" to "حذف",
    )

    fun eventType(type: String): String =
        eventTypes[type] ?: type.ifBlank { UNKNOWN_EVENT }

    fun alarm(alarm: String): String = alarmTypes[alarm] ?: alarm

    /**
     * Title for an event. For `alarm` events the subtype is appended; when the
     * device reports several comma-separated alarms only their count is shown.
     */
    fun title(type: String, alarm: String?): String {
        val base = eventType(type)
        if (type != "alarm" || alarm.isNullOrBlank()) return base
        val alarms = alarm.split(',').map { it.trim() }.filter { it.isNotEmpty() }
        return when {
            alarms.isEmpty() -> base
            alarms.size > 1 -> "$base (${alarms.size})"
            else -> "$base ${alarm(alarms.first())}"
        }
    }

    fun title(type: String, attributes: JsonObject?): String =
        title(type, attributes?.alarmValue())

    fun titleFromAttributesJson(type: String, attributesJson: String?): String {
        if (type != "alarm" || attributesJson.isNullOrBlank()) return eventType(type)
        val attributes = runCatching {
            JsonParser.parseString(attributesJson).takeIf { it.isJsonObject }?.asJsonObject
        }.getOrNull()
        return title(type, attributes)
    }

    /** Device reply text of a `commandResult` event (`attributes.result`), or null if empty. */
    fun commandResultFromAttributesJson(attributesJson: String?): String? {
        if (attributesJson.isNullOrBlank()) return null
        val attributes = runCatching {
            JsonParser.parseString(attributesJson).takeIf { it.isJsonObject }?.asJsonObject
        }.getOrNull() ?: return null
        val element = attributes.get("result") ?: return null
        return if (element.isJsonPrimitive) element.asString.trim().takeIf { it.isNotEmpty() } else null
    }

    private fun JsonObject.alarmValue(): String? {
        val element = get("alarm") ?: return null
        return if (element.isJsonPrimitive) element.asString else null
    }
}
