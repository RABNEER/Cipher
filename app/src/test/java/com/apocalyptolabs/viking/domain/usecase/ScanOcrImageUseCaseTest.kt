package com.apocalyptolabs.viking.domain.usecase

import com.apocalyptolabs.viking.core.model.Severity
import com.apocalyptolabs.viking.core.model.ThreatResult
import com.apocalyptolabs.viking.core.model.ThreatType
import com.apocalyptolabs.viking.data.repository.ThreatRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class ScanOcrImageUseCaseTest {

    private lateinit var checkUpiLinkUseCase: CheckUpiLinkUseCase
    private lateinit var repository: ThreatRepository
    private lateinit var useCase: ScanOcrImageUseCase

    @Before
    fun setUp() {
        checkUpiLinkUseCase = mock(CheckUpiLinkUseCase::class.java)
        repository = mock(ThreatRepository::class.java)
        useCase = ScanOcrImageUseCase(mock(), checkUpiLinkUseCase, repository)
    }

    @Test
    fun testScanOcrImage_upiQrExtracted_invokesCheckUpiLinkUseCase() = runBlocking {
        val ocrText = "Scan QR to pay: upi://pay?pa=paytm-0fficial@paytm&pn=Refund"
        val expectedResult = ThreatResult(
            target = "paytm-0fficial@paytm",
            type = ThreatType.UPI,
            severity = Severity.HIGH,
            explanation = "Domain homoglyph detected.",
            action = "Do not pay."
        )

        `when`(checkUpiLinkUseCase(anyString())).thenReturn(expectedResult)

        val result = useCase(ocrText)
        assertEquals(Severity.HIGH, result.severity)
        assertTrue(result.explanation.contains("OCR Screenshot Scan"))
    }
}
