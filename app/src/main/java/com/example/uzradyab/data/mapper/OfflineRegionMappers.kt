package com.example.uzradyab.data.mapper

import com.example.uzradyab.data.local.entity.OfflineRegionEntity
import com.example.uzradyab.domain.model.OfflineRegion

fun OfflineRegionEntity.toDomain(): OfflineRegion = OfflineRegion(
    id = id,
    name = name,
    minZoom = minZoom,
    maxZoom = maxZoom,
    sizeBytes = sizeBytes,
    state = state,
)
