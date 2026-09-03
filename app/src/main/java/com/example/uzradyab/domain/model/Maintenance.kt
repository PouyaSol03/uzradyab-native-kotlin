package com.example.uzradyab.domain.model

import kotlin.math.abs
import kotlin.math.roundToLong

enum class MaintenanceStatusLevel {
    Normal,
    Warning,
    Overdue
}

data class Maintenance(
    val id: Long,
    val name: String,
    val type: String = "totalDistance",
    val startMeters: Double,
    val periodMeters: Double,
    val attributes: Map<String, Any> = emptyMap()
) {
    val startKm: Double
        get() = startMeters / 1000.0

    val periodKm: Double
        get() = periodMeters / 1000.0

    fun distanceTraveledKm(currentOdometerKm: Double): Double {
        return maxOf(0.0, currentOdometerKm - startKm)
    }

    fun remainingKm(currentOdometerKm: Double): Double {
        return periodKm - distanceTraveledKm(currentOdometerKm)
    }

    fun progress(currentOdometerKm: Double): Float {
        if (periodKm <= 0.0) return 0f
        return (distanceTraveledKm(currentOdometerKm) / periodKm).coerceIn(0.0, 1.0).toFloat()
    }

    fun isOverdue(currentOdometerKm: Double): Boolean {
        return remainingKm(currentOdometerKm) <= 0.0
    }

    fun statusLevel(currentOdometerKm: Double): MaintenanceStatusLevel {
        val remaining = remainingKm(currentOdometerKm)
        return when {
            remaining <= 0.0 -> MaintenanceStatusLevel.Overdue
            progress(currentOdometerKm) >= 0.8f || remaining <= 1000.0 -> MaintenanceStatusLevel.Warning
            else -> MaintenanceStatusLevel.Normal
        }
    }
}
