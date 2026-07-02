package com.example.uzradyab.domain.manager

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChangelogManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("uzradyab_changelog_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val ENABLE_CHANGELOG = false
    }

    fun shouldShowChangelog(currentVersionCode: Int): Boolean {
        if (!ENABLE_CHANGELOG) return false

        val lastSeenVersion = prefs.getInt("last_seen_changelog_version", -1)

        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            // If the app was updated, lastUpdateTime will be strictly greater than firstInstallTime.
            val isUpdate = packageInfo.lastUpdateTime > packageInfo.firstInstallTime

            if (!isUpdate) {
                // It's a fresh install. We don't show the changelog.
                // But we mark this version as seen so it doesn't accidentally trigger later.
                if (lastSeenVersion < currentVersionCode) {
                    markChangelogSeen(currentVersionCode)
                }
                false
            } else {
                // It's an update! Show if we haven't seen it for this specific version yet.
                lastSeenVersion < currentVersionCode
            }
        } catch (e: Exception) {
            // Fallback in case of an error
            false
        }
    }

    fun markChangelogSeen(currentVersionCode: Int) {
        prefs.edit().putInt("last_seen_changelog_version", currentVersionCode).apply()
    }
}
