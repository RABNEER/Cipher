package com.apocalyptolabs.viking.core.util

import android.content.Context
import java.io.File
import java.io.FileWriter
import kotlin.system.exitProcess

class GlobalExceptionHandler(
    private val context: Context,
    private val defaultHandler: Thread.UncaughtExceptionHandler?
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            val logsDir = File(context.filesDir, "logs").apply { if (!exists()) mkdirs() }
            val crashLog = File(logsDir, "crash.log")
            FileWriter(crashLog, true).use { writer ->
                writer.append("\n=== CRASH AT ${System.currentTimeMillis()} ON THREAD ${thread.name} ===\n")
                writer.append(android.util.Log.getStackTraceString(throwable))
                writer.append("\n===================================================\n")
            }
            VikingLogger.e("GlobalExceptionHandler caught uncaught exception", throwable, "GlobalException")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        defaultHandler?.uncaughtException(thread, throwable) ?: exitProcess(1)
    }

    companion object {
        fun install(context: Context) {
            val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
            Thread.setDefaultUncaughtExceptionHandler(GlobalExceptionHandler(context, defaultHandler))
        }
    }
}
