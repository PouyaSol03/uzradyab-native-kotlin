package com.example.uzradyab.domain.repository

import com.example.uzradyab.domain.model.Device
import kotlinx.coroutines.flow.Flow

interface DeviceRepository {
    fun observeDevices(): Flow<List<Device>>
    suspend fun refreshDevices(): Result<Unit>
}
