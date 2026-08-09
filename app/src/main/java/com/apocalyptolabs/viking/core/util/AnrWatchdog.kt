package com.apocalyptolabs.viking.core.util

import android.os.Handler
import android.os.Looper

class AnrWatchdog(private val timeoutMs: Long = 3000L) : Thread("viking-anr-watchdog") {

    private val mainHandler = Handler(Looper.getMainLooper())
    @Volatile
    private var tick = 0L

    private val tickerRunnable = Runnable { tick = (tick + 1) % Long.MAX_VALUE }

    override fun run() {
        var lastTick = 0L
        while (!isInterrupted) {
            lastTick = tick
            mainHandler.post(tickerRunnable)
            try {
                sleep(timeoutMs)
            } catch (e: InterruptedException) {
                return
            }

            if (tick == lastTick) {
                val mainThread = Looper.getMainLooper().thread
                val stackTrace = mainThread.stackTrace.joinToString("\n")
                VikingLogger.e("ANR DETECTED: Main thread blocked > ${timeoutMs}ms!\nStack Trace:\n$stackTrace", tag = "ANRWatchdog")
            }
        }
    }
}
