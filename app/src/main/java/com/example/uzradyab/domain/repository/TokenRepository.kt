package com.example.uzradyab.domain.repository

interface TokenRepository {
    suspend fun syncToken(token: String, withRetries: Boolean = true): Result<Unit>
}
