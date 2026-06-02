package com.example.uzradyab.map.offline

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MapboxOfflineRegionManager @Inject constructor() {
    suspend fun clearOfflineMapData(): Result<Unit> {
        return runCatching { Unit }
    }
}
