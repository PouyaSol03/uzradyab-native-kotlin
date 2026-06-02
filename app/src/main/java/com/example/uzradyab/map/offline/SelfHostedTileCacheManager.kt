package com.example.uzradyab.map.offline

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import org.osmdroid.config.Configuration

@Singleton
class SelfHostedTileCacheManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    suspend fun clearOfflineMapData(): Result<Unit> {
        return runCatching {
            val prefs = context.getSharedPreferences(OSMDROID_PREFS, Context.MODE_PRIVATE)
            Configuration.getInstance().load(context, prefs)
            Configuration.getInstance().osmdroidTileCache?.deleteRecursively()
            Unit
        }
    }

    private companion object {
        const val OSMDROID_PREFS = "osmdroid"
    }
}
