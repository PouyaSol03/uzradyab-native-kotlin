package com.example.uzradyab.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.uzradyab.domain.repository.MapSettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class DefaultMapSettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : MapSettingsRepository {
    private val prefs: SharedPreferences = context.getSharedPreferences("map_settings_prefs", Context.MODE_PRIVATE)

    override fun observeMapStyle(): Flow<String> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == KEY_MAP_STYLE) {
                trySend(sharedPreferences.getString(KEY_MAP_STYLE, DEFAULT_STYLE) ?: DEFAULT_STYLE)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }.onStart {
        emit(prefs.getString(KEY_MAP_STYLE, DEFAULT_STYLE) ?: DEFAULT_STYLE)
    }.conflate()

    override suspend fun setMapStyle(style: String) {
        prefs.edit().putString(KEY_MAP_STYLE, style).apply()
    }

    override fun observeLastSelectedDeviceId(): Flow<Long?> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == KEY_LAST_DEVICE_ID) {
                val value = sharedPreferences.getLong(KEY_LAST_DEVICE_ID, -1L)
                trySend(if (value != -1L) value else null)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }.onStart {
        val value = prefs.getLong(KEY_LAST_DEVICE_ID, -1L)
        emit(if (value != -1L) value else null)
    }.conflate()

    override suspend fun setLastSelectedDeviceId(deviceId: Long) {
        prefs.edit().putLong(KEY_LAST_DEVICE_ID, deviceId).apply()
    }

    override suspend fun getCachedLatestEvent(deviceId: Long): String? {
        return prefs.getString("cached_event_$deviceId", null)
    }

    override suspend fun setCachedLatestEvent(deviceId: Long, eventJson: String) {
        prefs.edit().putString("cached_event_$deviceId", eventJson).apply()
    }

    companion object {
        private const val KEY_MAP_STYLE = "map_style"
        private const val KEY_LAST_DEVICE_ID = "last_selected_device_id"
        private const val DEFAULT_STYLE = "carto" // Can be osm, googleRoad, googleSatellite, carto
    }
}
