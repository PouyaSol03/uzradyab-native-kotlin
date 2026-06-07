package com.example.uzradyab.data.mapper

import com.example.uzradyab.data.remote.dto.CombinedReportDto
import com.example.uzradyab.domain.model.CombinedReportItem
import com.example.uzradyab.domain.model.Position

fun CombinedReportDto.toDomain(): CombinedReportItem = CombinedReportItem(
    deviceId = deviceId,
    route = route.map { coord ->
        Position(
            id = null,
            deviceId = deviceId,
            latitude = coord.getOrNull(1) ?: 0.0,
            longitude = coord.getOrNull(0) ?: 0.0,
            speed = 0.0,
            course = 0.0,
            fixTime = null,
            serverTime = null,
            address = null,
            attributesJson = "{}"
        )
    },
    events = events.map { it.toDomain() },
    positions = positions.map { it.toDomain() },
)
