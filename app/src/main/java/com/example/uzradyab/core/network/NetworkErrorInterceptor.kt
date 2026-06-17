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
            if (e is UnknownHostException || e is ConnectException || e is SocketTimeoutException || e is IOException) {
                networkEventBus.emitError()
            }
            throw e
        }
    }
}
