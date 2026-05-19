package com.example.uzradyab.feature.home

import com.example.uzradyab.core.model.AppUser
import com.example.uzradyab.core.model.Device
import com.example.uzradyab.core.model.Position

data class HomeUiState(
    val user: AppUser? = null,
    val devices: List<Device> = emptyList(),
    val positions: Map<Long, Position> = emptyMap(),
    val selectedDeviceId: Long? = null,
    val isLoading: Boolean = true,
    val isSigningOut: Boolean = false,
    val errorMessage: String? = null,
    val signedOut: Boolean = false,
)
