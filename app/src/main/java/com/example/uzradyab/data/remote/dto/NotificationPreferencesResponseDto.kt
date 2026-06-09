package com.example.uzradyab.data.remote.dto

import androidx.annotation.Keep

@Keep
data class NotificationPreferencesResponseDto(
    val message: String?,
    val preferences: Map<String, Boolean>?
)
