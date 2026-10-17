package com.example.uzradyab.presentation.command

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CommandFieldStateTest {
    private val speed = COMMAND_TYPES.first { it.type == "speed_on" }
    private val sos = COMMAND_TYPES.first { it.type == "sos_on" }

    private fun states(command: CommandData, values: Map<String, String>) =
        CommandValidation.evaluateFields(command.params, values)

    @Test
    fun onlyFirstFieldIsUnlockedAndHighlightedAtStart() {
        val fields = states(speed, emptyMap())
        assertEquals(listOf(false, true, true), fields.map { it.locked })
        assertEquals(listOf(true, false, false), fields.map { it.highlighted })
        fields.forEach { assertNull(it.error) }
    }

    @Test
    fun validFieldUnlocksTheNextOne() {
        val fields = states(speed, mapOf("kmh" to "120"))
        assertEquals(listOf(false, false, true), fields.map { it.locked })
        assertEquals(listOf(false, true, false), fields.map { it.highlighted })
    }

    @Test
    fun invalidFieldKeepsHighlightAndShowsError() {
        val fields = states(speed, mapOf("kmh" to "300"))
        assertTrue(fields[0].highlighted)
        assertEquals("عددی بین ۱ تا ۲۵۵ وارد کنید.", fields[0].error)
        assertTrue(fields[1].locked)
    }

    @Test
    fun lockedFieldsHideErrors() {
        val fields = states(speed, mapOf("kmh" to "", "sec" to "9999"))
        assertTrue(fields[1].locked)
        assertNull(fields[1].error)
    }

    @Test
    fun phoneWaitsForPreviousPhone() {
        val fields = states(sos, mapOf("n1" to "09121234567"))
        assertEquals(listOf(false, false, true), fields.map { it.locked })
        // empty optional phone is not highlighted
        assertFalse(fields[1].highlighted)
        assertTrue(CommandValidation.allValid(sos.params, mapOf("n1" to "09121234567")))
    }

    @Test
    fun filledSecondPhoneUnlocksThird() {
        val fields = states(sos, mapOf("n1" to "09121234567", "n2" to "09351234567"))
        assertFalse(fields[2].locked)
        assertFalse(fields[2].highlighted)
    }
}
