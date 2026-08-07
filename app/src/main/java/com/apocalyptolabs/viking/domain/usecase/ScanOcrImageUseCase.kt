package com.apocalyptolabs.viking.domain.usecase

import android.content.Context
import com.apocalyptolabs.viking.core.ai.PromptBuilder
import com.apocalyptolabs.viking.core.ai.ThreatClassifier
import com.apocalyptolabs.viking.core.model.Severity
import com.apocalyptolabs.viking.core.model.ThreatResult
import com.apocalyptolabs.viking.core.model.ThreatType
import com.apocalyptolabs.viking.data.repository.ThreatRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ScanOcrImageUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val checkUpiLinkUseCase: CheckUpiLinkUseCase,
    private val repository: ThreatRepository
) {
    private val UPI_QR_REGEX = Regex("upi://pay\\?[^\\s]+")
    private val URL_REGEX = Regex("https?://[a-zA-Z0-9.\\-_/]+")

    suspend operator fun invoke(extractedOcrText: String): ThreatResult {
        val startTime = System.currentTimeMillis()
        val lowerText = extractedOcrText.lowercase()

        val upiMatch = UPI_QR_REGEX.find(extractedOcrText)?.value
        val urlMatch = URL_REGEX.find(extractedOcrText)?.value

        val targetUrl = upiMatch ?: urlMatch

        if (targetUrl != null) {
            val result = checkUpiLinkUseCase(targetUrl)
            val updatedExplanation = "OCR Screenshot Scan: Extracted target ($targetUrl). ${result.explanation}"
            val finalResult = result.copy(explanation = updatedExplanation)
            repository.logThreat(finalResult, System.currentTimeMillis() - startTime)
            return finalResult
        }

        // Generic text scan
        val isPhishingText = lowerText.contains("kyc") || lowerText.contains("blocked") || lowerText.contains("lottery")
        val result = ThreatResult(
            target = "OCR Screenshot",
            type = ThreatType.UPI,
            severity = if (isPhishingText) Severity.HIGH else Severity.SAFE,
            explanation = if (isPhishingText) "Suspicious phishing text detected in image screenshot." else "No malicious payment link or phishing pattern found in image.",
            action = if (isPhishingText) "Do not follow instructions in screenshot." else "No action needed."
        )

        repository.logThreat(result, System.currentTimeMillis() - startTime)
        return result
    }
}
