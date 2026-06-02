package com.example.uzradyab.domain.model

data class OfflineRegion(
    val id: String,
    val name: String,
    val minZoom: Double,
    val maxZoom: Double,
    val sizeBytes: Long,
    val state: String,
)
