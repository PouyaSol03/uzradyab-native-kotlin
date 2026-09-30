package com.example.uzradyab.presentation.navigation

import com.example.uzradyab.core.utils.FormatUtils.toPersianDigits
import java.util.Calendar
import java.util.TimeZone
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/** Persian text formatting for the routing screens. */
object NavigationFormat {

    /** "۸۰ متر", "۳۲۵ متر", "۷٫۷ کیلومتر", "۱۲ کیلومتر". */
    fun distance(meters: Double): String {
        val m = meters.coerceAtLeast(0.0)
        return when {
            m < 100 -> "${(((m / 10).roundToInt()) * 10).coerceAtLeast(10)} متر".toPersianDigits()
            m < 1000 -> {
                val rounded = ((m / 25).roundToInt()) * 25
                if (rounded >= 1000) "۱ کیلومتر" else "$rounded متر".toPersianDigits()
            }
            m < 10_000 -> {
                val tenths = (m / 100).roundToLong()
                val whole = tenths / 10
                val fraction = tenths % 10
                val text = if (fraction == 0L) "$whole" else "${whole}٫${fraction}"
                "$text کیلومتر".toPersianDigits()
            }
            else -> "${(m / 1000).roundToInt()} کیلومتر".toPersianDigits()
        }
    }

    /** "کمتر از ۱ دقیقه", "۲۰ دقیقه", "۱ ساعت و ۵ دقیقه". */
    fun duration(seconds: Double): String {
        val totalMinutes = (seconds.coerceAtLeast(0.0) / 60).roundToInt()
        if (totalMinutes < 1) return "کمتر از ۱ دقیقه"
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return when {
            hours == 0 -> "$minutes دقیقه"
            minutes == 0 -> "$hours ساعت"
            else -> "$hours ساعت و $minutes دقیقه"
        }.toPersianDigits()
    }

    /** "۱۶:۴۴" */
    fun clock(epochMillis: Long, timeZone: TimeZone = TimeZone.getDefault()): String {
        val calendar = Calendar.getInstance(timeZone).apply { timeInMillis = epochMillis }
        val hour = calendar.get(Calendar.HOUR_OF_DAY).toString().padStart(2, '0')
        val minute = calendar.get(Calendar.MINUTE).toString().padStart(2, '0')
        return "$hour:$minute".toPersianDigits()
    }

    fun eta(nowMillis: Long, remainingSeconds: Double): String =
        clock(nowMillis + (remainingSeconds.coerceAtLeast(0.0) * 1000).roundToLong())

    fun number(value: Int): String = value.toString().toPersianDigits()

    /** 1 -> "اول", 2 -> "دوم", ... */
    fun ordinal(n: Int): String = when (n) {
        1 -> "اول"
        2 -> "دوم"
        3 -> "سوم"
        4 -> "چهارم"
        5 -> "پنجم"
        6 -> "ششم"
        7 -> "هفتم"
        8 -> "هشتم"
        9 -> "نهم"
        10 -> "دهم"
        else -> "${n}م".toPersianDigits()
    }

    /** Compass direction for a bearing in degrees: "شمال", "شمال شرقی", ... */
    fun compassDirection(bearing: Int): String {
        val names = listOf("شمال", "شمال شرقی", "شرق", "جنوب شرقی", "جنوب", "جنوب غربی", "غرب", "شمال غربی")
        val index = (((bearing % 360 + 360) % 360 + 22.5) / 45).toInt() % 8
        return names[index]
    }

    /** "به تازگی", "۳ ساعت قبل", ... Same wording as the Home status card. */
    fun relativeTime(epochMillis: Long?, nowMillis: Long): String {
        if (epochMillis == null) return "نامشخص"
        val minutes = kotlin.math.abs(nowMillis - epochMillis) / 60_000
        val hours = minutes / 60
        val days = hours / 24
        return when {
            minutes <= 10 -> "به تازگی"
            minutes < 60 -> "$minutes دقیقه قبل".toPersianDigits()
            hours < 24 -> "$hours ساعت قبل".toPersianDigits()
            else -> "$days روز قبل".toPersianDigits()
        }
    }
}
