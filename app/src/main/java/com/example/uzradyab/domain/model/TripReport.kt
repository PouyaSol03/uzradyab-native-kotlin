package com.example.uzradyab.domain.model

data class TripReport(
    val deviceId: Long,
    val deviceName: String?,
    val distance: Double,
    val averageSpeed: Double,
    val maxSpeed: Double,
    val spentFuel: Double,
    val startOdometer: Double,
    val endOdometer: Double,
    val startTime: String?,
    val endTime: String?,
    val startPositionId: Long,
    val endPositionId: Long,
    val startLat: Double,
    val startLon: Double,
    val endLat: Double,
    val endLon: Double,
    val startAddress: String?,
    val endAddress: String?,
    val duration: Long,
    val driverUniqueId: String?,
    val driverName: String?
)
