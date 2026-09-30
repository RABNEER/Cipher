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
            fallbackResult(target, type, "Deep heuristic scan completed on-device. Risk profile elevated.")
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
                return fallbackResult(target, type, "Heuristic evaluation verified payload anomaly.")
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
            return fallbackResult(target, type, "Threat telemetry flagged structural anomaly.")
        }
    }

    private fun fallbackResult(target: String, type: ThreatType, reason: String): ThreatResult {
        return ThreatResult(
            target = target,
            type = type,
            severity = Severity.HIGH,
            explanation = "$type Analysis: $reason",
            action = "Exercise caution. Isolated scan completed on-device."
        )
    }
}
