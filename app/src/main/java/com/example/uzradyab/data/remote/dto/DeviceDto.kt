package com.example.uzradyab.data.remote.dto

import com.google.gson.JsonObject

data class DeviceDto(
    val id: Long = 0,
    val name: String? = null,
    val uniqueId: String? = null,
    val status: String? = null,
    val category: String? = null,
    val disabled: Boolean = false,
    val lastUpdate: String? = null,
    val expirationTime: String? = null,
    val attributes: JsonObject? = null,
)
