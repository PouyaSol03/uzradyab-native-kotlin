package com.example.uzradyab.domain.repository

import kotlinx.coroutines.flow.Flow

interface MapSettingsRepository {
    fun observeMapStyle(): Flow<String>
    suspend fun setMapStyle(style: String)
}
