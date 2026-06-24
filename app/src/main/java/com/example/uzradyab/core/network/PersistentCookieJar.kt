package com.example.uzradyab.core.network

import android.content.Context
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class PersistentCookieJar(private val context: Context) : CookieJar {
    private val preferences: SharedPreferences by lazy {
        val encryptedPrefs = try {
            getEncryptedSharedPreferences()
        } catch (e: Exception) {
            context.deleteSharedPreferences("uzradyab_cookies_secure")
            getEncryptedSharedPreferences()
        }

        // Migration from old unencrypted prefs
        val oldPrefs = context.getSharedPreferences("uzradyab_cookies", Context.MODE_PRIVATE)
        if (oldPrefs.contains(KEY_COOKIES)) {
            encryptedPrefs.edit()
                .putStringSet(KEY_COOKIES, oldPrefs.getStringSet(KEY_COOKIES, emptySet()))
                .apply()
            
            oldPrefs.edit().clear().apply()
        }

        encryptedPrefs
    }

    private fun getEncryptedSharedPreferences(): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            "uzradyab_cookies_secure",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        if (cookies.isEmpty()) return

        val urlStr = url.toString()
        // Merge with existing cookies, de-duplicate by domain|path|name, and drop expired
        val stored = (loadAllCookies() + cookies.map { urlStr to it })
            .filter { it.second.expiresAt > System.currentTimeMillis() }
            .associateBy { "${it.second.domain}|${it.second.path}|${it.second.name}" }
            .values
            .map { (originUrl, cookie) -> "$originUrl$SEPARATOR$cookie" }
            .toSet()

        preferences.edit()
            .putStringSet(KEY_COOKIES, stored)
            .apply()
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val now = System.currentTimeMillis()
        val cookies = loadAllCookies()
        val valid = cookies.filter { it.second.expiresAt > now }

        // Prune expired cookies from storage
        if (valid.size != cookies.size) {
            preferences.edit()
                .putStringSet(KEY_COOKIES, valid.map { (originUrl, cookie) -> "$originUrl$SEPARATOR$cookie" }.toSet())
                .apply()
        }

        return valid.map { it.second }.filter { it.matches(url) }
    }

    fun clear() {
        preferences.edit().remove(KEY_COOKIES).apply()
    }

    /**
     * Load all stored cookies. Each entry is stored as `originUrl||cookieString`.
     * Falls back to parsing against [DEFAULT_SERVER_URL] for legacy entries
     * that don't contain the separator.
     */
    private fun loadAllCookies(): List<Pair<String, Cookie>> {
        return preferences.getStringSet(KEY_COOKIES, emptySet()).orEmpty()
            .mapNotNull { entry ->
                val separatorIdx = entry.indexOf(SEPARATOR)
                val (originUrl, cookieStr) = if (separatorIdx > 0) {
                    entry.substring(0, separatorIdx) to entry.substring(separatorIdx + SEPARATOR.length)
                } else {
                    // Legacy format: no origin URL stored, fall back to default
                    DEFAULT_SERVER_URL to entry
                }
                
                // استفاده از تابع استاندارد و ایمنِ OkHttp برای پارس کردن URL
                val httpUrl = originUrl.toHttpUrlOrNull() ?: DEFAULT_SERVER_URL.toHttpUrlOrNull() ?: return@mapNotNull null
                Cookie.parse(httpUrl, cookieStr)?.let { originUrl to it }
            }
    }

    private companion object {
        const val KEY_COOKIES = "cookies"
        const val DEFAULT_SERVER_URL = "https://app.uzradyab.ir"
        /** Separator between origin URL and cookie string in storage. */
        const val SEPARATOR = "||"
    }
}