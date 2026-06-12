package com.example.uzradyab.core.utils

import java.util.Calendar
import java.util.TimeZone

object JalaliUtils {

    fun gregorianToJalali(gYear: Int, gMonth: Int, gDay: Int): IntArray {
        val gDaysInMonth = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        val jDaysInMonth = intArrayOf(31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)
        var gy = gYear - 1600
        var gm = gMonth - 1
        var gd = gDay - 1
        var gDayNo = 365 * gy + (gy + 3) / 4 - (gy + 99) / 100 + (gy + 399) / 400
        for (i in 0 until gm) {
            gDayNo += gDaysInMonth[i]
        }
        if (gm > 1 && ((gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0))) {
            gDayNo++
        }
        gDayNo += gd
        var jDayNo = gDayNo - 79
        val jNp = jDayNo / 12053
        jDayNo %= 12053
        var jy = 979 + 33 * jNp + 4 * (jDayNo / 1461)
        jDayNo %= 1461
        if (jDayNo >= 366) {
            jy += (jDayNo - 1) / 365
            jDayNo = (jDayNo - 1) % 365
        }
        var jm = 0
        while (jm < 11 && jDayNo >= jDaysInMonth[jm]) {
            jDayNo -= jDaysInMonth[jm]
            jm++
        }
        val jd = jDayNo + 1
        return intArrayOf(jy, jm + 1, jd)
    }

    private val jalaliMonths = arrayOf(
        "فروردین", "اردیبهشت", "خرداد",
        "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر",
        "دی", "بهمن", "اسفند"
    )

    fun getTodayJalaliString(): String {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tehran"))
        val gYear = cal.get(Calendar.YEAR)
        val gMonth = cal.get(Calendar.MONTH) + 1
        val gDay = cal.get(Calendar.DAY_OF_MONTH)
        
        val jDate = gregorianToJalali(gYear, gMonth, gDay)
        val day = jDate[2]
        val monthName = jalaliMonths[jDate[1] - 1]
        val year = jDate[0]

        return "امروز | $day $monthName $year".toPersianDigits()
    }

    fun jalaliToGregorian(jYear: Int, jMonth: Int, jDay: Int): IntArray {
        val gDaysInMonth = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        val jDaysInMonth = intArrayOf(31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)
        var jy = jYear - 979
        var jm = jMonth - 1
        var jd = jDay - 1
        var jDayNo = 365 * jy + (jy / 33) * 8 + (jy % 33 + 3) / 4
        for (i in 0 until jm) {
            jDayNo += jDaysInMonth[i]
        }
        jDayNo += jd
        var gDayNo = jDayNo + 79
        var gy = 1600 + 400 * (gDayNo / 146097)
        gDayNo %= 146097
        var leap = true
        if (gDayNo >= 36525) {
            gDayNo--
            gy += 100 * (gDayNo / 36524)
            gDayNo %= 36524
            if (gDayNo >= 365) {
                gDayNo++
            } else {
                leap = false
            }
        }
        gy += 4 * (gDayNo / 1461)
        gDayNo %= 1461
        if (gDayNo >= 366) {
            leap = false
            gDayNo--
            gy += gDayNo / 365
            gDayNo %= 365
        }
        if (leap) {
            gDaysInMonth[1] = 29
        }
        var gm = 0
        while (gm < 11 && gDayNo >= gDaysInMonth[gm]) {
            gDayNo -= gDaysInMonth[gm]
            gm++
        }
        val gd = gDayNo + 1
        return intArrayOf(gy, gm + 1, gd)
    }

    fun getDaysInJalaliMonth(year: Int, month: Int): Int {
        if (month in 1..6) return 31
        if (month in 7..11) return 30
        val a = (year - 474) % 2820
        val b = (a + 474) * 682
        val isLeap = (b % 2816) < 682
        return if (isLeap) 30 else 29
    }

    fun getDayOfWeekJalali(year: Int, month: Int, day: Int): Int {
        val g = jalaliToGregorian(year, month, day)
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tehran"))
        cal.set(g[0], g[1] - 1, g[2])
        val dow = cal.get(Calendar.DAY_OF_WEEK)
        return dow % 7
    }

    fun getMonthName(month: Int): String {
        return if (month in 1..12) jalaliMonths[month - 1] else ""
    }

    fun String.toPersianDigits(): String {
        val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        return buildString(length) {
            this@toPersianDigits.forEach { char ->
                append(if (char in '0'..'9') persianDigits[char - '0'] else char)
            }
        }
    }
}
