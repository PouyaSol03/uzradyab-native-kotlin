package com.example.uzradyab.domain.repository

import com.example.uzradyab.data.remote.dto.AppConfigDto
import kotlinx.coroutines.flow.StateFlow

interface AppConfigRepository {
    val currentConfig: StateFlow<AppConfigDto?>
    suspend fun getAppConfig(): Result<AppConfigDto>
}
