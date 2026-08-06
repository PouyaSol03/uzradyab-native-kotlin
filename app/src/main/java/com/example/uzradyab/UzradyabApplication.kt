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
        
        // Increase ambient cache for offline maps to 250MB for better road trip caching
        OfflineManager.getInstance(this).setMaximumAmbientCacheSize(
            250 * 1024 * 1024L,
            object : OfflineManager.FileSourceCallback {
                override fun onSuccess() {}
                override fun onError(message: String) {}
            }
        )
    }
}
