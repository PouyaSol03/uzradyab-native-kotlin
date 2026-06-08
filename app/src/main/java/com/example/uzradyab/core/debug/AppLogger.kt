package com.example.uzradyab.core.debug

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicLong

// ── Log level ──────────────────────────────────────────────────────────────────
enum class LogLevel { REQUEST, RESPONSE, ERROR, INFO }

// ── A single log entry ────────────────────────────────────────────────────────
data class LogEntry(
    val id: Long,
    val timestamp: String,
    val level: LogLevel,
    val tag: String,
    val message: String,
    val durationMs: Long? = null,
    val statusCode: Int? = null,
)

// ── Singleton store ────────────────────────────────────────────────────────────
object AppLogger {
    private const val MAX_ENTRIES = 500
    private val counter = AtomicLong(0)
    private val sdf = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    fun log(
        level: LogLevel,
        tag: String,
        message: String,
        durationMs: Long? = null,
        statusCode: Int? = null,
    ) {
        val entry = LogEntry(
            id = counter.incrementAndGet(),
            timestamp = sdf.format(Date()),
            level = level,
            tag = tag,
            message = message,
            durationMs = durationMs,
            statusCode = statusCode,
        )
        _logs.update { current ->
            val updated = current + entry
            if (updated.size > MAX_ENTRIES) updated.drop(updated.size - MAX_ENTRIES) else updated
        }
        // Also mirror to Logcat with a single unified tag so grep is easy
        android.util.Log.d("AppLogger", "[$tag] $message")
    }

    fun clear() {
        _logs.value = emptyList()
    }
}
