package com.apocalyptolabs.viking.core.ai

import com.apocalyptolabs.viking.core.model.Severity
import com.apocalyptolabs.viking.core.model.ThreatResult
import com.apocalyptolabs.viking.core.model.ThreatType
import com.apocalyptolabs.viking.core.util.VikingLogger
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThreatClassifier(
    private val inferenceEngine: InferenceEngine
) {
    @Inject
    constructor(gemmaEngine: GemmaEngine) : this(gemmaEngine as InferenceEngine)

    companion object {
        private const val TAG = "ThreatClassifier"
    }

    suspend fun classify(
        prompt: String,
        target: String,
        type: ThreatType
    ): ThreatResult {
        PromptBuilder.detectInjection(prompt)?.let { injectionTerm ->
            VikingLogger.w("Prompt injection detected and blocked: $injectionTerm", TAG)
            return PromptBuilder.buildInjectionDetectedResult(target, type)
        }
        return try {
            val rawResponse = inferenceEngine.infer(prompt)
            parseResponse(rawResponse, target, type)
        } catch (e: Exception) {
            VikingLogger.w("Inference fallback triggered for $target ($type)", TAG)
            fallbackResult(target, type, "Scan engine unavailable or execution timed out. Exercise caution.")
        }
    }

    private fun parseResponse(
        rawResponse: String,
        target: String,
        type: ThreatType
    ): ThreatResult {
        try {
            val cleaned = rawResponse
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val jsonStart = cleaned.indexOf('{')
            val jsonEnd = cleaned.lastIndexOf('}')

            if (jsonStart == -1 || jsonEnd == -1 || jsonEnd < jsonStart) {
                return fallbackResult(target, type, "Model response did not contain valid JSON structure.")
            }

            val jsonString = cleaned.substring(jsonStart, jsonEnd + 1)
            val json = JSONObject(jsonString)

            val severityStr = json.optString("severity", "MEDIUM").uppercase()
            val severity = try {
                Severity.valueOf(severityStr)
            } catch (e: IllegalArgumentException) {
                Severity.MEDIUM
            }

            val threat = json.optString("threat", "$type Analysis")
            val explanation = json.optString("explanation", "Extracted features analyzed on-device.")
            val action = json.optString("action", "Review application permissions and behavior.")

            return ThreatResult(
                target = target,
                type = type,
                severity = severity,
                explanation = if (threat.isNotBlank() && threat != "$type Analysis") "$threat: $explanation" else explanation,
                action = action
            )
        } catch (e: Exception) {
            VikingLogger.e("JSON parsing error on response", e, TAG)
            return fallbackResult(target, type, "Failed to parse model output safely.")
        }
    }

    private fun fallbackResult(target: String, type: ThreatType, reason: String): ThreatResult {
        return ThreatResult(
            target = target,
            type = type,
            severity = Severity.MEDIUM,
            explanation = "Scan failed, exercise caution. $reason",
            action = "Manual review recommended before proceeding."
        )
    }
}
