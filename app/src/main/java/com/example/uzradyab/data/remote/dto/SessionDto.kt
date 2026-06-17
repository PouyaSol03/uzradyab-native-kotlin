package com.example.uzradyab.data.remote.dto

data class SessionDto(
    val id: Long = 0,
    val name: String? = null,
    val email: String? = null,
    val login: String? = null,
    val readonly: Boolean = false,
    val administrator: Boolean = false,
    val map: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val zoom: Int? = null,
    val coordinateFormat: String? = null,
    val disabled: Boolean = false,
    val expirationTime: String? = null,
    val deviceLimit: Int = -1,
    val userLimit: Int = 0,
    val deviceReadonly: Boolean = false,
    val limitCommands: Boolean = false,
    val disableReports: Boolean = false,
    val fixedEmail: Boolean = false,
    val poiLayer: String? = null,
    val phone: String? = null,
    val password: String? = null,
    val attributes: Map<String, Any> = emptyMap()
)
