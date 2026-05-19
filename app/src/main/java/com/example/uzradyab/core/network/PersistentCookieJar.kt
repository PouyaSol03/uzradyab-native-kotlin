package com.example.uzradyab.core.network

import android.content.Context
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

class PersistentCookieJar(context: Context) : CookieJar {
    private val preferences = context.getSharedPreferences("uzradyab_cookies", Context.MODE_PRIVATE)

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        if (cookies.isEmpty()) return

        val stored = (loadAllCookies() + cookies)
            .filter { it.expiresAt > System.currentTimeMillis() }
            .associateBy { "${it.domain}|${it.path}|${it.name}" }
            .values
            .map(Cookie::toString)
            .toSet()

        preferences.edit()
            .putStringSet(KEY_COOKIES, stored)
            .apply()
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val now = System.currentTimeMillis()
        val cookies = loadAllCookies()
        val valid = cookies.filter { it.expiresAt > now }

        if (valid.size != cookies.size) {
            preferences.edit()
                .putStringSet(KEY_COOKIES, valid.map(Cookie::toString).toSet())
                .apply()
        }

        return valid.filter { it.matches(url) }
    }

    fun clear() {
        preferences.edit().remove(KEY_COOKIES).apply()
    }

    private fun loadAllCookies(): List<Cookie> {
        return preferences.getStringSet(KEY_COOKIES, emptySet()).orEmpty()
            .mapNotNull { Cookie.parse(NetworkConfig.BASE_URL.toHttpUrl(), it) }
    }

    private fun String.toHttpUrl(): HttpUrl = HttpUrl.Builder()
        .scheme(substringBefore("://"))
        .host(substringAfter("://").substringBefore("/"))
        .build()

    private companion object {
        const val KEY_COOKIES = "cookies"
    }
}
