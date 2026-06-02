package com.example.uzradyab.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "daily_distance",
    primaryKeys = ["deviceId", "date"],
)
data class DailyDistanceEntity(
    val deviceId: Long,
    val date: String,
    val distanceMeters: Double,
    val updatedAt: Long,
)
