package com.example.uzradyab.core.debug

import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.internal.http.promisesBody
import okio.Buffer
import java.io.IOException
import java.nio.charset.Charset

/**
 * OkHttp interceptor that captures every request and response into [AppLogger].
 * Logs:
 *  - Method + URL + request body snippet
 *  - Status code + URL + duration + response body snippet
 *  - Any IO exceptions as ERROR entries
 */
class NetworkLogInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.toString()
        val method = request.method
        val shortUrl = shortenUrl(url)

        // ── Log request ───────────────────────────────────────────────────────
        val reqBodySnippet = runCatching {
            request.body?.let { body ->
                val buffer = Buffer()
                body.writeTo(buffer)
                buffer.readString(Charset.forName("UTF-8")).take(300)
            }
        }.getOrNull()

        AppLogger.log(
            level = LogLevel.REQUEST,
            tag = "$method $shortUrl",
            message = buildString {
                append("→ $method $url")
                if (!reqBodySnippet.isNullOrBlank()) {
                    append("\nBody: ${reqBodySnippet.trimIndent()}")
                }
                val headers = filterHeaders(request.headers)
                if (headers.isNotEmpty()) append("\nHeaders: $headers")
            },
        )

        // ── Execute request ───────────────────────────────────────────────────
        val startNs = System.nanoTime()
        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: IOException) {
            AppLogger.log(
                level = LogLevel.ERROR,
                tag = "$method $shortUrl",
                message = "✗ FAILED: ${e.javaClass.simpleName}: ${e.message}",
            )
            throw e
        }

        val durationMs = (System.nanoTime() - startNs) / 1_000_000L
        val code = response.code

        // ── Log response ──────────────────────────────────────────────────────
        val respBodySnippet = runCatching {
            val source = response.body?.source() ?: return@runCatching null
            source.request(Long.MAX_VALUE)
            val buffer = source.buffer.clone()
            buffer.readString(Charset.forName("UTF-8")).take(500)
        }.getOrNull()

        AppLogger.log(
            level = if (code in 200..299) LogLevel.RESPONSE else LogLevel.ERROR,
            tag = "$method $shortUrl",
            message = buildString {
                val icon = if (code in 200..299) "✓" else "✗"
                append("$icon $code ${response.message} (${durationMs}ms)\n$url")
                if (!respBodySnippet.isNullOrBlank()) {
                    val snippet = respBodySnippet.take(400)
                    append("\nBody: $snippet${if (respBodySnippet.length > 400) "…" else ""}")
                }
            },
            durationMs = durationMs,
            statusCode = code,
        )

        return response
    }

    private fun shortenUrl(url: String): String {
        return try {
            val path = java.net.URL(url).path
            if (path.isBlank()) url.take(40) else path.take(40)
        } catch (_: Exception) {
            url.take(40)
        }
    }

    private fun filterHeaders(headers: Headers): Map<String, String> {
        val skip = setOf("cookie", "authorization", "set-cookie")
        return (0 until headers.size)
            .mapNotNull { i ->
                val name = headers.name(i).lowercase()
                if (name !in skip) headers.name(i) to headers.value(i) else null
            }
            .toMap()
    }
}
