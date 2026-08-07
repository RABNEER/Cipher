package com.apocalyptolabs.viking.domain.usecase

import android.content.Context
import com.apocalyptolabs.viking.core.ai.ThreatClassifier
import com.apocalyptolabs.viking.core.model.Severity
import com.apocalyptolabs.viking.data.repository.ThreatRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock

class CheckUpiLinkUseCaseTest {

    private lateinit var context: Context
    private lateinit var classifier: ThreatClassifier
    private lateinit var repository: ThreatRepository
    private lateinit var useCase: CheckUpiLinkUseCase

    @Before
    fun setUp() {
        context = mock(Context::class.java)
        classifier = mock(ThreatClassifier::class.java)
        repository = mock(ThreatRepository::class.java)
        useCase = CheckUpiLinkUseCase(context, classifier, repository)
    }

    @Test
    fun testCheckUpiLink_punycodeDomain_returnsHighSeverity() = runBlocking {
        val result = useCase("https://xn--sbi-83a.com")
        assertEquals(Severity.HIGH, result.severity)
        assertTrue(result.explanation.contains("Punycode xn--"))
    }

    @Test
    fun testCheckUpiLink_shortenerUrl_returnsHighSeverity() = runBlocking {
        val result = useCase("https://bit.ly/claim-refund")
        assertEquals(Severity.HIGH, result.severity)
        assertTrue(result.explanation.contains("Shortened URL"))
    }
}
