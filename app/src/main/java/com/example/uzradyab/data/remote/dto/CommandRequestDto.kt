package com.example.uzradyab.data.remote.dto

import androidx.annotation.Keep

@Keep
data class CommandRequestDto(
    val type: String,
    val deviceId: Long,
    val attributes: Map<String, String>
)

@Keep
data class CommandResponseDto(
    val id: Long,
    val deviceId: Long,
    val type: String,
    val textChannel: Boolean
)
