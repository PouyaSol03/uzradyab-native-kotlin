package com.example.uzradyab.data.repository

import com.example.uzradyab.core.network.PersistentCookieJar
import com.example.uzradyab.data.local.dao.UserSessionDao
import com.example.uzradyab.data.mapper.toDomain
import com.example.uzradyab.data.mapper.toEntity
import com.example.uzradyab.data.remote.api.TraccarApi
import com.example.uzradyab.domain.model.UserSession
import com.example.uzradyab.domain.repository.AuthRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthRepositoryImpl @Inject constructor(
    private val api: TraccarApi,
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

    override suspend fun login(phoneNumber: String, password: String): Result<UserSession> = runCatching {
        val session = api.login(phoneNumber = phoneNumber, password = password)
        val entity = session.toEntity()
        userSessionDao.upsert(entity)
        entity.toDomain()
    }

    override suspend fun logout(): Result<Unit> = runCatching {
        runCatching { api.logout() }
        cookieJar.clear()
        userSessionDao.clear()
    }
}
