package com.example.uzradyab.domain.model

data class Event(
    val id: Long,
    val deviceId: Long?,
    val type: String,
    val eventTime: String?,
    val attributesJson: String,
)
