package com.example.uzradyab.domain.repository

import kotlinx.coroutines.flow.Flow

interface MapSettingsRepository {
    fun observeMapStyle(): Flow<String>
    fun getMapStyleSync(): String
    suspend fun setMapStyle(style: String)
    fun observeLastSelectedDeviceId(): Flow<Long?>
    suspend fun setLastSelectedDeviceId(deviceId: Long)
    fun observeTrackedDeviceIds(): Flow<Set<Long>>
    suspend fun addTrackedDeviceId(deviceId: Long)
    suspend fun getCachedLatestEvent(deviceId: Long): String?
    suspend fun setCachedLatestEvent(deviceId: Long, eventJson: String)
}
