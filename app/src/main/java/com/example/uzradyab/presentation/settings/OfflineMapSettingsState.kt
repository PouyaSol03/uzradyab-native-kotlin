package com.example.uzradyab.presentation.settings

import com.example.uzradyab.domain.model.OfflineRegion

data class OfflineMapSettingsState(
    val regions: List<OfflineRegion> = emptyList(),
    val storageLimitMb: Int = 500,
    val minZoom: Double = 5.0,
    val maxZoom: Double = 15.0,
    val isClearing: Boolean = false,
    val message: String? = null,
)
