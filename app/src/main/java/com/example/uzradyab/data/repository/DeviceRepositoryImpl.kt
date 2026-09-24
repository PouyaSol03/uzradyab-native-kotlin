package com.example.uzradyab.data.repository

import com.example.uzradyab.data.local.dao.DeviceDao
import com.example.uzradyab.data.local.dao.UserSessionDao
import com.example.uzradyab.data.local.entity.UserDeviceCrossRef
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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class DeviceRepositoryImpl @Inject constructor(
    private val api: TraccarApi,
    private val deviceDao: DeviceDao,
    private val userSessionDao: UserSessionDao,
) : DeviceRepository {

    private val pagingMutex = Mutex()
    private var currentOffset = 0
    private var hasMoreDevices = true
    private var isFetchingMore = false

    override fun observeDevices(): Flow<List<Device>> {
        return userSessionDao.observeCurrentSession().flatMapLatest { session ->
            if (session == null) flowOf(emptyList())
            else deviceDao.observeDevices(session.id).map { entities -> entities.map { it.toDomain() } }
        }
    }

    override suspend fun refreshDevices(limit: Int, offset: Int): Result<Unit> = runCatching {
        pagingMutex.withLock {
            val session = userSessionDao.getCurrentSession() ?: return@runCatching
            val devices = api.getDevices(limit = limit, offset = offset)
            val entities = devices.map { it.toEntity() }
            deviceDao.upsertAll(entities)
            
            val crossRefs = devices.map { UserDeviceCrossRef(session.id, it.id) }
            deviceDao.upsertUserDeviceCrossRefs(crossRefs)
            
            if (offset == 0) {
                currentOffset = devices.size
                hasMoreDevices = devices.size >= limit
                deviceDao.deleteOldUserDeviceCrossRefs(session.id, devices.map { it.id })
            } else {
                currentOffset += devices.size
                hasMoreDevices = devices.size >= limit
            }
        }
    }

    override suspend fun loadMoreDevices(limit: Int): Result<Int> = runCatching {
        pagingMutex.withLock {
            android.util.Log.d("DevicePaging", "loadMoreDevices called: hasMore=$hasMoreDevices, isFetchingMore=$isFetchingMore, offset=$currentOffset")
            if (!hasMoreDevices || isFetchingMore) return@runCatching 0
            isFetchingMore = true
            try {
                val session = userSessionDao.getCurrentSession() ?: return@runCatching 0
                val devices = api.getDevices(limit = limit, offset = currentOffset)
                android.util.Log.d("DevicePaging", "loadMoreDevices received ${devices.size} items at offset $currentOffset")
                if (devices.isEmpty()) {
                    hasMoreDevices = false
                    return@runCatching 0
                }
                
                val entities = devices.map { it.toEntity() }
                deviceDao.upsertAll(entities)
                
                val crossRefs = devices.map { UserDeviceCrossRef(session.id, it.id) }
                deviceDao.upsertUserDeviceCrossRefs(crossRefs)
                
                currentOffset += devices.size
                if (devices.size < limit) {
                    hasMoreDevices = false
                }
                devices.size
            } catch (e: Exception) {
                android.util.Log.e("DevicePaging", "loadMoreDevices failed", e)
                throw e
            } finally {
                isFetchingMore = false
            }
        }
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
        
        val session = userSessionDao.getCurrentSession()
        if (session != null) {
            deviceDao.upsertUserDeviceCrossRefs(listOf(UserDeviceCrossRef(session.id, newDeviceDto.id)))
        }
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
        // Fetch original device from server to avoid losing fields like expirationTime
        val remoteResponse = api.getDeviceRaw(id)
        if (!remoteResponse.isSuccessful) {
            throw Exception("Failed to fetch device from server: ${remoteResponse.code()}")
        }
        val remoteArray = remoteResponse.body()
        if (remoteArray == null || remoteArray.size() == 0) {
            throw Exception("Device not found on server")
        }
        
        val deviceJson = remoteArray.get(0).asJsonObject
        android.util.Log.d("DeviceUpdate", "Original device from server before update: $deviceJson")

        // Update fields. We only overwrite the specific fields from UI
        deviceJson.addProperty("name", name)
        deviceJson.addProperty("uniqueId", uniqueId)
        deviceJson.addProperty("phone", phone)

        // Preserve other existing attributes in the JSON
        val attributes = if (deviceJson.has("attributes") && !deviceJson.get("attributes").isJsonNull) {
            deviceJson.get("attributes").asJsonObject
        } else {
            JsonObject()
        }

        if (currentKilometers != null) {
            attributes.addProperty("currentKilometers", currentKilometers)
        }
        deviceJson.add("attributes", attributes)

        android.util.Log.d("DeviceUpdate", "Sending update device request body: $deviceJson")

        val updateResponse = api.updateDeviceRaw(id, deviceJson)
        if (!updateResponse.isSuccessful) {
            val errorBody = updateResponse.errorBody()?.string()
            android.util.Log.e("DeviceUpdate", "Update failed: code=${updateResponse.code()} body=$errorBody")
            throw Exception("Failed to update device: ${updateResponse.code()}")
        }
        val limitToRefresh = pagingMutex.withLock { currentOffset.coerceAtLeast(30) }
        refreshDevices(limit = limitToRefresh, offset = 0)
    }
}
