package com.apocalyptolabs.viking.domain.usecase

import android.app.usage.UsageStatsManager
import android.content.Context
import com.apocalyptolabs.viking.core.ai.PromptBuilder
import com.apocalyptolabs.viking.core.ai.ThreatClassifier
import com.apocalyptolabs.viking.core.model.NfcRecordType
import com.apocalyptolabs.viking.core.model.Severity
import com.apocalyptolabs.viking.core.model.ThreatResult
import com.apocalyptolabs.viking.core.model.ThreatType
import com.apocalyptolabs.viking.core.util.VikingLogger
import com.apocalyptolabs.viking.data.repository.ThreatRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class MonitorNfcUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val classifier: ThreatClassifier,
    private val repository: ThreatRepository
) {
    private val PAYMENT_PACKAGES = setOf(
        "com.phonepe.app", "com.google.android.apps.nfc.payment", "net.one97.paytm", "org.altruist.bhim"
    )

    suspend operator fun invoke(
        tagType: String,
        dataSize: Int,
        payloadUrl: String? = null,
        isStandardFormat: Boolean = true,
        readLatencyMs: Long = 0L
    ): ThreatResult {
        val startTime = System.currentTimeMillis()

        // 1. Payment app foreground check
        if (isPaymentAppInForeground()) {
            val blockedResult = ThreatResult(
                target = "NFC Tag ($tagType)",
                type = ThreatType.NFC,
                severity = Severity.HIGH,
                explanation = "NFC read attempt blocked while payment application is in foreground.",
                action = "Close payment app before scanning unverified NFC tags."
            )
            repository.logThreat(blockedResult, System.currentTimeMillis() - startTime)
            return blockedResult
        }

        val hasExternalUrl = !payloadUrl.isNullOrBlank() && (payloadUrl.startsWith("http://") || payloadUrl.startsWith("https://"))
        val rType = if (hasExternalUrl) NfcRecordType.URL else NfcRecordType.TEXT
        val isRelaySuspected = readLatencyMs > 500L

        // 2. Direct Return check
        val directReturn = PromptBuilder.checkNfcDirectReturn(
            recordType = rType,
            isRelayAttackSuspected = isRelaySuspected
        )

        if (directReturn != null) {
            repository.logThreat(directReturn, System.currentTimeMillis() - startTime)
            return directReturn
        }

        val prompt = PromptBuilder.nfcPrompt(
            recordType = rType,
            hasExternalUrl = hasExternalUrl,
            isStandardPaymentFormat = isStandardFormat,
            domainIfUrl = payloadUrl,
            readTimeMs = readLatencyMs,
            isRelayAttackSuspected = isRelaySuspected
        )

        val result = classifier.classify(prompt, "NFC Tag ($tagType)", ThreatType.NFC)
        val duration = System.currentTimeMillis() - startTime
        repository.logThreat(result, duration)
        return result
    }

    private fun isPaymentAppInForeground(): Boolean {
        try {
            val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return false
            val endTime = System.currentTimeMillis()
            val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, endTime - 10_000L, endTime)
            if (stats.isNullOrEmpty()) return false

            val topApp = stats.maxByOrNull { it.lastTimeUsed } ?: return false
            return PAYMENT_PACKAGES.contains(topApp.packageName)
        } catch (e: Exception) {
            VikingLogger.w("Could not check UsageStatsManager for payment apps", "MonitorNfcUseCase")
            return false
        }
    }
}
