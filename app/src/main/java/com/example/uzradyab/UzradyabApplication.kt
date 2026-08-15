package com.example.uzradyab

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import org.maplibre.android.MapLibre
import org.maplibre.android.offline.OfflineManager
import javax.inject.Inject

@HiltAndroidApp
class UzradyabApplication : Application(), Configuration.Provider {
    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        com.example.uzradyab.core.debug.LocalCrashReporter.init(this)
        
        // Initialize MapLibre globally
        MapLibre.getInstance(this)
        
        // Set ambient cache for offline maps to 100MB
        // MapLibre automatically uses LRU (Least Recently Used) to remove older tiles when the limit is reached
        OfflineManager.getInstance(this).setMaximumAmbientCacheSize(
            100 * 1024 * 1024L,
            object : OfflineManager.FileSourceCallback {
                override fun onSuccess() {}
                override fun onError(message: String) {}
            }
        )
    }
}
