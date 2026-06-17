package com.example.uzradyab.data.repository

import android.util.Log
import com.example.uzradyab.domain.repository.TokenRepository
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenRepositoryImpl @Inject constructor() : TokenRepository {

    override suspend fun syncToken(token: String, withRetries: Boolean): Result<Unit> {
        val maxRetries = if (withRetries) 3 else 0
        var currentDelay = 2000L

        for (attempt in 0..maxRetries) {
            try {
                // Mock API Call: Replace with actual Traccar API client call
                val success = mockApiCall(token)
                
                if (success) {
                    Log.d("TokenRepositoryImpl", "Token synced successfully on attempt ${attempt + 1}")
                    return Result.success(Unit)
                } else {
                    throw Exception("Mock API returned failure")
                }
            } catch (e: Exception) {
                Log.e("TokenRepositoryImpl", "Failed to sync token on attempt ${attempt + 1}: ${e.message}")
                if (attempt == maxRetries) {
                    return Result.failure(e)
                }
                delay(currentDelay)
                currentDelay *= 2 // Exponential backoff (2s, 4s, 8s)
            }
        }
        return Result.failure(Exception("Max retries reached without success"))
    }

    private suspend fun mockApiCall(token: String): Boolean {
        // Simulate network delay
        delay(500)
        // Simulate a failure to trigger backoff for testing or just fail randomly
        // For production, this should be the actual API request
        return false // Return false to simulate network failure and test the fallback mechanism
    }
}
