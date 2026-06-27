package com.example.uzradyab.core.network

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UnauthorizedInterceptor @Inject constructor(
    private val sessionEventBus: SessionEventBus
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        
        if (response.code == 401) {
            val path = response.request.url.encodedPath
            if (!path.endsWith("/api/session")) {
                sessionEventBus.emitUnauthorized()
            }
        }
        
        return response
    }
}
