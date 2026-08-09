package com.apocalyptolabs.viking.core.model

data class ThreatResult(
    val id: Long = 0,
    val target: String,
    val type: ThreatType,
    val severity: Severity,
    val explanation: String,
    val action: String,
    val timestamp: Long = System.currentTimeMillis()
)
