package com.example.uzradyab.presentation.profile

import com.example.uzradyab.core.utils.FormatUtils
import org.junit.Assert.assertEquals
import org.junit.Test

class ProfileExpiryTest {
    private val now = FormatUtils.parseIsoDate("2026-10-17T08:00:00Z")!!

    @Test
    fun farExpiryIsActive() {
        val expiry = ProfileExpiryFormatter.from("2027-03-15T20:30:00.000+00:00", now)
        assertEquals(ExpiryTone.Active, expiry.tone)
        // 2027-03-16 00:00 Tehran = 25 Esfand 1405
        assertEquals("اعتبار حساب تا ۲۵ اسفند ۱۴۰۵", expiry.label)
    }

    @Test
    fun withinSevenDaysIsSoon() {
        assertEquals(ExpiryTone.Soon, ProfileExpiryFormatter.from("2026-10-24T08:00:00Z", now).tone)
        assertEquals(ExpiryTone.Active, ProfileExpiryFormatter.from("2026-10-24T08:00:01Z", now).tone)
    }

    @Test
    fun pastDateIsExpired() {
        val expiry = ProfileExpiryFormatter.from("2026-10-16T08:00:00Z", now)
        assertEquals(ExpiryTone.Expired, expiry.tone)
        assertEquals("اعتبار حساب در ۲۴ مهر ۱۴۰۵ پایان یافت", expiry.label)
    }

    @Test
    fun missingDateIsUnknown() {
        assertEquals(ExpiryTone.Unknown, ProfileExpiryFormatter.from(null, now).tone)
        assertEquals(ExpiryTone.Unknown, ProfileExpiryFormatter.from("garbage", now).tone)
    }
}
