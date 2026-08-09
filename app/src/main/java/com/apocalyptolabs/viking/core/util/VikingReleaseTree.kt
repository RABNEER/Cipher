package com.apocalyptolabs.viking.core.util

import android.content.Context
import android.util.Log
import timber.log.Timber
import java.io.File
import java.io.FileWriter

class VikingReleaseTree(private val context: Context) : Timber.Tree() {

    private val logDir = File(context.filesDir, "logs").apply { if (!exists()) mkdirs() }
    private val maxFileSize = 1_024_024L

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority < Log.ERROR) return

        try {
            rotateLogsIfNeeded()
            val targetFile = File(logDir, "viking_app_1.log")
            FileWriter(targetFile, true).use { writer ->
                val time = System.currentTimeMillis()
                writer.append("$time [${tag ?: "Viking"}] ERROR: $message\n")
                if (t != null) {
                    writer.append(Log.getStackTraceString(t))
                    writer.append("\n")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun rotateLogsIfNeeded() {
        val currentLog = File(logDir, "viking_app_1.log")
        if (currentLog.exists() && currentLog.length() >= maxFileSize) {
            val log3 = File(logDir, "viking_app_3.log")
            val log2 = File(logDir, "viking_app_2.log")

            if (log3.exists()) log3.delete()
            if (log2.exists()) log2.renameTo(log3)
            currentLog.renameTo(log2)
        }
    }
}
