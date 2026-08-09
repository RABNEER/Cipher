package com.apocalyptolabs.viking.domain.usecase

import com.apocalyptolabs.viking.core.ai.PromptBuilder
import com.apocalyptolabs.viking.core.ai.ThreatClassifier
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
        "cbi", "police", "customs", "narcotics", "ed", "enforcement directorate",
        "digital arrest", "digital custody", "video interrogation", "skype call",
        "parcel seized", "illegal package", "aadhaar blocked", "warrant issued"
    )

    suspend operator fun invoke(callerOrSender: String, textOrTranscript: String): ThreatResult {
        val startTime = System.currentTimeMillis()
        val lowerText = textOrTranscript.lowercase()

        val matchedKeywords = DIGITAL_ARREST_KEYWORDS.filter { lowerText.contains(it) }

        if (matchedKeywords.isNotEmpty()) {
            val criticalResult = ThreatResult(
                target = "Digital Arrest Attempt ($callerOrSender)",
                type = ThreatType.CALL,
                severity = Severity.CRITICAL,
                explanation = "DIGITAL ARREST FRAUD DETECTED! Scammer impersonating law enforcement (${matchedKeywords.joinToString(", ")}). Indian Law Enforcement NEVER conducts arrests or demands money over video/phone calls.",
                action = "DISCONNECT IMMEDIATELY. Do not transfer funds. Report immediately to 1930 Cybercrime Helpline."
            )
            repository.logThreat(criticalResult, System.currentTimeMillis() - startTime)
            return criticalResult
        }

        val prompt = PromptBuilder.callPrompt(
            callerType = com.apocalyptolabs.viking.core.model.CallerType.DOMESTIC_UNKNOWN,
            callDurationSeconds = 60,
            isRepeatCaller = false,
            prefixRiskScore = 5,
            lengthMismatch = false
        )

        val result = classifier.classify(prompt, callerOrSender, ThreatType.CALL)
        repository.logThreat(result, System.currentTimeMillis() - startTime)
        return result
    }
}
