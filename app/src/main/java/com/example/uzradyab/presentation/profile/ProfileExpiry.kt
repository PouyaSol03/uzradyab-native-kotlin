package com.example.uzradyab.presentation.profile

import com.example.uzradyab.core.utils.FormatUtils
import com.example.uzradyab.core.utils.JalaliUtils
import com.example.uzradyab.core.utils.JalaliUtils.toPersianDigits
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

enum class ExpiryTone { Active, Soon, Expired, Unknown }

/** Text and tone of the account expiry chip on the profile header. */
data class ProfileExpiry(val label: String, val tone: ExpiryTone)

object ProfileExpiryFormatter {
    /** The chip turns red this many days before the account expires. */
    const val WARNING_DAYS = 7

    private const val DAY_MS = 24L * 60 * 60 * 1000

    fun from(expirationTime: String?, now: Date = Date()): ProfileExpiry {
        val date = FormatUtils.parseIsoDate(expirationTime)
            ?: return ProfileExpiry("تاریخ انقضا نامشخص", ExpiryTone.Unknown)

        val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tehran")).apply { time = date }
        val jDate = JalaliUtils.gregorianToJalali(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
        val dateText = "${jDate[2]} ${JalaliUtils.getMonthName(jDate[1])} ${jDate[0]}".toPersianDigits()

        val msLeft = date.time - now.time
        return when {
            msLeft <= 0 -> ProfileExpiry("اعتبار حساب در $dateText پایان یافت", ExpiryTone.Expired)
            msLeft <= WARNING_DAYS * DAY_MS -> ProfileExpiry("اعتبار حساب تا $dateText", ExpiryTone.Soon)
            else -> ProfileExpiry("اعتبار حساب تا $dateText", ExpiryTone.Active)
        }
    }
}
