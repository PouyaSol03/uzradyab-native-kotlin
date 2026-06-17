package com.example.uzradyab.data.remote.dto

data class GeofenceDto(
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val area: String,
    val calendarId: Long = 0,
    val attributes: Map<String, Any> = emptyMap()
)

data class PermissionDto(
    val deviceId: Long = 0,
    val geofenceId: Long = 0,
    val groupId: Long = 0,
    val userId: Long = 0
)
