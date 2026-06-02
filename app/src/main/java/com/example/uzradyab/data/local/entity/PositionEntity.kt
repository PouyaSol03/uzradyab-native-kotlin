package com.example.uzradyab.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "positions",
    indices = [
        Index(value = ["deviceId", "serverTime"]),
        Index(value = ["deviceId", "isLatest"]),
    ],
)
data class PositionEntity(
    @PrimaryKey val localId: String,
    val remoteId: Long?,
    val deviceId: Long,
    val latitude: Double,
    val longitude: Double,
    val speed: Double,
    val course: Double,
    val fixTime: String?,
    val serverTime: String?,
    val address: String?,
    val attributesJson: String,
    val isLatest: Boolean,
)
