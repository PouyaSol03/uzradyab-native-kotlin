package com.example.uzradyab.data.remote

import android.util.Log
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.uzradyab.domain.repository.TokenRepository
import com.example.uzradyab.sync.worker.SyncFcmTokenWorker
import com.google.firebase.messaging.FirebaseMessagingService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class UzradyabFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var tokenRepository: TokenRepository

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("UzradyabFCMService", "New token received: $token")

        serviceScope.launch {
            // First tier and second tier: direct attempt with 3 exponential backoff retries
            val result = tokenRepository.syncToken(token, withRetries = true)
            
            if (result.isFailure) {
                Log.d("UzradyabFCMService", "Token sync failed after retries, enqueueing WorkManager job")
                scheduleTokenSyncWork(token)
            }
        }
    }

    private fun scheduleTokenSyncWork(token: String) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<SyncFcmTokenWorker>()
            .setConstraints(constraints)
            .setInputData(workDataOf(SyncFcmTokenWorker.KEY_TOKEN to token))
            .build()

        WorkManager.getInstance(applicationContext).enqueue(workRequest)
    }
}
