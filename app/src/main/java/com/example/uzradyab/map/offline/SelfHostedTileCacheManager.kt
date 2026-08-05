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
            // MapLibre uses mbgl-cache.db in the cache directory
            val mapLibreCacheFile = File(context.cacheDir, "mbgl-cache.db")
            if (mapLibreCacheFile.exists()) {
                mapLibreCacheFile.delete()
            }
            Unit
        }
    }
}
