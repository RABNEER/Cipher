package com.apocalyptolabs.viking.core.util

import com.apocalyptolabs.viking.BuildConfig
import timber.log.Timber

object VikingLogger {

    fun d(message: String, tag: String = "Viking") {
        if (BuildConfig.DEBUG) {
            Timber.tag(tag).d(sanitize(message))
        }
    }

    fun i(message: String, tag: String = "Viking") {
        if (BuildConfig.DEBUG) {
            Timber.tag(tag).i(sanitize(message))
        }
    }

    fun w(message: String, tag: String = "Viking") {
        if (BuildConfig.DEBUG) {
            Timber.tag(tag).w(sanitize(message))
        }
    }

    fun e(message: String, throwable: Throwable? = null, tag: String = "Viking") {
        if (throwable != null) {
            Timber.tag(tag).e(throwable, sanitize(message))
        } else {
            Timber.tag(tag).e(sanitize(message))
        }
    }

    private fun sanitize(message: String): String {
        return message
            .replace(Regex("\\b\\d{4,8}\\b"), "[REDACTED_NUM]")
            .replace(Regex("https?://\\S+"), "[REDACTED_URL]")
    }
}
