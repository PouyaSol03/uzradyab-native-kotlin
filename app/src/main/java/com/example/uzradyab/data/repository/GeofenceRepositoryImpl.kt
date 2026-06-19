package com.example.uzradyab.data.repository

import com.example.uzradyab.data.remote.api.TraccarApi
import com.example.uzradyab.data.remote.dto.GeofenceDto
import com.example.uzradyab.data.remote.dto.PermissionDto
import com.example.uzradyab.domain.model.Geofence
import com.example.uzradyab.domain.model.GeofenceShape
import com.example.uzradyab.domain.repository.GeofenceRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeofenceRepositoryImpl @Inject constructor(
    private val api: TraccarApi
) : GeofenceRepository {

    override suspend fun getGeofences(): Result<List<Geofence>> { // Removed deviceId
        return try {
            val dtos = api.getGeofences() // Calls API without query param
            Result.success(dtos.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createGeofence(name: String, area: String, description: String?): Result<Geofence> {
        return try {
            val dto = GeofenceDto(
                name = name,
                area = area,
                description = description
            )
            val result = api.createGeofence(dto)
            Result.success(result.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateGeofence(id: Long, name: String, area: String, description: String?): Result<Geofence> {
        return try {
            val dto = GeofenceDto(
                id = id,
                name = name,
                area = area,
                description = description
            )
            val result = api.updateGeofence(id, dto)
            Result.success(result.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteGeofence(id: Long): Result<Unit> {
        return try {
            api.deleteGeofence(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun GeofenceDto.toDomain(): Geofence {
        val shape = Geofence.parseArea(this.area)
        val isCircle = shape is GeofenceShape.Circle
        return Geofence(
            id = this.id,
            name = this.name,
            description = this.description,
            area = this.area,
            centerLat = if (shape is GeofenceShape.Circle) shape.lat else null,
            centerLon = if (shape is GeofenceShape.Circle) shape.lon else null,
            radius = if (shape is GeofenceShape.Circle) shape.radius else null,
            isCircle = isCircle
        )
    }
}
