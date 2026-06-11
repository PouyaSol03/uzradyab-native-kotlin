package com.example.uzradyab.domain.repository

import com.example.uzradyab.domain.model.Position
import kotlinx.coroutines.flow.Flow

interface PositionRepository {
    fun observeLatestPositions(): Flow<Map<Long, Position>>
    fun observeHistory(deviceId: Long, limit: Int): Flow<List<Position>>
    suspend fun getLatestPosition(deviceId: Long): Position?
    suspend fun refreshLatestPositions(): Result<Unit>
    suspend fun pruneHistory(maxRowsPerDevice: Int): Result<Unit>
}
