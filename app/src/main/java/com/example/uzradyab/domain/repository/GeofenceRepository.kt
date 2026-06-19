package com.example.uzradyab.domain.repository

import com.example.uzradyab.domain.model.Geofence

interface GeofenceRepository {
    suspend fun getGeofences(): Result<List<Geofence>> // Removed deviceId
    suspend fun createGeofence(name: String, area: String, description: String? = null): Result<Geofence>
    suspend fun updateGeofence(id: Long, name: String, area: String, description: String? = null): Result<Geofence>
    suspend fun deleteGeofence(id: Long): Result<Unit>
    // Removed linkGeofenceToDevice and unlinkGeofenceFromDevice
}
