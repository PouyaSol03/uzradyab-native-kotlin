package com.example.uzradyab.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.uzradyab.data.local.entity.EventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM events ORDER BY eventTime DESC LIMIT :limit")
    fun observeRecentEvents(limit: Int): Flow<List<EventEntity>>

    @Upsert
    suspend fun upsertAll(events: List<EventEntity>)

    @Query("DELETE FROM events")
    suspend fun clear()
}
