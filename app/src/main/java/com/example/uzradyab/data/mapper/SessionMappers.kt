package com.example.uzradyab.data.mapper

import com.example.uzradyab.data.local.entity.UserSessionEntity
import com.example.uzradyab.data.remote.dto.SessionDto
import com.example.uzradyab.domain.model.UserSession

fun SessionDto.toEntity(): UserSessionEntity = UserSessionEntity(
    id = id,
    name = name?.takeIf { it.isNotBlank() } ?: email.orEmpty(),
    email = email.orEmpty(),
    readonly = readonly,
)

fun UserSessionEntity.toDomain(): UserSession = UserSession(
    id = id,
    name = name,
    email = email,
    readonly = readonly,
)
