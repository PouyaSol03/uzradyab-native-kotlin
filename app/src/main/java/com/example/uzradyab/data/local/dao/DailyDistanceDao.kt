package com.example.uzradyab.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.uzradyab.data.local.entity.DailyDistanceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyDistanceDao {
    @Query("SELECT * FROM daily_distance WHERE deviceId = :deviceId AND date = :date LIMIT 1")
    fun observeDailyDistance(deviceId: Long, date: String): Flow<DailyDistanceEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: DailyDistanceEntity)

    @Query("DELETE FROM daily_distance WHERE date < :minimumDate")
    suspend fun deleteOlderThan(minimumDate: String)
}
