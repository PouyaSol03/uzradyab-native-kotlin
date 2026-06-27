package com.example.uzradyab.presentation.reports

import androidx.compose.runtime.Immutable

@Immutable
data class StopReportUiModel(
    val deviceId: Long,
    val positionId: Long,
    val latitude: Double,
    val longitude: Double,
    val startTime: String,
    val endTime: String,
    val address: String?,
    val duration: String,
    val engineHours: String,
    val spentFuel: String
)
