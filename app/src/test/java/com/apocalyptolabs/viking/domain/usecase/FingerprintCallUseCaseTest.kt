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

class FingerprintCallUseCaseTest {

    private lateinit var classifier: ThreatClassifier
    private lateinit var repository: ThreatRepository
    private lateinit var useCase: FingerprintCallUseCase

    @Before
    fun setUp() {
        classifier = mock(ThreatClassifier::class.java)
        repository = mock(ThreatRepository::class.java)
        useCase = FingerprintCallUseCase(classifier, repository)
    }

    @Test
    fun testFingerprintCall_spoofedBankLength_returnsHighSeverity() = runBlocking {
        val result = useCase(callerNumber = "180012345", callDurationSeconds = 30)
        assertEquals(Severity.CRITICAL, result.severity)
        assertTrue(result.explanation.contains("Spoofed Bank Helpline"))
    }
}
