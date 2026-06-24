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
        val obj = com.google.gson.JsonParser.parseString(attributesJson).asJsonObject
        if (obj.has("currentKilometers")) obj.get("currentKilometers").asString else ""
    } catch (e: com.google.gson.JsonSyntaxException) {
        ""
    } catch (e: IllegalStateException) {
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
