package com.example.uzradyab.core.utils

import com.example.uzradyab.domain.model.Device
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DeviceSearchTest {
    private fun device(id: Long, name: String, uniqueId: String = "", phone: String? = null) = Device(
        id = id,
        name = name,
        uniqueId = uniqueId,
        status = "online",
        category = null,
        disabled = false,
        lastUpdate = null,
        expirationTime = null,
        attributesJson = "{}",
        phone = phone,
    )

    @Test
    fun persianDigitsMatchSerialAndPhone() {
        val car = device(1, "پژو", uniqueId = "86012345", phone = "09121234567")
        assertTrue(DeviceSearch.matches(car, "۸۶۰۱"))
        assertTrue(DeviceSearch.matches(car, "۰۹۱۲"))
    }

    @Test
    fun arabicLettersMatchPersianName() {
        assertTrue(DeviceSearch.matches(device(1, "پیکان"), "پيكان"))
    }

    @Test
    fun blankQueryMatchesEverything() {
        assertTrue(DeviceSearch.matches(device(1, "any"), "  "))
        assertFalse(DeviceSearch.matches(device(1, "any"), "other"))
    }

    @Test
    fun mergeKeepsLocalFirstAndAddsUnloadedServerResults() {
        val local = listOf(device(1, "car one"), device(2, "truck"))
        val remote = listOf(device(1, "car one"), device(3, "car two"), device(4, "bus"))
        val merged = DeviceSearch.merge(local, remote, "car")
        assertEquals(listOf(1L, 3L), merged.map { it.id })
    }
}
