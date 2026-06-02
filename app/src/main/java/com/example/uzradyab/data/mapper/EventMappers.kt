package com.example.uzradyab.data.mapper

import com.example.uzradyab.data.local.entity.EventEntity
import com.example.uzradyab.data.remote.dto.EventDto
import com.example.uzradyab.domain.model.Event

fun EventDto.toEntity(): EventEntity = EventEntity(
    id = id,
    deviceId = deviceId,
    type = type,
    eventTime = eventTime,
    attributesJson = attributes?.toString() ?: "{}",
)

fun EventEntity.toDomain(): Event = Event(
    id = id,
    deviceId = deviceId,
    type = type,
    eventTime = eventTime,
    attributesJson = attributesJson,
)
