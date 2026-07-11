package com.example.uzradyab.data.local.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "user_device_cross_ref",
    primaryKeys = ["userId", "deviceId"],
    indices = [
        Index(value = ["deviceId"]),
        Index(value = ["userId"])
    ]
)
data class UserDeviceCrossRef(
    val userId: Long,
    val deviceId: Long
)
