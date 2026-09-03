package com.example.uzradyab.data.repository

import com.example.uzradyab.data.remote.api.TraccarApi
import com.example.uzradyab.data.remote.dto.MaintenanceDto
import com.example.uzradyab.data.remote.dto.PermissionDto
import com.example.uzradyab.domain.model.Maintenance
import com.example.uzradyab.domain.repository.MaintenanceRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MaintenanceRepositoryImpl @Inject constructor(
    private val api: TraccarApi
) : MaintenanceRepository {

    override suspend fun getMaintenances(deviceId: Long?): Result<List<Maintenance>> {
        return try {
            val dtos = api.getMaintenances(deviceId)
            Result.success(dtos.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createMaintenance(
        deviceId: Long,
        name: String,
        periodKm: Double,
        startKm: Double
    ): Result<Maintenance> {
        return try {
            val dto = MaintenanceDto(
                id = 0,
                name = name,
                type = "totalDistance",
                start = startKm * 1000.0,
                period = periodKm * 1000.0
            )
            val created = api.createMaintenance(dto)
            // Link newly created maintenance to the device
            try {
                api.linkPermission(
                    PermissionDto(
                        deviceId = deviceId,
                        maintenanceId = created.id
                    )
                )
            } catch (linkError: Exception) {
                // If permission linking throws an error, still return created item
            }
            Result.success(created.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateMaintenance(
        id: Long,
        name: String,
        periodKm: Double,
        startKm: Double
    ): Result<Maintenance> {
        return try {
            val dto = MaintenanceDto(
                id = id,
                name = name,
                type = "totalDistance",
                start = startKm * 1000.0,
                period = periodKm * 1000.0
            )
            val updated = api.updateMaintenance(id, dto)
            Result.success(updated.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun resetMaintenance(
        id: Long,
        currentKm: Double,
        maintenance: Maintenance
    ): Result<Maintenance> {
        return try {
            val dto = MaintenanceDto(
                id = id,
                name = maintenance.name,
                type = maintenance.type,
                start = currentKm * 1000.0,
                period = maintenance.periodMeters,
                attributes = maintenance.attributes
            )
            val updated = api.updateMaintenance(id, dto)
            Result.success(updated.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteMaintenance(id: Long): Result<Unit> {
        return try {
            api.deleteMaintenance(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun MaintenanceDto.toDomain(): Maintenance {
        val (normStartMeters, normPeriodMeters) = if (period >= 100_000.0) {
            Pair(start, period)
        } else {
            Pair(start * 1000.0, period * 1000.0)
        }
        return Maintenance(
            id = id,
            name = name,
            type = type,
            startMeters = normStartMeters,
            periodMeters = normPeriodMeters,
            attributes = attributes ?: emptyMap()
        )
    }
}
