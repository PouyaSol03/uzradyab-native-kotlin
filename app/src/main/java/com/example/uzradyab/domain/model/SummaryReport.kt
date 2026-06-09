package com.example.uzradyab.domain.model

data class SummaryReport(
    val deviceId: Long,
    val deviceName: String?,
    val distance: Double,
    val averageSpeed: Double,
    val maxSpeed: Double,
    val spentFuel: Double,
    val startOdometer: Double,
    val endOdometer: Double
)