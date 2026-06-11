package com.example.uzradyab.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.uzradyab.data.local.entity.PositionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PositionDao {
    @Query("SELECT * FROM positions WHERE isLatest = 1")
    fun observeLatestPositions(): Flow<List<PositionEntity>>

    @Query("SELECT * FROM positions WHERE deviceId = :deviceId ORDER BY serverTime DESC LIMIT :limit")
    fun observeHistory(deviceId: Long, limit: Int): Flow<List<PositionEntity>>

    @Query("SELECT * FROM positions WHERE deviceId = :deviceId AND isLatest = 1 LIMIT 1")
    suspend fun getLatestPosition(deviceId: Long): PositionEntity?

    @Query("UPDATE positions SET isLatest = 0 WHERE deviceId IN (:deviceIds)")
    suspend fun clearLatestFlags(deviceIds: List<Long>)

    @Upsert
    suspend fun upsertAll(positions: List<PositionEntity>)

    @Transaction
    suspend fun upsertLatest(positions: List<PositionEntity>) {
        val deviceIds = positions.map { it.deviceId }.distinct()
        if (deviceIds.isNotEmpty()) {
            clearLatestFlags(deviceIds)
        }
        upsertAll(positions.map { it.copy(isLatest = true) })
    }

    @Query(
        """
        DELETE FROM positions
        WHERE localId IN (
            SELECT localId FROM positions
            WHERE deviceId = :deviceId AND isLatest = 0
            ORDER BY serverTime DESC
            LIMIT -1 OFFSET :maxRows
        )
        """
    )
    suspend fun pruneDeviceHistory(deviceId: Long, maxRows: Int)

    @Query("SELECT DISTINCT deviceId FROM positions")
    suspend fun deviceIdsWithHistory(): List<Long>
}
