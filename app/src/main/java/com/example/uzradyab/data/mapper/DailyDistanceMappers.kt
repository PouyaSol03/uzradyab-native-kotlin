package com.example.uzradyab.data.mapper

import com.example.uzradyab.data.local.entity.DailyDistanceEntity
import com.example.uzradyab.domain.model.DailyDistance

fun DailyDistanceEntity.toDomain(): DailyDistance {
    return DailyDistance(
        deviceId = deviceId,
        date = date,
        distanceMeters = distanceMeters,
        updatedAt = updatedAt,
    )
}

fun DailyDistance.toEntity(): DailyDistanceEntity {
    return DailyDistanceEntity(
        deviceId = deviceId,
        date = date,
        distanceMeters = distanceMeters,
        updatedAt = updatedAt,
    )
}
