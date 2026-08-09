package com.apocalyptolabs.viking.domain.usecase

import android.content.Context
import com.apocalyptolabs.viking.core.ai.ThreatClassifier
import com.apocalyptolabs.viking.core.model.Severity
import com.apocalyptolabs.viking.data.repository.ThreatRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock

class AnalyzeSmsUseCaseTest {

    private lateinit var context: Context
    private lateinit var classifier: ThreatClassifier
    private lateinit var repository: ThreatRepository
    private lateinit var useCase: AnalyzeSmsUseCase

    @Before
    fun setUp() {
        context = mock(Context::class.java)
        classifier = mock(ThreatClassifier::class.java)
        repository = mock(ThreatRepository::class.java)
        useCase = AnalyzeSmsUseCase(context, classifier, repository)
    }

    @Test
    fun testAnalyzeSms_bankShortcode_returnsSafeSeverity() = runBlocking {
        val result = useCase("VM-SBIINB", "Your A/C 1234 credited with Rs 500.")
        assertEquals(Severity.SAFE, result.severity)
        assertEquals("Legitimate financial institution short code (VM-SBIINB).", result.explanation)
    }
}
