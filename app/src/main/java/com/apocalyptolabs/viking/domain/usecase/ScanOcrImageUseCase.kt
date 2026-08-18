package com.apocalyptolabs.viking.domain.usecase

import com.apocalyptolabs.viking.core.model.Severity
import com.apocalyptolabs.viking.core.model.ThreatResult
import com.apocalyptolabs.viking.core.model.ThreatType
import com.apocalyptolabs.viking.data.repository.ThreatRepository
import javax.inject.Inject

class ScanOcrImageUseCase @Inject constructor(
    private val checkUpiLinkUseCase: CheckUpiLinkUseCase,
    private val repository: ThreatRepository
) {
    private val UPI_QR_REGEX = Regex("upi://pay\\?[^\\s]+")
    private val URL_REGEX = Regex("https?://[a-zA-Z0-9.\\-_/]+")
    private val VPA_REGEX = Regex("\\b[a-zA-Z0-9.\\-_]{2,}@[a-zA-Z][a-zA-Z0-9]{1,}\\b")

    suspend operator fun invoke(extractedOcrText: String): ThreatResult {
        val startTime = System.currentTimeMillis()
        val lowerText = extractedOcrText.lowercase()

        val upiMatch = UPI_QR_REGEX.find(extractedOcrText)?.value
        val urlMatch = URL_REGEX.find(extractedOcrText)?.value
        // Bare payee VPAs (merchant@upi) pasted as plain text in screenshots.
        val vpaMatch = if (upiMatch == null && urlMatch == null) {
            VPA_REGEX.find(extractedOcrText)?.value
        } else null

        val targetUrl = upiMatch ?: urlMatch ?: vpaMatch?.let { "upi://pay?pa=$it" }

        if (targetUrl != null) {
            // CheckUpiLinkUseCase already persists the scan; do not double-log.
            val scanResult = checkUpiLinkUseCase(targetUrl)
            return scanResult.copy(
                explanation = "OCR Screenshot Scan: Extracted target ($targetUrl). ${scanResult.explanation}"
            )
        }

        val isPhishingText = lowerText.contains("kyc") || lowerText.contains("blocked") || lowerText.contains("lottery")
        val result = ThreatResult(
            target = "OCR Screenshot",
            type = ThreatType.UPI,
            severity = if (isPhishingText) Severity.HIGH else Severity.SAFE,
            explanation = if (isPhishingText) "Suspicious phishing text detected in image screenshot." else "No malicious payment link or phishing pattern found in image.",
            action = if (isPhishingText) "Do not follow instructions in screenshot." else "No action needed."
        )

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
