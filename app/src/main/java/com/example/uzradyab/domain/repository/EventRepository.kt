package com.example.uzradyab.domain.repository

import com.example.uzradyab.domain.model.Event
import com.example.uzradyab.domain.model.LatestNotificationEvent
import kotlinx.coroutines.flow.Flow

interface EventRepository {
    fun observeRecentEvents(limit: Int): Flow<List<Event>>
    suspend fun fetchLatestDeviceEvents(deviceId: Long): Result<List<LatestNotificationEvent>>
    suspend fun getEventsReport(deviceId: Long, from: String, to: String): Result<List<Event>>
}
