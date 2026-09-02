package com.example.uzradyab.core.network

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

class NetworkErrorInterceptor @Inject constructor(
    private val networkEventBus: NetworkEventBus
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        try {
            return chain.proceed(chain.request())
        } catch (e: Exception) {
            val isCanceled = e is IOException && e.message == "Canceled"
            if (!isCanceled && (e is UnknownHostException || e is ConnectException || e is SocketTimeoutException || e is IOException)) {
                networkEventBus.emitError()
            }
            if (e is SocketTimeoutException) {
                throw IOException("پاسخی از سمت سرور دریافت نشد، زمان اتصال پایان یافت (Timeout).")
            }
            throw e
        }
    }
}
