package com.example.uzradyab.domain.model

data class DailyDistance(
    val deviceId: Long,
    val date: String,
    val distanceMeters: Double,
    val updatedAt: Long,
)
