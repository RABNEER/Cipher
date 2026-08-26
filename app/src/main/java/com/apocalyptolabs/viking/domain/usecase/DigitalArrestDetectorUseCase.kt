package com.apocalyptolabs.viking.domain.usecase

import com.apocalyptolabs.viking.core.ai.PromptBuilder
import com.apocalyptolabs.viking.core.ai.ThreatClassifier
import com.apocalyptolabs.viking.core.model.CallerType
import com.apocalyptolabs.viking.core.model.Severity
import com.apocalyptolabs.viking.core.model.ThreatResult
import com.apocalyptolabs.viking.core.model.ThreatType
import com.apocalyptolabs.viking.data.repository.ThreatRepository
import javax.inject.Inject

class DigitalArrestDetectorUseCase @Inject constructor(
    private val classifier: ThreatClassifier,
    private val repository: ThreatRepository
) {
    private val DIGITAL_ARREST_KEYWORDS = listOf(
        "cbi", "police", "customs", "narcotics", "enforcement directorate",
        "digital arrest", "digital custody", "video interrogation", "skype call",
        "parcel seized", "illegal package", "parcel with drugs", "aadhaar blocked",
        "warrant issued", "arrest warrant", "money laundering case"
    )

    private val KEYWORD_REGEXES = DIGITAL_ARREST_KEYWORDS.map {
        Regex("\\b${Regex.escape(it)}\\b", RegexOption.IGNORE_CASE)
    }

    suspend operator fun invoke(callerOrSender: String, textOrTranscript: String): ThreatResult {
        val startTime = System.currentTimeMillis()

        val matchedKeywords = KEYWORD_REGEXES.withIndex()
            .filter { it.value.containsMatchIn(textOrTranscript) }
            .map { DIGITAL_ARREST_KEYWORDS[it.index] }

        if (matchedKeywords.isNotEmpty()) {
            val criticalResult = ThreatResult(
                target = "Digital Arrest Attempt ($callerOrSender)",
                type = ThreatType.CALL,
                severity = Severity.CRITICAL,
                explanation = "DIGITAL ARREST FRAUD DETECTED! Scammer impersonating law enforcement (${matchedKeywords.joinToString(", ")}). Indian Law Enforcement NEVER conducts arrests or demands money over video/phone calls.",
                action = "DISCONNECT IMMEDIATELY. Do not transfer funds. Report immediately to 1930 Cybercrime Helpline."
            )
            safeLog(criticalResult, System.currentTimeMillis() - startTime)
            return criticalResult
        }

        val prompt = PromptBuilder.callPrompt(
            callerType = CallerType.DOMESTIC_UNKNOWN,
            callDurationSeconds = 60,
            isRepeatCaller = false,
            prefixRiskScore = 5,
            lengthMismatch = false
        )

        val result = classifier.classify(prompt, callerOrSender, ThreatType.CALL)
        safeLog(result, System.currentTimeMillis() - startTime)
        return result
    }

    private suspend fun safeLog(threat: ThreatResult, duration: Long) {
        try {
            repository.logThreat(threat, duration)
        } catch (_: Throwable) {
        }
    }
}
