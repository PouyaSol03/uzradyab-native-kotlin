package com.example.uzradyab.data.remote.dto

data class SummaryReportDto(
    val deviceId: Long = 0,
    val distance: Double = 0.0,
    val averageSpeed: Double = 0.0,
    val maxSpeed: Double = 0.0,
    val spentFuel: Double = 0.0,
    val startOdometer: Double = 0.0,
    val endOdometer: Double = 0.0,
)
