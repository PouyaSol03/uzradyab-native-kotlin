package com.example.uzradyab.data.remote.dto

data class MaintenanceDto(
    val id: Long = 0,
    val name: String,
    val type: String = "totalDistance",
    val start: Double = 0.0,
    val period: Double = 0.0,
    val attributes: Map<String, Any>? = null
)
