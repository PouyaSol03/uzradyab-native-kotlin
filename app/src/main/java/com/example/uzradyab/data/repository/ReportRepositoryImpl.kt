package com.example.uzradyab.data.repository

import com.example.uzradyab.data.local.dao.DailyDistanceDao
import com.example.uzradyab.data.mapper.toDomain
import com.example.uzradyab.data.mapper.toEntity
import com.example.uzradyab.data.remote.api.TraccarApi
import com.example.uzradyab.domain.model.DailyDistance
import com.example.uzradyab.domain.repository.ReportRepository
import com.example.uzradyab.domain.model.CombinedReportItem
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

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
        val summary = api.getSummaryReport(
            from = from,
            to = to,
            daily = false,
            deviceId = deviceId,
        ).firstOrNull()
        dailyDistanceDao.upsert(
            DailyDistance(
                deviceId = deviceId,
                date = date,
                distanceMeters = summary?.distance ?: 0.0,
                updatedAt = System.currentTimeMillis(),
            ).toEntity(),
        )
    }

    override suspend fun getCombinedReport(
        deviceIds: List<Long>,
        from: String,
        to: String,
    ): Result<List<CombinedReportItem>> = runCatching {
        api.getCombinedReport(
            from = from,
            to = to,
            deviceIds = deviceIds
        ).map { it.toDomain() }
    }
}

