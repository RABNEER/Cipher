package com.apocalyptolabs.viking.core.ai

import com.apocalyptolabs.viking.core.model.Severity
import com.apocalyptolabs.viking.core.model.ThreatType
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class ThreatClassifierTest {

    private lateinit var gemmaEngine: GemmaEngine
    private lateinit var classifier: ThreatClassifier

    @Before
    fun setUp() {
        gemmaEngine = mock(GemmaEngine::class.java)
        classifier = ThreatClassifier(gemmaEngine)
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

        `when`(gemmaEngine.infer("test_prompt")).thenReturn(validJson)

        val result = classifier.classify("test_prompt", "target.apk", ThreatType.APK)
        assertEquals(Severity.CRITICAL, result.severity)
        assertEquals("Ransomware Signature: Detected encryption routines.", result.explanation)
        assertEquals("Uninstall immediately.", result.action)
    }

    @Test
    fun testClassify_malformedJson_returnsFallbackResult() = runBlocking {
        `when`(gemmaEngine.infer("test_prompt")).thenReturn("Not a valid json response")

        val result = classifier.classify("test_prompt", "target.apk", ThreatType.APK)
        assertEquals(Severity.MEDIUM, result.severity)
        assertEquals("Scan failed, exercise caution. Model response did not contain valid JSON structure.", result.explanation)
    }
}
