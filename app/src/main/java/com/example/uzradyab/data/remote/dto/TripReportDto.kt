package com.example.uzradyab.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class TripReportDto(
    val deviceId: Long,
    val deviceName: String? = null,
    val distance: Double = 0.0,
    val averageSpeed: Double = 0.0,
    val maxSpeed: Double = 0.0,
    val spentFuel: Double = 0.0,
    val startOdometer: Double = 0.0,
    val endOdometer: Double = 0.0,
    val startTime: String? = null,
    val endTime: String? = null,
    val startPositionId: Long = 0,
    val endPositionId: Long = 0,
    val startLat: Double = 0.0,
    val startLon: Double = 0.0,
    val endLat: Double = 0.0,
    val endLon: Double = 0.0,
    val startAddress: String? = null,
    val endAddress: String? = null,
    val duration: Long = 0,
    val driverUniqueId: String? = null,
    val driverName: String? = null
)
