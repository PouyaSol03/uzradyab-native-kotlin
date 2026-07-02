package com.example.uzradyab.data.repository

import com.example.uzradyab.data.remote.api.TraccarApi
import com.example.uzradyab.data.remote.dto.AppConfigDto
import com.example.uzradyab.domain.repository.AppConfigRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppConfigRepositoryImpl @Inject constructor(
    private val traccarApi: TraccarApi
) : AppConfigRepository {

    private val _currentConfig = MutableStateFlow<AppConfigDto?>(null)
    override val currentConfig: StateFlow<AppConfigDto?> = _currentConfig.asStateFlow()

    override suspend fun getAppConfig(): Result<AppConfigDto> {
        return try {
            val result = traccarApi.getAppConfig()
            _currentConfig.value = result
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
