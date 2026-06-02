package com.example.uzradyab.domain.model

data class Position(
    val id: Long?,
    val deviceId: Long,
    val latitude: Double,
    val longitude: Double,
    val speed: Double,
    val course: Double,
    val fixTime: String?,
    val serverTime: String?,
    val address: String?,
    val attributesJson: String,
)
