package com.example.uzradyab.domain.repository

import com.example.uzradyab.domain.model.DailyDistance
import kotlinx.coroutines.flow.Flow

interface ReportRepository {
    fun observeDailyDistance(deviceId: Long, date: String): Flow<DailyDistance?>

    suspend fun refreshDailyDistance(
        deviceId: Long,
        date: String,
        from: String,
        to: String,
    ): Result<Unit>
}
