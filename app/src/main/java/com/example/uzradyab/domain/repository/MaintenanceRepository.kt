package com.example.uzradyab.domain.repository

import com.example.uzradyab.domain.model.Maintenance

interface MaintenanceRepository {
    suspend fun getMaintenances(deviceId: Long? = null): Result<List<Maintenance>>
    suspend fun createMaintenance(deviceId: Long, name: String, periodKm: Double, startKm: Double): Result<Maintenance>
    suspend fun updateMaintenance(id: Long, name: String, periodKm: Double, startKm: Double): Result<Maintenance>
    suspend fun resetMaintenance(id: Long, currentKm: Double, maintenance: Maintenance): Result<Maintenance>
    suspend fun deleteMaintenance(id: Long): Result<Unit>
}
