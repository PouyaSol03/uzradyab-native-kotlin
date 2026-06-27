package com.example.uzradyab.core.debug

import android.content.Context
import java.io.File

object LocalCrashReporter {
    private const val FILE_NAME = "last_crash.txt"

    fun init(context: Context) {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        val appContext = context.applicationContext
        
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val file = File(appContext.filesDir, FILE_NAME)
                file.writeText(throwable.stackTraceToString())
            } catch (e: Exception) {
                // Ignore any errors while saving the crash
            } finally {
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
    }

    fun getCrashLog(context: Context): String? {
        val file = File(context.filesDir, FILE_NAME)
        return if (file.exists()) {
            runCatching { file.readText() }.getOrNull()
        } else {
            null
        }
    }

    fun clearCrashLog(context: Context) {
        val file = File(context.filesDir, FILE_NAME)
        if (file.exists()) {
            file.delete()
        }
    }
}
