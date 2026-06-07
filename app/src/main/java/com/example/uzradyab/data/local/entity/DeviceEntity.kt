package com.example.uzradyab.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "devices")
data class DeviceEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val uniqueId: String,
    val status: String,
    val category: String?,
    val disabled: Boolean,
    val lastUpdate: String?,
    val expirationTime: String?,
    val attributesJson: String,
    val phone: String?,
)

