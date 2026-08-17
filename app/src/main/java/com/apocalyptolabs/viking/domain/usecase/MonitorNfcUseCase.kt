package com.apocalyptolabs.viking.domain.usecase

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Process
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

        if (isPaymentAppInForeground()) {
            val blockedResult = ThreatResult(
                target = "NFC Tag ($tagType)",
                type = ThreatType.NFC,
                severity = Severity.HIGH,
                explanation = "NFC read attempt blocked while payment application is in foreground.",
                action = "Close payment app before scanning unverified NFC tags."
            )
            safeLog(blockedResult, System.currentTimeMillis() - startTime)
            return blockedResult
        }

        val hasExternalUrl = !payloadUrl.isNullOrBlank() && (payloadUrl.startsWith("http://") || payloadUrl.startsWith("https://"))
        val rType = if (!isStandardFormat) {
            NfcRecordType.UNKNOWN
        } else if (hasExternalUrl) {
            NfcRecordType.URL
        } else {
            NfcRecordType.TEXT
        }
        val isRelaySuspected = readLatencyMs > 500L

        val directReturn = PromptBuilder.checkNfcDirectReturn(
            recordType = rType,
            isRelayAttackSuspected = isRelaySuspected
        )

        if (directReturn != null) {
            safeLog(directReturn, System.currentTimeMillis() - startTime)
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
        safeLog(result, duration)
        return result
    }

    /**
     * Only queries UsageStats when the user actually granted "Usage Access".
     * Without the grant, queryUsageStats returns empty results or throws a
     * SecurityException, making the check dead code that silently never fires.
     */
    private fun isPaymentAppInForeground(): Boolean {
        if (!hasUsageStatsPermission()) return false
        return try {
            val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return false
            val endTime = System.currentTimeMillis()
            val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_BEST, endTime - 60_000L, endTime)
            if (stats.isNullOrEmpty()) return false

            // Only trust entries actually used within the last few seconds.
            val recentWindow = endTime - 5_000L
            val topApp = stats.filter { it.lastTimeUsed >= recentWindow }
                .maxByOrNull { it.lastTimeUsed } ?: return false
            PAYMENT_PACKAGES.contains(topApp.packageName)
        } catch (e: Exception) {
            VikingLogger.w("Could not check UsageStatsManager for payment apps", "MonitorNfcUseCase")
            false
        }
    }

    private fun hasUsageStatsPermission(): Boolean {
        return try {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
            val mode = appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
            mode == AppOpsManager.MODE_ALLOWED
        } catch (_: Exception) {
            false
        }
    }

    private suspend fun safeLog(threat: ThreatResult, duration: Long) {
        try {
            repository.logThreat(threat, duration)
        } catch (_: Throwable) {
        }
    }
}
