package com.example.uzradyab.data.repository

import com.example.uzradyab.core.debug.AppLogger
import com.example.uzradyab.core.debug.LogLevel
import com.example.uzradyab.data.local.dao.DailyDistanceDao
import com.example.uzradyab.data.mapper.toDomain
import com.example.uzradyab.data.mapper.toEntity
import com.example.uzradyab.data.remote.api.TraccarApi
import com.example.uzradyab.domain.model.CombinedReportItem
import com.example.uzradyab.domain.model.DailyDistance
import com.example.uzradyab.domain.model.SummaryReport
import com.example.uzradyab.domain.repository.ReportRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ReportRepositoryImpl @Inject constructor(
    private val api: TraccarApi,
    private val dailyDistanceDao: DailyDistanceDao,
) : ReportRepository {

    override fun observeDailyDistance(deviceId: Long, date: String): Flow<DailyDistance?> {
        return dailyDistanceDao.observeDailyDistance(deviceId, date)
            .map { row -> row?.toDomain() }
    }

    override suspend fun refreshDailyDistance(
        deviceId: Long,
        date: String,
        from: String,
        to: String,
    ): Result<Unit> = runCatching {
        AppLogger.log(LogLevel.REQUEST, "Report", "refreshDailyDistance for $deviceId: $from to $to")
        val summary = api.getSummaryReport(
            from = from,
            to = to,
            daily = false,
            deviceId = deviceId,
        ).firstOrNull()
        AppLogger.log(LogLevel.RESPONSE, "Report", "refreshDailyDistance success")

        dailyDistanceDao.upsert(
            DailyDistance(
                deviceId = deviceId,
                date = date,
                distanceMeters = summary?.distance ?: 0.0,
                updatedAt = System.currentTimeMillis(),
            ).toEntity(),
        )
    }.onFailure {
        AppLogger.log(LogLevel.ERROR, "Report", "refreshDailyDistance failed: ${it.message}")
    }

    override suspend fun getCombinedReport(
        deviceIds: List<Long>,
        from: String,
        to: String,
    ): Result<List<CombinedReportItem>> = runCatching {
        AppLogger.log(LogLevel.REQUEST, "Report", "getCombinedReport for ${deviceIds.size} devices")
        val result = api.getCombinedReport(
            from = from,
            to = to,
            deviceIds = deviceIds
        ).map { it.toDomain() }
        AppLogger.log(LogLevel.RESPONSE, "Report", "getCombinedReport received ${result.size} items")
        result
    }.onFailure {
        AppLogger.log(LogLevel.ERROR, "Report", "getCombinedReport failed: ${it.message}")
    }

    override suspend fun getSummaryReport(
        deviceId: Long,
        from: String,
        to: String
    ): Result<List<SummaryReport>> = runCatching {
        AppLogger.log(LogLevel.REQUEST, "Report", "getSummaryReport for $deviceId: $from to $to")
        val result = api.getSummaryReport(
            deviceId = deviceId,
            from = from,
            to = to,
            daily = false
        ).map { it.toDomain() }
        AppLogger.log(LogLevel.RESPONSE, "Report", "getSummaryReport received ${result.size} items")
        result
    }.onFailure {
        AppLogger.log(LogLevel.ERROR, "Report", "getSummaryReport failed: ${it.message}")
    }

    override suspend fun getStopsReport(
        deviceId: Long,
        from: String,
        to: String
    ): Result<List<com.example.uzradyab.domain.model.StopReport>> = runCatching {
        AppLogger.log(LogLevel.REQUEST, "Report", "getStopsReport for $deviceId: $from to $to")
        val result = api.getStopsReport(
            deviceId = deviceId,
            from = from,
            to = to
        ).map { it.toDomain() }
        AppLogger.log(LogLevel.RESPONSE, "Report", "getStopsReport received ${result.size} items")
        result
    }.onFailure {
        AppLogger.log(LogLevel.ERROR, "Report", "getStopsReport failed: ${it.message}")
    }
}
