package com.example.uzradyab.domain.model

data class Device(
    val id: Long,
    val name: String,
    val uniqueId: String,
    val status: String,
    val category: String?,
    val disabled: Boolean,
    val lastUpdate: String?,
    val expirationTime: String?,
    val attributesJson: String,
    val phone: String?,
)

