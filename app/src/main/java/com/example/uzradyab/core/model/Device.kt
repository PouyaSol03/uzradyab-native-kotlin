package com.example.uzradyab.core.model

data class Device(
    val id: Long,
    val name: String,
    val uniqueId: String,
    val status: String,
    val lastUpdate: String?,
    val expirationTime: String?,
)
