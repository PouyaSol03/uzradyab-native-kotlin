package com.example.uzradyab.data.repository

import com.example.uzradyab.data.local.dao.OfflineRegionDao
import com.example.uzradyab.data.mapper.toDomain
import com.example.uzradyab.domain.model.OfflineRegion
import com.example.uzradyab.domain.repository.MapCacheRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MapCacheRepositoryImpl @Inject constructor(
    private val offlineRegionDao: OfflineRegionDao,
) : MapCacheRepository {
    override fun observeOfflineRegions(): Flow<List<OfflineRegion>> {
        return offlineRegionDao.observeRegions().map { rows -> rows.map { it.toDomain() } }
    }

    override suspend fun clearOfflineRegions(): Result<Unit> = runCatching {
        offlineRegionDao.clear()
    }
}
