package com.example.uzradyab.domain.repository

import com.example.uzradyab.domain.model.OfflineRegion
import kotlinx.coroutines.flow.Flow

interface MapCacheRepository {
    fun observeOfflineRegions(): Flow<List<OfflineRegion>>
    suspend fun clearOfflineRegions(): Result<Unit>
}
