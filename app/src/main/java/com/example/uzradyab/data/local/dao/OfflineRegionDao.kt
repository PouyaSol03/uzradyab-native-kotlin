package com.example.uzradyab.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.uzradyab.data.local.entity.OfflineRegionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OfflineRegionDao {
    @Query("SELECT * FROM offline_regions ORDER BY name")
    fun observeRegions(): Flow<List<OfflineRegionEntity>>

    @Upsert
    suspend fun upsert(region: OfflineRegionEntity)

    @Query("DELETE FROM offline_regions")
    suspend fun clear()
}
