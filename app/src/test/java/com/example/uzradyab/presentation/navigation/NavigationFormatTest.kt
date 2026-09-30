package com.example.uzradyab.presentation.navigation

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.TimeZone

class NavigationFormatTest {

    @Test
    fun distance_roundsLikeTheDesign() {
        assertEquals("۸۰ متر", NavigationFormat.distance(78.0))
        assertEquals("۱۰ متر", NavigationFormat.distance(2.0))
        assertEquals("۱۲۵ متر", NavigationFormat.distance(127.0))
        assertEquals("۵۵۰ متر", NavigationFormat.distance(548.0))
        assertEquals("۱ کیلومتر", NavigationFormat.distance(995.0))
        assertEquals("۷٫۷ کیلومتر", NavigationFormat.distance(7_700.0))
        assertEquals("۹ کیلومتر", NavigationFormat.distance(9_004.0))
        assertEquals("۴۰ کیلومتر", NavigationFormat.distance(40_086.0))
    }

    @Test
    fun duration_formatsMinutesAndHours() {
        assertEquals("کمتر از ۱ دقیقه", NavigationFormat.duration(20.0))
        assertEquals("۲۴ دقیقه", NavigationFormat.duration(1_468.0))
        assertEquals("۱ ساعت", NavigationFormat.duration(3_600.0))
        assertEquals("۱ ساعت و ۵ دقیقه", NavigationFormat.duration(3_900.0))
    }

    @Test
    fun clock_usesPersianDigits() {
        val utc = TimeZone.getTimeZone("UTC")
        assertEquals("۱۶:۴۴", NavigationFormat.clock((16 * 60 + 44) * 60_000L, utc))
        assertEquals("۰۹:۰۵", NavigationFormat.clock((9 * 60 + 5) * 60_000L, utc))
        // 24-hour clock: no AM/PM, midnight is 00
        assertEquals("۲۳:۰۵", NavigationFormat.clock((23 * 60 + 5) * 60_000L, utc))
        assertEquals("۰۰:۱۰", NavigationFormat.clock(10 * 60_000L, utc))
    }

    @Test
    fun ordinalAndCompass() {
        assertEquals("دوم", NavigationFormat.ordinal(2))
        assertEquals("سوم", NavigationFormat.ordinal(3))
        assertEquals("شمال", NavigationFormat.compassDirection(353))
        assertEquals("جنوب شرقی", NavigationFormat.compassDirection(132))
    }
}
