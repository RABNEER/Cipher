package com.apocalyptolabs.viking.domain.usecase

import com.apocalyptolabs.viking.core.ai.PromptBuilder
import com.apocalyptolabs.viking.core.ai.ThreatClassifier
import com.apocalyptolabs.viking.core.model.CallerType
import com.apocalyptolabs.viking.core.model.Severity
import com.apocalyptolabs.viking.core.model.ThreatResult
import com.apocalyptolabs.viking.core.model.ThreatType
import com.apocalyptolabs.viking.data.repository.ThreatRepository
import javax.inject.Inject

class FingerprintCallUseCase @Inject constructor(
    private val classifier: ThreatClassifier,
    private val repository: ThreatRepository
) {

    private val INTERNATIONAL_SCAM_PREFIXES = listOf("+92", "+1900", "+44700", "+234", "+880", "+254")

    suspend operator fun invoke(
        callerNumber: String,
        callDurationSeconds: Int = 0,
        speechFeatures: List<String> = emptyList()
    ): ThreatResult {
        val startTime = System.currentTimeMillis()

        val is140Telemarketer = callerNumber.startsWith("140")
        val isIntlScam = INTERNATIONAL_SCAM_PREFIXES.any { callerNumber.startsWith(it) }
        val isSpoofedBankFormat = callerNumber.startsWith("1800") && callerNumber.length != 11

        val callerType = when {
            isSpoofedBankFormat -> CallerType.SPOOFED
            is140Telemarketer -> CallerType.TELEMARKETING_140
            isIntlScam -> CallerType.INTERNATIONAL_SUSPICIOUS
            else -> CallerType.DOMESTIC_UNKNOWN
        }

        // 1. Direct Return check
        val directReturn = PromptBuilder.checkCallDirectReturn(
            callerType = callerType,
            callerNumber = callerNumber,
            callDurationSeconds = callDurationSeconds
        )

        if (directReturn != null) {
            repository.logThreat(directReturn, System.currentTimeMillis() - startTime)
            return directReturn
        }

        // Duration heuristics
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
            isLongTelemarketer -> result.copy(
                severity = Severity.HIGH,
                explanation = "Extended 140-telemarketer call (>60s) with high social engineering risk. ${result.explanation}",
                action = "Disconnect call if asked for OTP or personal banking details."
            )
            isShortIntlPing -> result.copy(
                severity = Severity.LOW,
                explanation = "Short international ping call (wangiri callback scam signature).",
                action = "Do not call back unknown international numbers."
            )
            else -> result
        }

        val duration = System.currentTimeMillis() - startTime
        repository.logThreat(finalResult, duration)
        return finalResult
    }
}
