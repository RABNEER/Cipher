package com.apocalyptolabs.viking.core.util

import com.apocalyptolabs.viking.core.model.Severity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Long.toFormattedDate(): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(this))
}

fun Severity.toShortLabel(): String {
    return when (this) {
        Severity.CRITICAL -> "CRITICAL"
        Severity.HIGH -> "HIGH"
        Severity.MEDIUM -> "MEDIUM"
        Severity.LOW -> "LOW"
        Severity.SAFE -> "SAFE"
    }
}
