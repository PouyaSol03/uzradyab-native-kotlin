package com.example.uzradyab.data.repository

import com.example.uzradyab.data.local.dao.DeviceDao
import com.example.uzradyab.data.mapper.toDomain
import com.example.uzradyab.data.mapper.toEntity
import com.example.uzradyab.data.remote.api.TraccarApi
import com.example.uzradyab.domain.model.Device
import com.example.uzradyab.domain.repository.DeviceRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DeviceRepositoryImpl @Inject constructor(
    private val api: TraccarApi,
    private val deviceDao: DeviceDao,
) : DeviceRepository {
    override fun observeDevices(): Flow<List<Device>> {
        return deviceDao.observeDevices().map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun refreshDevices(): Result<Unit> = runCatching {
        deviceDao.upsertAll(api.getDevices().map { it.toEntity() })
    }
}
