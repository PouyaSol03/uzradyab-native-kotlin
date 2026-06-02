package com.example.uzradyab.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "offline_regions")
data class OfflineRegionEntity(
    @PrimaryKey val id: String,
    val name: String,
    val minZoom: Double,
    val maxZoom: Double,
    val sizeBytes: Long,
    val state: String,
)
