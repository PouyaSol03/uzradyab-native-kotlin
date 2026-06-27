package com.example.uzradyab.core.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

object FormatUtils {
    // ThreadLocal instances for SimpleDateFormat because it's not thread-safe.
    // This drastically reduces allocations and GC pressure during list rendering.
    
    private val isoFormatters = object : ThreadLocal<List<SimpleDateFormat>>() {
        override fun initialValue(): List<SimpleDateFormat> {
            val tz = TimeZone.getTimeZone("UTC")
            return listOf(
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.US).apply { timeZone = tz },
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.US).apply { timeZone = tz },
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply { timeZone = tz },
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply { timeZone = tz }
            )
        }
    }

    private val timeFormatterTehran = object : ThreadLocal<SimpleDateFormat>() {
        override fun initialValue(): SimpleDateFormat {
            return SimpleDateFormat("HH:mm", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("Asia/Tehran")
            }
        }
    }

    private val calendarTehran = object : ThreadLocal<Calendar>() {
        override fun initialValue(): Calendar {
            return Calendar.getInstance(TimeZone.getTimeZone("Asia/Tehran"))
        }
    }

    fun parseIsoDate(value: String?): java.util.Date? {
        if (value.isNullOrBlank()) return null
        val formatters = isoFormatters.get() ?: return null
        for (formatter in formatters) {
            try {
                return formatter.parse(value)
            } catch (e: Exception) {
                // Ignore and try next
            }
        }
        return null
    }

    /**
     * Formats an ISO date string to a Persian Jalali date string (e.g. "1402/05/23 - 14:30")
     */
    fun formatEventTimeJalali(value: String?): String {
        val parsedDate = parseIsoDate(value) ?: return ""
        
        val cal = calendarTehran.get()!!
        cal.time = parsedDate
        
        val gY = cal.get(Calendar.YEAR)
        val gM = cal.get(Calendar.MONTH) + 1
        val gD = cal.get(Calendar.DAY_OF_MONTH)
        
        val jDate = JalaliUtils.gregorianToJalali(gY, gM, gD)
        val timeStr = timeFormatterTehran.get()!!.format(parsedDate)
        
        val jYStr = jDate[0].toString()
        val jMStr = jDate[1].toString().padStart(2, '0')
        val jDStr = jDate[2].toString().padStart(2, '0')
        
        return "$jYStr/$jMStr/$jDStr - $timeStr".toPersianDigits()
    }

    fun String.toPersianDigits(): String {
        val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        val chars = this.toCharArray()
        for (i in chars.indices) {
            val char = chars[i]
            if (char in '0'..'9') {
                chars[i] = persianDigits[char - '0']
            }
        }
        return String(chars)
    }
    
    fun formatDoublePersian(value: Double, decimals: Int = 1): String {
        return String.format(Locale.US, "%.${decimals}f", value).toPersianDigits()
    }
}
