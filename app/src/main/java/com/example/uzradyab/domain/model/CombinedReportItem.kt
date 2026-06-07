package com.example.uzradyab.domain.model

data class CombinedReportItem(
    val deviceId: Long,
    val route: List<Position>,
    val events: List<Event>,
    val positions: List<Position>,
)
