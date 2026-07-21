package com.example.uzradyab.domain.manager

import com.example.uzradyab.data.remote.api.TraccarApi
import com.example.uzradyab.domain.repository.AuthRepository
import com.example.uzradyab.domain.repository.TokenRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.firstOrNull
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class FcmTokenManager @Inject constructor(
    private val authRepository: AuthRepository,
    private val traccarApi: TraccarApi,
    private val tokenRepository: TokenRepository
) {
    private suspend fun getCurrentToken(): String? = suspendCancellableCoroutine { cont ->
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                android.util.Log.d("FCM_TOKEN", "Fetched FCM Token: $token")
                cont.resume(token)
            } else {
                android.util.Log.e("FCM_TOKEN", "Failed to fetch FCM Token", task.exception)
                cont.resume(null)
            }
        }
    }

    suspend fun syncCurrentToken(): Result<Unit> = runCatching {
        val token = getCurrentToken() ?: return Result.failure(Exception("Could not fetch FCM token"))
        syncToken(token).getOrThrow()
    }

    suspend fun removeCurrentToken(): Result<Unit> = runCatching {
        val token = getCurrentToken() ?: return Result.failure(Exception("Could not fetch FCM token"))
        removeToken(token).getOrThrow()
    }

    suspend fun syncToken(token: String): Result<Unit> = runCatching {
        // Send to Django backend
        tokenRepository.syncToken(token, withRetries = true).getOrThrow()
        
        val session = authRepository.currentSession.firstOrNull() ?: return Result.success(Unit)
        
        // Fetch fresh session info from server to ensure we have latest attributes
        val freshSession = traccarApi.getSession()
        
        val tokensString = freshSession.attributes["notificationTokens"] as? String ?: ""
        val tokens = if (tokensString.isNotBlank()) tokensString.split(",").toMutableList() else mutableListOf()
        
        if (!tokens.contains(token)) {
            tokens.add(token)
            // Keep only the last 3 tokens to prevent bloat
            val trimmedTokens = tokens.takeLast(3).joinToString(",")
            
            val updatedAttributes = freshSession.attributes.toMutableMap()
            updatedAttributes["notificationTokens"] = trimmedTokens
            
            val updatedDto = freshSession.copy(attributes = updatedAttributes)
            traccarApi.updateUser(freshSession.id, updatedDto)
        }
    }

    suspend fun removeToken(token: String): Result<Unit> = runCatching {
        val session = authRepository.currentSession.firstOrNull() ?: return Result.success(Unit)
        val freshSession = traccarApi.getSession()
        
        val tokensString = freshSession.attributes["notificationTokens"] as? String ?: ""
        val tokens = if (tokensString.isNotBlank()) tokensString.split(",").toMutableList() else mutableListOf()
        
        if (tokens.contains(token)) {
            tokens.remove(token)
            val updatedTokensStr = tokens.joinToString(",")
            
            val updatedAttributes = freshSession.attributes.toMutableMap()
            if (updatedTokensStr.isNotBlank()) {
                updatedAttributes["notificationTokens"] = updatedTokensStr
            } else {
                updatedAttributes.remove("notificationTokens")
            }
            
            val updatedDto = freshSession.copy(attributes = updatedAttributes)
            traccarApi.updateUser(freshSession.id, updatedDto)
        }
    }
}
