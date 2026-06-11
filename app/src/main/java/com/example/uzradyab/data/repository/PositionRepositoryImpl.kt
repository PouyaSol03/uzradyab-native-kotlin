package com.example.uzradyab.data.repository

import com.example.uzradyab.data.local.dao.PositionDao
import com.example.uzradyab.data.mapper.toDomain
import com.example.uzradyab.data.mapper.toEntity
import com.example.uzradyab.data.remote.api.TraccarApi
import com.example.uzradyab.domain.model.Position
import com.example.uzradyab.domain.repository.PositionRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PositionRepositoryImpl @Inject constructor(
    private val api: TraccarApi,
    private val positionDao: PositionDao,
) : PositionRepository {
    override fun observeLatestPositions(): Flow<Map<Long, Position>> {
        return positionDao.observeLatestPositions()
            .map { rows -> rows.associate { it.deviceId to it.toDomain() } }
    }

    override fun observeHistory(deviceId: Long, limit: Int): Flow<List<Position>> {
        return positionDao.observeHistory(deviceId, limit)
            .map { rows -> rows.map { it.toDomain() } }
    }

    override suspend fun getLatestPosition(deviceId: Long): Position? {
        return positionDao.getLatestPosition(deviceId)?.toDomain()
    }

    override suspend fun refreshLatestPositions(): Result<Unit> = runCatching {
        positionDao.upsertLatest(api.getPositions().map { it.toEntity(isLatest = true) })
        pruneHistory(maxRowsPerDevice = 1_000).getOrThrow()
    }

    override suspend fun pruneHistory(maxRowsPerDevice: Int): Result<Unit> = runCatching {
        positionDao.deviceIdsWithHistory().forEach { deviceId ->
            positionDao.pruneDeviceHistory(deviceId = deviceId, maxRows = maxRowsPerDevice)
        }
    }
}
