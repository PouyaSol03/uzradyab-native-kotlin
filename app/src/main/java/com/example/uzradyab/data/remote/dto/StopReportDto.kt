package com.example.uzradyab.data.remote.dto

import com.google.gson.annotations.SerializedName

data class StopReportDto(
    @SerializedName("deviceId") val deviceId: Long,
    @SerializedName("deviceName") val deviceName: String?,
    @SerializedName("positionId") val positionId: Long,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("startTime") val startTime: String,
    @SerializedName("endTime") val endTime: String,
    @SerializedName("address") val address: String?,
    @SerializedName("duration") val duration: Long,
    @SerializedName("engineHours") val engineHours: Long,
    @SerializedName("spentFuel") val spentFuel: Double
)
