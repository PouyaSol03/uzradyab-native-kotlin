package com.example.uzradyab.data.remote.dto

import com.google.gson.JsonObject

data class AddDeviceRequestDto(
    val name: String,
    val uniqueId: String,
    val phone: String,
    val category: String = "default",
    val expirationTime: String,
    val attributes: JsonObject
)
