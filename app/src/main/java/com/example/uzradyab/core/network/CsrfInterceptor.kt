package com.example.uzradyab.core.network

import okhttp3.Interceptor
import okhttp3.Response

class CsrfInterceptor(private val cookieJar: PersistentCookieJar) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Only POST/PUT/DELETE requests usually need CSRF token
        if (originalRequest.method == "GET" || originalRequest.method == "HEAD" || originalRequest.method == "OPTIONS") {
            return chain.proceed(originalRequest)
        }

        // Get the cookies for the request URL to find csrftoken
        val url = originalRequest.url
        val cookies = cookieJar.loadForRequest(url)
        var csrfToken: String? = null
        
        for (cookie in cookies) {
            if (cookie.name == "csrftoken") {
                csrfToken = cookie.value
                break
            }
        }

        return if (csrfToken != null) {
            val requestBuilder = originalRequest.newBuilder()
                .header("X-CSRFToken", csrfToken)
            chain.proceed(requestBuilder.build())
        } else {
            chain.proceed(originalRequest)
        }
    }
}
