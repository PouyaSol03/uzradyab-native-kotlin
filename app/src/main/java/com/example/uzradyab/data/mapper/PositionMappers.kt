package com.example.uzradyab.data.mapper

import com.example.uzradyab.data.local.entity.PositionEntity
import com.example.uzradyab.data.remote.dto.PositionDto
import com.example.uzradyab.domain.model.Position

fun PositionDto.toEntity(isLatest: Boolean): PositionEntity = PositionEntity(
    localId = id?.toString() ?: "${deviceId}_${serverTime ?: fixTime ?: latitude}_$longitude",
    remoteId = id,
    deviceId = deviceId,
    latitude = latitude,
    longitude = longitude,
    speed = speed,
    course = course,
    fixTime = fixTime,
    serverTime = serverTime,
    address = address,
    attributesJson = attributes?.toString() ?: "{}",
    isLatest = isLatest,
)

fun PositionEntity.toDomain(): Position = Position(
    id = remoteId,
    deviceId = deviceId,
    latitude = latitude,
    longitude = longitude,
    speed = speed,
    course = course,
    fixTime = fixTime,
    serverTime = serverTime,
    address = address,
    attributesJson = attributesJson,
)
