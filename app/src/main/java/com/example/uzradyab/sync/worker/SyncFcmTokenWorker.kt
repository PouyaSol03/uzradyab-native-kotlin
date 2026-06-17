package com.example.uzradyab.sync.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.uzradyab.domain.repository.TokenRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncFcmTokenWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val tokenRepository: TokenRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val token = inputData.getString(KEY_TOKEN) ?: return Result.failure()

        // Call syncToken without internal retries, since WorkManager handles backoff
        val result = tokenRepository.syncToken(token, withRetries = false)
        
        return if (result.isSuccess) {
            Result.success()
        } else {
            Result.retry()
        }
    }

    companion object {
        const val KEY_TOKEN = "fcm_token"
    }
}
