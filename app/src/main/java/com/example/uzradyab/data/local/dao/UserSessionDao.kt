package com.example.uzradyab.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.uzradyab.data.local.entity.UserSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserSessionDao {
    @Query("SELECT * FROM user_session WHERE singletonId = 1")
    fun observeCurrentSession(): Flow<UserSessionEntity?>

    @Query("SELECT * FROM user_session WHERE singletonId = 1")
    suspend fun getCurrentSession(): UserSessionEntity?

    @Upsert
    suspend fun upsert(session: UserSessionEntity)

    @Query("DELETE FROM user_session")
    suspend fun clear()
}
