package com.example.uzradyab.domain.repository

interface NotificationRepository {
    suspend fun getPreferences(userId: Long): Result<Map<String, Boolean>>
    suspend fun togglePreference(userId: Long, key: String): Result<Unit>
}
