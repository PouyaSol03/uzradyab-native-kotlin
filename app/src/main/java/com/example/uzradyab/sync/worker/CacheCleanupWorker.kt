package com.example.uzradyab.sync.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.uzradyab.domain.repository.PositionRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class CacheCleanupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val positionRepository: PositionRepository,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return positionRepository.pruneHistory(maxRowsPerDevice = 1_000).fold(
            onSuccess = { Result.success() },
            onFailure = { Result.retry() },
        )
    }
}
