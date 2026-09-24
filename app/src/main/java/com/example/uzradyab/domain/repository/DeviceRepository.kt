package com.example.uzradyab.domain.repository

import com.example.uzradyab.domain.model.Device
import kotlinx.coroutines.flow.Flow

interface DeviceRepository {
    fun observeDevices(): Flow<List<Device>>
    suspend fun refreshDevices(limit: Int = 30, offset: Int = 0): Result<Unit>
    suspend fun loadMoreDevices(limit: Int = 30): Result<Int>
    suspend fun addDevice(
        name: String,
        uniqueId: String,
        phone: String,
        currentKilometers: Double?
    ): Result<Unit>
    suspend fun getDevice(deviceId: Long): Device?
    suspend fun updateDevice(
        id: Long,
        name: String,
        uniqueId: String,
        phone: String,
        currentKilometers: Double?
    ): Result<Unit>
}
