package com.example.uzradyab.domain.model

data class StopReport(
    val deviceId: Long,
    val deviceName: String?,
    val positionId: Long,
    val latitude: Double,
    val longitude: Double,
    val startTime: String,
    val endTime: String,
    val address: String?,
    val duration: Long,
    val engineHours: Long,
    val spentFuel: Double
)
