package com.example.uzradyab.presentation.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RegisterValidationTest {
    @Test
    fun passwordRuleStateRequiresLengthDigitAndSpecialCharacter() {
        val incomplete = passwordRuleState("abcdefg1")
        assertTrue(incomplete.hasMinimumLength)
        assertTrue(incomplete.hasDigit)
        assertFalse(incomplete.hasSpecialCharacter)
        assertFalse(incomplete.isValid)

        val complete = passwordRuleState("abcdefg1!")
        assertTrue(complete.hasMinimumLength)
        assertTrue(complete.hasDigit)
        assertTrue(complete.hasSpecialCharacter)
        assertTrue(complete.isValid)
    }

    @Test
    fun isValidIranPhoneNumberRequiresElevenDigitsStartingWith09() {
        assertTrue(isValidIranPhoneNumber("09156131179"))
        assertFalse(isValidIranPhoneNumber("9156131179"))
        assertFalse(isValidIranPhoneNumber("08156131179"))
        assertFalse(isValidIranPhoneNumber("09156131abc"))
    }

    @Test
    fun maskPhoneNumberKeepsPrefixAndSuffix() {
        assertEquals("0915xxxxx79", maskPhoneNumber("09156131179"))
        assertEquals("123", maskPhoneNumber("123"))
    }
}
