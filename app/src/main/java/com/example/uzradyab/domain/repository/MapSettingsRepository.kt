package com.example.uzradyab.domain.repository

import kotlinx.coroutines.flow.Flow

interface MapSettingsRepository {
    fun observeMapStyle(): Flow<String>
    suspend fun setMapStyle(style: String)
    fun observeLastSelectedDeviceId(): Flow<Long?>
    suspend fun setLastSelectedDeviceId(deviceId: Long)
    suspend fun getCachedLatestEvent(deviceId: Long): String?
    suspend fun setCachedLatestEvent(deviceId: Long, eventJson: String)
}
