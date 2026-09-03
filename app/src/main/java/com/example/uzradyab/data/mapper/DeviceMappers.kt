package com.example.uzradyab.data.mapper

import com.example.uzradyab.data.local.entity.DeviceEntity
import com.example.uzradyab.data.remote.dto.DeviceDto
import com.example.uzradyab.domain.model.Device

fun DeviceDto.toEntity(): DeviceEntity = DeviceEntity(
    id = id,
    name = name?.takeIf { it.isNotBlank() } ?: uniqueId.orEmpty(),
    uniqueId = uniqueId.orEmpty(),
    status = status ?: "unknown",
    category = category,
    disabled = disabled,
    lastUpdate = lastUpdate,
    expirationTime = expirationTime,
    attributesJson = attributes?.toString() ?: "{}",
    phone = phone,
)

fun DeviceEntity.toDomain(): Device {
    val km = try {
        val regex = "\"currentKilometers\"\\s*:\\s*\"?(\\d+(?:\\.\\d+)?)\"?".toRegex()
        regex.find(attributesJson)?.groupValues?.get(1) ?: ""
    } catch (e: Exception) {
        ""
    }

    return Device(
        id = id,
        name = name,
        uniqueId = uniqueId,
        status = status,
        category = category,
        disabled = disabled,
        lastUpdate = lastUpdate,
        expirationTime = expirationTime,
        attributesJson = attributesJson,
        phone = phone,
        currentKilometers = km
    )
}
