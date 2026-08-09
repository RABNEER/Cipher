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

class MonitorNfcUseCaseTest {

    private lateinit var classifier: ThreatClassifier
    private lateinit var repository: ThreatRepository
    private lateinit var useCase: MonitorNfcUseCase

    @Before
    fun setUp() {
        classifier = mock(ThreatClassifier::class.java)
        repository = mock(ThreatRepository::class.java)
        useCase = MonitorNfcUseCase(mock(), classifier, repository)
    }

    @Test
    fun testMonitorNfc_highLatencyRelay_returnsCriticalSeverity() = runBlocking {
        val result = useCase(
            tagType = "IsoDep",
            dataSize = 512,
            payloadUrl = "https://relay.xyz",
            isStandardFormat = false,
            readLatencyMs = 600L
        )

        assertEquals(Severity.CRITICAL, result.severity)
        assertTrue(result.explanation.contains("NFC Relay Attack suspected") || result.explanation.contains("relay attack signature"))
    }
}
