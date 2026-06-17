package com.example.uzradyab.domain.repository

import com.example.uzradyab.domain.model.Geofence

interface GeofenceRepository {
    suspend fun getGeofences(deviceId: Long? = null): Result<List<Geofence>>
    suspend fun createGeofence(name: String, area: String, description: String? = null): Result<Geofence>
    suspend fun updateGeofence(id: Long, name: String, area: String, description: String? = null): Result<Geofence>
    suspend fun deleteGeofence(id: Long): Result<Unit>
    suspend fun linkGeofenceToDevice(deviceId: Long, geofenceId: Long): Result<Unit>
    suspend fun unlinkGeofenceFromDevice(deviceId: Long, geofenceId: Long): Result<Unit>
}
