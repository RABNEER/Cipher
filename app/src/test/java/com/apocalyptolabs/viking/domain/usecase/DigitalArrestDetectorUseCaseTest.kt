package com.apocalyptolabs.viking.domain.usecase

import com.apocalyptolabs.viking.core.ai.ThreatClassifier
import com.apocalyptolabs.viking.core.model.Severity
import com.apocalyptolabs.viking.data.repository.ThreatRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock

class DigitalArrestDetectorUseCaseTest {

    private lateinit var classifier: ThreatClassifier
    private lateinit var repository: ThreatRepository
    private lateinit var useCase: DigitalArrestDetectorUseCase

    @Before
    fun setUp() {
        classifier = mock(ThreatClassifier::class.java)
        repository = mock(ThreatRepository::class.java)
        useCase = DigitalArrestDetectorUseCase(classifier, repository)
    }

    @Test
    fun testDigitalArrest_cbiKeywords_returnsCriticalSeverity() = runBlocking {
        val text = "This is Officer Sharma from CBI. Your Aadhaar is under digital arrest due to illegal parcel."
        val result = useCase("Unknown Officer", text)

        assertEquals(Severity.CRITICAL, result.severity)
        assertTrue(result.explanation.contains("DIGITAL ARREST FRAUD DETECTED"))
        assertTrue(result.action.contains("DISCONNECT IMMEDIATELY"))
    }
}
