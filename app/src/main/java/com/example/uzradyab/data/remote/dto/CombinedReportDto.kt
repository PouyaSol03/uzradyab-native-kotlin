package com.example.uzradyab.data.remote.dto

data class CombinedReportDto(
    val deviceId: Long = 0,
    val route: List<List<Double>> = emptyList(),
    val events: List<EventDto> = emptyList(),
    val positions: List<PositionDto> = emptyList(),
)
