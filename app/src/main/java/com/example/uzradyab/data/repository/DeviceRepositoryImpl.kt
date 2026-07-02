package com.example.uzradyab.data.repository

import com.example.uzradyab.data.local.dao.DeviceDao
import com.example.uzradyab.data.mapper.toDomain
import com.example.uzradyab.data.mapper.toEntity
import com.example.uzradyab.data.remote.api.TraccarApi
import com.example.uzradyab.domain.model.Device
import com.example.uzradyab.domain.repository.DeviceRepository
import com.example.uzradyab.data.remote.dto.AddDeviceRequestDto
import com.google.gson.JsonObject
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

    override suspend fun addDevice(
        name: String,
        uniqueId: String,
        phone: String,
        currentKilometers: Double?
    ): Result<Unit> = runCatching {
        val cal = java.util.Calendar.getInstance().apply {
            add(java.util.Calendar.DAY_OF_YEAR, 365)
        }
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
        }
        val expirationTime = sdf.format(cal.time).replace("Z", "+00:00")

        val attributes = JsonObject().apply {
            if (currentKilometers != null) {
                addProperty("currentKilometers", currentKilometers)
            }
        }

        val request = AddDeviceRequestDto(
            name = name,
            uniqueId = uniqueId,
            phone = phone,
            expirationTime = expirationTime,
            attributes = attributes
        )

        val newDeviceDto = api.addDevice(request)
        deviceDao.upsertAll(listOf(newDeviceDto.toEntity()))
    }

    override suspend fun getDevice(deviceId: Long): Device? {
        return deviceDao.getDeviceById(deviceId)?.toDomain()
    }

    override suspend fun updateDevice(
        id: Long,
        name: String,
        uniqueId: String,
        phone: String,
        currentKilometers: Double?
    ): Result<Unit> = runCatching {
        val existingDevice = deviceDao.getDeviceById(id)
        val category = existingDevice?.category ?: "default"

        val attributes = JsonObject().apply {
            if (currentKilometers != null) {
                addProperty("currentKilometers", currentKilometers)
            }
        }

        // Build a partial object containing only what we want to send
        val partialRequest = JsonObject().apply {
            addProperty("id", id)
            addProperty("name", name)
            addProperty("uniqueId", uniqueId)
            addProperty("phone", phone)
            addProperty("category", category)
            add("attributes", attributes)
        }

        android.util.Log.d("DeviceUpdate", "Sending partial update device request: $partialRequest")

        val updateResponse = api.updateDeviceRaw(id, partialRequest)
        if (!updateResponse.isSuccessful) {
            throw Exception("Failed to update device: ${updateResponse.code()}")
        }

        // Fetch fresh data from server to reflect changes
        val devices = api.getDevices()
        deviceDao.upsertAll(devices.map { it.toEntity() })
    }
}
