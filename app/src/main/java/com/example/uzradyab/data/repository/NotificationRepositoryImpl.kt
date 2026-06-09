package com.example.uzradyab.data.repository

import com.example.uzradyab.data.remote.api.NotificationApi
import com.example.uzradyab.domain.repository.NotificationRepository
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val api: NotificationApi
) : NotificationRepository {

    override suspend fun getPreferences(userId: Long): Result<Map<String, Boolean>> {
        return try {
            val response = api.getPreferences(userId)
            val prefs = response.preferences ?: emptyMap()
            Result.success(prefs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun togglePreference(userId: Long, key: String): Result<Unit> {
        return try {
            api.togglePreference(userId, key)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
