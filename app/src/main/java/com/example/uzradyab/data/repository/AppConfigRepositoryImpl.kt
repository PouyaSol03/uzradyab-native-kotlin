package com.example.uzradyab.data.repository

import com.example.uzradyab.data.remote.api.TraccarApi
import com.example.uzradyab.data.remote.dto.AppConfigDto
import com.example.uzradyab.domain.repository.AppConfigRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppConfigRepositoryImpl @Inject constructor(
    private val traccarApi: TraccarApi
) : AppConfigRepository {

    override suspend fun getAppConfig(): Result<AppConfigDto> {
        return try {
            val result = traccarApi.getAppConfig()
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
