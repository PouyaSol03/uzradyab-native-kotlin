package com.example.uzradyab.data.repository

import android.util.Log
import com.example.uzradyab.core.network.PersistentCookieJar
import com.example.uzradyab.data.local.dao.UserSessionDao
import com.example.uzradyab.data.mapper.toDomain
import com.example.uzradyab.data.mapper.toEntity
import com.example.uzradyab.data.remote.api.NotificationApi
import com.example.uzradyab.data.remote.api.TraccarApi
import com.example.uzradyab.domain.model.UserSession
import com.example.uzradyab.domain.repository.AuthRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeoutOrNull

class AuthRepositoryImpl @Inject constructor(
    private val api: TraccarApi,
    private val notificationApi: NotificationApi,
    private val userSessionDao: UserSessionDao,
    private val cookieJar: PersistentCookieJar,
) : AuthRepository {
    override val currentSession: Flow<UserSession?> =
        userSessionDao.observeCurrentSession().map { it?.toDomain() }

    override suspend fun refreshSession(): Result<UserSession> = runCatching {
        val session = api.getSession()
        val entity = session.toEntity()
        userSessionDao.upsert(entity)
        entity.toDomain()
    }

    override suspend fun checkServerHealth(): Result<Int> = runCatching {
        val response = api.checkServerHealth()
        response.code()
    }

    override suspend fun login(phoneNumber: String, password: String): Result<UserSession> = runCatching {
        val session = api.login(phoneNumber = phoneNumber, password = password)
        val entity = session.toEntity()
        userSessionDao.upsert(entity)
        entity.toDomain()
    }

    override suspend fun logout(): Result<Unit> = runCatching {
        // Stop push notifications for this user before the session is cleared; never block logout on it.
        val session = userSessionDao.getCurrentSession()
        if (session == null) {
            Log.d(FCM_LOG_TAG, "⚠️ UNREGISTER skipped: no saved session on logout")
        } else {
            val userId = session.id.toString()
            Log.d(FCM_LOG_TAG, "🚀 UNREGISTER: Attempting to unregister FCM Token on logout... (UserId: $userId)")
            runCatching {
                withTimeoutOrNull(FCM_UNREGISTER_TIMEOUT_MS) {
                    notificationApi.unregisterFcmToken(userId)
                }
            }.onSuccess { result ->
                if (result == null) {
                    Log.e(FCM_LOG_TAG, "⏱️ UNREGISTER timed out after ${FCM_UNREGISTER_TIMEOUT_MS}ms, continuing logout (UserId: $userId)")
                } else {
                    Log.d(FCM_LOG_TAG, "✅ UNREGISTER SUCCESS: FCM Token unregistered (UserId: $userId)")
                }
            }.onFailure { e ->
                Log.e(FCM_LOG_TAG, "❌ UNREGISTER failed, continuing logout (UserId: $userId): ${e.message}")
            }
        }
        runCatching { api.logout() }
        cookieJar.clear()
        userSessionDao.clear()
    }

    override suspend fun isSessionExpired(): Boolean {
        val session = userSessionDao.getCurrentSession() ?: return false
        val ageMs = System.currentTimeMillis() - session.loginTimestamp
        if (ageMs > SESSION_EXPIRY_MS) {
            logout()
            return true
        }
        return false
    }

    companion object {
        private const val SESSION_EXPIRY_MS = 30L * 24L * 60L * 60L * 1000L // 30 days
        private const val FCM_UNREGISTER_TIMEOUT_MS = 5_000L
        // Same tag as the register logs so one Logcat filter shows the whole FCM flow
        private const val FCM_LOG_TAG = "TokenRepositoryImpl"
    }
}
