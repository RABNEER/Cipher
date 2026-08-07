package com.apocalyptolabs.viking.core.ai

import com.apocalyptolabs.viking.core.model.Severity
import com.apocalyptolabs.viking.core.model.ThreatResult
import com.apocalyptolabs.viking.core.model.ThreatType
import com.apocalyptolabs.viking.core.util.VikingLogger
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Executes structured prompts via GemmaEngine and parses the response into ThreatResult objects.
 * Implements strict fail-safe fallback logic in compliance with Viking security constraints.
 */
@Singleton
class ThreatClassifier @Inject constructor(
    private val gemmaEngine: GemmaEngine
) {
    companion object {
        private const val TAG = "ThreatClassifier"
    }

    suspend fun classify(
        prompt: String,
        target: String,
        type: ThreatType
    ): ThreatResult {
        return try {
            val rawResponse = gemmaEngine.infer(prompt)
            parseResponse(rawResponse, target, type)
        } catch (e: PromptInjectionException) {
            VikingLogger.w("Prompt injection detected and blocked: ${e.injectionTerm}", TAG)
            PromptBuilder.buildInjectionDetectedResult(target, type)
        } catch (e: Exception) {
            VikingLogger.w("Inference/Parsing failed for target $target of type $type. Falling back to fail-safe result.", TAG)
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
