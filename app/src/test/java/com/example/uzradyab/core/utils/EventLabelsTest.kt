package com.example.uzradyab.core.utils

import org.junit.Assert.assertEquals
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
}
