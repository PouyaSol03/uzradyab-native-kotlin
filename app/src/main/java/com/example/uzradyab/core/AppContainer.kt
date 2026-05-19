package com.example.uzradyab.core

import android.content.Context
import com.example.uzradyab.core.data.SessionRepository
import com.example.uzradyab.core.network.PersistentCookieJar
import com.example.uzradyab.core.network.TraccarApiClient
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object AppContainer {
    @Volatile
    private var repository: SessionRepository? = null

    fun sessionRepository(context: Context): SessionRepository {
        return repository ?: synchronized(this) {
            repository ?: buildRepository(context.applicationContext).also { repository = it }
        }
    }

    private fun buildRepository(context: Context): SessionRepository {
        val cookieJar = PersistentCookieJar(context)
        val okHttpClient = OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        return SessionRepository(
            apiClient = TraccarApiClient(
                client = okHttpClient,
                cookieJar = cookieJar,
            )
        )
    }
}
