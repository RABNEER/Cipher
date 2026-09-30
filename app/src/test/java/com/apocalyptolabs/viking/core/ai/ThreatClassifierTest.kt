package com.apocalyptolabs.viking.core.ai

import com.apocalyptolabs.viking.core.model.Severity
import com.apocalyptolabs.viking.core.model.ThreatType
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class ThreatClassifierTest {

    private class FakeInferenceEngine(private val returnJson: String) : InferenceEngine {
        override suspend fun infer(prompt: String): String = returnJson
    }

    @Test
    fun testClassify_validJson_parsesCorrectly() = runBlocking {
        val validJson = """
            ```json
            {
              "severity": "CRITICAL",
              "threat": "Ransomware Signature",
              "explanation": "Detected encryption routines.",
              "action": "Uninstall immediately."
            }
            ```
        """.trimIndent()

        val engine = FakeInferenceEngine(validJson)
        val classifier = ThreatClassifier(engine)

        val result = classifier.classify("test_prompt", "target.apk", ThreatType.APK)
        assertEquals(Severity.CRITICAL, result.severity)
        assertEquals("Ransomware Signature: Detected encryption routines.", result.explanation)
        assertEquals("Uninstall immediately.", result.action)
    }

    @Test
    fun testClassify_malformedJson_returnsFallbackResult() = runBlocking {
        val engine = FakeInferenceEngine("Not a valid json response")
        val classifier = ThreatClassifier(engine)

        val result = classifier.classify("test_prompt", "target.apk", ThreatType.APK)
        assertEquals(Severity.HIGH, result.severity)
        assertEquals("APK Analysis: Heuristic evaluation verified payload anomaly.", result.explanation)
    }
}
