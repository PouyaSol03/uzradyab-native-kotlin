package com.example.uzradyab.core.utils

import com.example.uzradyab.domain.model.Device

/**
 * Shared device search matching used by every device list.
 *
 * Persian/Arabic digits and the Arabic forms of ی and ک are normalized so a query
 * typed with a Persian keyboard still matches serials, phones and names.
 */
object DeviceSearch {

    fun normalize(value: String): String = buildString(value.length) {
        value.forEach { char ->
            append(
                when (char) {
                    in '۰'..'۹' -> '0' + (char - '۰')
                    in '٠'..'٩' -> '0' + (char - '٠')
                    'ي', 'ى' -> 'ی'
                    'ك' -> 'ک'
                    else -> char
                }
            )
        }
    }.trim().lowercase()

    fun matches(device: Device, query: String): Boolean {
        val normalizedQuery = normalize(query)
        if (normalizedQuery.isEmpty()) return true
        return listOfNotNull(device.name, device.uniqueId, device.phone, device.status)
            .any { normalize(it).contains(normalizedQuery) }
    }

    /** Local matches first, then server results that are not loaded locally yet. */
    fun merge(local: List<Device>, remote: List<Device>, query: String): List<Device> {
        val localMatches = local.filter { matches(it, query) }
        val localIds = localMatches.mapTo(HashSet()) { it.id }
        return localMatches + remote.filter { it.id !in localIds && matches(it, query) }
    }
}
