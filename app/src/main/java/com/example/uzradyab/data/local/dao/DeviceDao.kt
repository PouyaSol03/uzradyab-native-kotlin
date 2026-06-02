package com.example.uzradyab.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.uzradyab.data.local.entity.DeviceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceDao {
    @Query("SELECT * FROM devices ORDER BY name COLLATE NOCASE")
    fun observeDevices(): Flow<List<DeviceEntity>>

    @Upsert
    suspend fun upsertAll(devices: List<DeviceEntity>)

    @Query("DELETE FROM devices")
    suspend fun clear()
}
