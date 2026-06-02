package com.example.uzradyab.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val id: Long,
    val deviceId: Long?,
    val type: String,
    val eventTime: String?,
    val attributesJson: String,
)
