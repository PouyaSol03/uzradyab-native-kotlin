package com.example.uzradyab.core.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EventLabelsTest {
    @Test
    fun nonAlarmEventUsesEventLabel() {
        assertEquals("سویچ روشن", EventLabels.titleFromAttributesJson("ignitionOn", "{\"alarm\":\"sos\"}"))
    }

    @Test
    fun alarmEventAppendsAlarmSubtype() {
        assertEquals("هشدار باطری کم", EventLabels.titleFromAttributesJson("alarm", "{\"alarm\":\"lowBattery\"}"))
        assertEquals("هشدار قطع ولتاژ", EventLabels.titleFromAttributesJson("alarm", "{\"alarm\":\"powerCut\"}"))
    }

    @Test
    fun alarmEventWithSeveralAlarmsShowsCount() {
        assertEquals("هشدار (2)", EventLabels.titleFromAttributesJson("alarm", "{\"alarm\":\"sos,vibration\"}"))
    }

    @Test
    fun alarmEventWithoutSubtypeFallsBackToPlainAlarm() {
        assertEquals("هشدار", EventLabels.titleFromAttributesJson("alarm", "{}"))
        assertEquals("هشدار", EventLabels.titleFromAttributesJson("alarm", "not json"))
    }

    @Test
    fun unknownValuesFallBackToRawKey() {
        assertEquals("هشدار customAlarm", EventLabels.titleFromAttributesJson("alarm", "{\"alarm\":\"customAlarm\"}"))
        assertEquals("customEvent", EventLabels.eventType("customEvent"))
        assertEquals("رویداد جدید", EventLabels.eventType(""))
    }

    @Test
    fun commandResultReadsResultAttribute() {
        assertEquals(
            "Cut off the fuel supply: Success!",
            EventLabels.commandResultFromAttributesJson("{\"result\":\" Cut off the fuel supply: Success! \"}"),
        )
    }

    @Test
    fun commandResultIsNullWhenMissingOrBlank() {
        assertNull(EventLabels.commandResultFromAttributesJson("{}"))
        assertNull(EventLabels.commandResultFromAttributesJson("{\"result\":\"  \"}"))
        assertNull(EventLabels.commandResultFromAttributesJson("not json"))
        assertNull(EventLabels.commandResultFromAttributesJson(null))
    }
}
