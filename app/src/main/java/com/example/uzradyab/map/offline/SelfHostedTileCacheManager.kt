package com.example.uzradyab.map.offline

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import java.io.File

@Singleton
class SelfHostedTileCacheManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    suspend fun clearOfflineMapData(): Result<Unit> {
        return runCatching {
            kotlin.coroutines.suspendCoroutine<Unit> { continuation ->
                org.maplibre.android.offline.OfflineManager.getInstance(context)
                    .clearAmbientCache(object : org.maplibre.android.offline.OfflineManager.FileSourceCallback {
                        override fun onSuccess() {
                            continuation.resumeWith(Result.success(Unit))
                        }
                        override fun onError(message: String) {
                            continuation.resumeWith(Result.failure(Exception(message)))
                        }
                    })
            }
        }
    }
}
