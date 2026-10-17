package com.example.uzradyab.presentation.command

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CommandCatalogTest {
    private fun command(type: String) = COMMAND_TYPES.first { it.type == type }

    @Test
    fun existingCommandsKeepTheirStrings() {
        assertEquals("STATUS#", command("status").command)
        assertEquals("SOS,A,{EMAIL}#", command("admin").command)
        assertEquals("RELAY,0#", command("start").command)
        assertEquals("RELAY,1#", command("stop").command)
        assertEquals("RESET#", command("reset").command)
        assertEquals("SERVER,1,uzradyab.ir,5023,0#", command("server").command)
        assertEquals("GMT,E,0,0#", command("time").command)
        assertEquals("LBSON#", command("lbs").command)
    }

    @Test
    fun tileOrderStartsWithStatusThenWhere() {
        assertEquals(
            listOf("status", "where", "admin", "start", "stop", "reset", "server", "time", "lbs"),
            COMMAND_TYPES.take(9).map { it.type },
        )
        assertEquals(21, COMMAND_TYPES.size)
    }

    @Test
    fun speedCommandPutsCycleBeforeLimit() {
        val text = command("speed_on").buildCommand(mapOf("kmh" to "120", "sec" to "30", "m" to "1"))
        assertEquals("SPEED,ON,30,120,1#", text)
    }

    @Test
    fun persianDigitsAreSentAsLatin() {
        val text = command("moving_on").buildCommand(mapOf("r" to "۲۰۰", "m" to "3"))
        assertEquals("MOVING,ON,200,3#", text)
    }

    @Test
    fun powerAlarmKeepsFixedValues() {
        assertEquals("POWERALM,ON,2,5,300#", command("power_on").buildCommand(mapOf("m" to "2")))
    }

    @Test
    fun sosJoinsOnlyFilledNumbers() {
        val sos = command("sos_on")
        assertEquals("SOS,A,09121234567#", sos.buildCommand(mapOf("n1" to "09121234567", "n2" to "", "n3" to "")))
        assertEquals(
            "SOS,A,09121234567,09351234567#",
            sos.buildCommand(mapOf("n1" to "09121234567", "n2" to "09351234567")),
        )
    }

    @Test
    fun numberValidationChecksRange() {
        val kmh = command("speed_on").params.first() as NumberParam
        assertTrue(CommandValidation.isValid(kmh, "255"))
        assertTrue(CommandValidation.isValid(kmh, "۱۲۰"))
        assertFalse(CommandValidation.isValid(kmh, "0"))
        assertFalse(CommandValidation.isValid(kmh, "256"))
        assertFalse(CommandValidation.isValid(kmh, "12.5"))
        assertEquals("عددی بین ۱ تا ۲۵۵ وارد کنید.", CommandValidation.errorFor(kmh, "300"))
    }

    @Test
    fun emptyFieldsNeverShowErrors() {
        command("sos_on").params.forEach { assertNull(CommandValidation.errorFor(it, "")) }
        command("speed_on").params.forEach { assertNull(CommandValidation.errorFor(it, "")) }
    }

    @Test
    fun phoneValidation() {
        val (n1, n2) = command("sos_on").params.map { it as PhoneParam }
        assertFalse(CommandValidation.isValid(n1, ""))
        assertTrue(CommandValidation.isValid(n2, ""))
        assertTrue(CommandValidation.isValid(n1, "+989121234567"))
        assertFalse(CommandValidation.isValid(n1, "0912"))
        assertEquals(CommandValidation.PHONE_ERROR, CommandValidation.errorFor(n1, "abc"))
    }

    @Test
    fun notifyMustBeChosen() {
        val speed = command("speed_on")
        assertFalse(CommandValidation.allValid(speed.params, mapOf("kmh" to "120", "sec" to "30")))
        assertFalse(CommandValidation.allValid(speed.params, mapOf("kmh" to "120", "sec" to "30", "m" to "2")))
        assertTrue(CommandValidation.allValid(speed.params, mapOf("kmh" to "120", "sec" to "30", "m" to "1")))
    }

    @Test
    fun placeholderUsesPersianDigits() {
        assertEquals("(۵ تا ۶۰۰)", (command("speed_on").params[1] as NumberParam).placeholder)
    }
}
