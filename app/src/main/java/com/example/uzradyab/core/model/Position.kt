package com.example.uzradyab.core.model

data class Position(
    val deviceId: Long,
    val latitude: Double,
    val longitude: Double,
    val speed: Double,
    val serverTime: String?,
)
