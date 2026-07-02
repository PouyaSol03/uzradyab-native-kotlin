package com.example.uzradyab.domain.repository

import com.example.uzradyab.data.remote.dto.AppConfigDto

interface AppConfigRepository {
    suspend fun getAppConfig(): Result<AppConfigDto>
}
