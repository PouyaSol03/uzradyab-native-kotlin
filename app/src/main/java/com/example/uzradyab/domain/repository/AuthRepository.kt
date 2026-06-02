package com.example.uzradyab.domain.repository

import com.example.uzradyab.domain.model.UserSession
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentSession: Flow<UserSession?>
    suspend fun refreshSession(): Result<UserSession>
    suspend fun login(phoneNumber: String, password: String): Result<UserSession>
    suspend fun logout(): Result<Unit>
}
