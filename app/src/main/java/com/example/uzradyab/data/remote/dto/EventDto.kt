package com.example.uzradyab.data.remote.dto

import com.google.gson.JsonObject

data class EventDto(
    val id: Long = 0,
    val deviceId: Long? = null,
    val type: String = "",
    val eventTime: String? = null,
    val attributes: JsonObject? = null,
)
