package com.apocalyptolabs.viking.domain.usecase

import android.content.Context
import com.apocalyptolabs.viking.core.ai.PromptBuilder
import com.apocalyptolabs.viking.core.ai.ThreatClassifier
import com.apocalyptolabs.viking.core.model.CallerType
import com.apocalyptolabs.viking.core.model.Severity
import com.apocalyptolabs.viking.core.model.ThreatResult
import com.apocalyptolabs.viking.core.model.ThreatType
import com.apocalyptolabs.viking.core.util.VikingLogger
import com.apocalyptolabs.viking.data.repository.ThreatRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FingerprintCallUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val classifier: ThreatClassifier,
    private val repository: ThreatRepository
) {

    private val INTERNATIONAL_SCAM_PREFIXES = listOf("+92", "+1900", "+44700", "+234", "+880", "+254")
    private val telemarketerPrefixes = mutableSetOf("140")

    init {
        loadTelemarketerPrefixesAsset()
    }

    private fun loadTelemarketerPrefixesAsset() {
        try {
            context.assets.open("scam_prefixes.txt").bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    if (line.isBlank() || line.trimStart().startsWith("#")) return@forEach
                    val prefix = line.split(",").firstOrNull()?.trim() ?: return@forEach
                    if (prefix.isNotEmpty()) telemarketerPrefixes.add(prefix)
                }
            }
        } catch (e: Exception) {
            VikingLogger.w("Could not load scam_prefixes.txt asset, using defaults", "FingerprintCallUseCase")
        }
    }

    suspend operator fun invoke(
        callerNumber: String,
        callDurationSeconds: Int = 0,
        speechFeatures: List<String> = emptyList()
    ): ThreatResult {
        val startTime = System.currentTimeMillis()
        val cleanNumber = callerNumber.removePrefix("+91").trim()

        val is140Telemarketer = telemarketerPrefixes.any { cleanNumber.startsWith(it) }
        val isIntlScam = INTERNATIONAL_SCAM_PREFIXES.any { callerNumber.startsWith(it) }
        val isSpoofedBankFormat = cleanNumber.startsWith("1800") && cleanNumber.length != 11

        val callerType = when {
            isSpoofedBankFormat -> CallerType.SPOOFED
            is140Telemarketer -> CallerType.TELEMARKETING_140
            isIntlScam -> CallerType.INTERNATIONAL_SUSPICIOUS
            else -> CallerType.DOMESTIC_UNKNOWN
        }

        val directReturn = PromptBuilder.checkCallDirectReturn(
            callerType = callerType,
            callerNumber = callerNumber,
            callDurationSeconds = callDurationSeconds
        )

        if (directReturn != null) {
            safeLog(directReturn, System.currentTimeMillis() - startTime)
            return directReturn
        }

        // A short international ping from a known scam origin is itself a HIGH signal,
        // regardless of what the model returns — never downgrade below HIGH.
        val isShortIntlPing = isIntlScam && callDurationSeconds in 1..3
        val isLongTelemarketer = is140Telemarketer && callDurationSeconds > 60

        val prompt = PromptBuilder.callPrompt(
            callerType = callerType,
            callDurationSeconds = callDurationSeconds,
            isRepeatCaller = false,
            prefixRiskScore = if (isIntlScam) 9 else if (is140Telemarketer) 6 else 0,
            lengthMismatch = isSpoofedBankFormat
        )

        val result = classifier.classify(prompt, "Call from $callerNumber", ThreatType.CALL)

        val finalResult = when {
            isShortIntlPing -> result.copy(
                severity = maxOf(result.severity, Severity.HIGH),
                explanation = "Wangiri callback scam signature: short international ping from a known high-risk prefix. ${result.explanation}",
                action = "Do NOT call back unknown international numbers."
            )
            isLongTelemarketer -> result.copy(
                severity = maxOf(result.severity, Severity.HIGH),
                explanation = "Extended 140-telemarketer call (>60s) with high social engineering risk. ${result.explanation}",
                action = "Disconnect call if asked for OTP or personal banking details."
            )
            else -> result
        }

        safeLog(finalResult, System.currentTimeMillis() - startTime)
        return finalResult
    }

    private suspend fun safeLog(threat: ThreatResult, duration: Long) {
        try {
            repository.logThreat(threat, duration)
        } catch (_: Throwable) {
        }
    }
}
