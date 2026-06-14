package com.example.uzradyab

import org.maplibre.android.offline.OfflineManager
import android.content.Context

fun checkCacheMethod(context: Context) {
    OfflineManager.getInstance(context).setMaximumAmbientCacheSize(
        50L * 1024 * 1024,
        object : OfflineManager.FileSourceCallback {
            override fun onSuccess() {}
            override fun onError(message: String) {}
        }
    )
}
