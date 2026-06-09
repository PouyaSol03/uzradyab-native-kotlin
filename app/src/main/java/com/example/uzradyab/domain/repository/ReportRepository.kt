package com.example.uzradyab.domain.repository

import com.example.uzradyab.domain.model.DailyDistance
import com.example.uzradyab.domain.model.CombinedReportItem
import com.example.uzradyab.domain.model.SummaryReport
import kotlinx.coroutines.flow.Flow

interface ReportRepository {
    fun observeDailyDistance(deviceId: Long, date: String): Flow<DailyDistance?>

    suspend fun refreshDailyDistance(
        deviceId: Long,
        date: String,
        from: String,
        to: String,
    ): Result<Unit>

    suspend fun getCombinedReport(
        deviceIds: List<Long>,
        from: String,
        to: String,
    ): Result<List<CombinedReportItem>>

    // متد جدید برای دریافت خلاصه گزارش (Summary)
    suspend fun getSummaryReport(
        deviceId: Long,
        from: String,
        to: String,
    ): Result<List<SummaryReport>>
}

