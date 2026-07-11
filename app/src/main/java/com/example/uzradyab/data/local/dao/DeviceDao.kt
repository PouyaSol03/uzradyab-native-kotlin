package com.example.uzradyab.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.uzradyab.data.local.entity.DeviceEntity
import com.example.uzradyab.data.local.entity.UserDeviceCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceDao {
    @Query("SELECT devices.* FROM devices INNER JOIN user_device_cross_ref ON devices.id = user_device_cross_ref.deviceId WHERE user_device_cross_ref.userId = :userId ORDER BY name COLLATE NOCASE")
    fun observeDevices(userId: Long): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM devices WHERE id = :id")
    suspend fun getDeviceById(id: Long): DeviceEntity?

    @Upsert
    suspend fun upsertUserDeviceCrossRefs(refs: List<UserDeviceCrossRef>)

    @Query("DELETE FROM user_device_cross_ref WHERE userId = :userId AND deviceId NOT IN (:validDeviceIds)")
    suspend fun deleteOldUserDeviceCrossRefs(userId: Long, validDeviceIds: List<Long>)

    @Upsert
    suspend fun upsertAll(devices: List<DeviceEntity>)

    @Query("DELETE FROM devices")
    suspend fun clear()
}
