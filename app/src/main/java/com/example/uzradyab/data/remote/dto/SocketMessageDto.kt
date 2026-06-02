package com.example.uzradyab.data.remote.dto

data class SocketMessageDto(
    val devices: List<DeviceDto>? = null,
    val positions: List<PositionDto>? = null,
    val events: List<EventDto>? = null,
)
