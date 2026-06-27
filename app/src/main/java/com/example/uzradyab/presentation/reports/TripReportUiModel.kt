package com.example.uzradyab.presentation.reports

import androidx.compose.runtime.Immutable

@Immutable
data class TripReportUiModel(
    val startPositionId: Long,
    val startTime: String,
    val endTime: String,
    val distance: String,
    val averageSpeed: String,
    val maxSpeed: String,
    val duration: String,
    val spentFuel: String,
    val startAddress: String?,
    val endAddress: String?,
    val startLat: Double,
    val startLon: Double,
    val endLat: Double,
    val endLon: Double
)
