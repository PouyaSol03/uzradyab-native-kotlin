package com.example.uzradyab.data.remote.dto

import com.google.gson.JsonObject

data class PositionDto(
    val id: Long? = null,
    val deviceId: Long = 0,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val speed: Double = 0.0,
    val course: Double = 0.0,
    val fixTime: String? = null,
    val serverTime: String? = null,
    val address: String? = null,
    val attributes: JsonObject? = null,
)
