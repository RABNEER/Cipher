package com.apocalyptolabs.viking.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apocalyptolabs.viking.core.ai.GemmaEngine
import com.apocalyptolabs.viking.core.model.ThreatResult
import com.apocalyptolabs.viking.data.repository.ThreatRepository
import com.apocalyptolabs.viking.domain.usecase.AnalyzeSmsUseCase
import com.apocalyptolabs.viking.domain.usecase.CheckUpiLinkUseCase
import com.apocalyptolabs.viking.domain.usecase.FingerprintCallUseCase
import com.apocalyptolabs.viking.domain.usecase.MonitorNfcUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val activeThreatCount: Int = 0,
    val totalThreatCount: Int = 0,
    val isModelReady: Boolean = false,
    val recentThreats: List<ThreatResult> = emptyList()
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: ThreatRepository,
    private val gemmaEngine: GemmaEngine,
    private val checkUpiLinkUseCase: CheckUpiLinkUseCase,
    private val analyzeSmsUseCase: AnalyzeSmsUseCase,
    private val fingerprintCallUseCase: FingerprintCallUseCase,
    private val monitorNfcUseCase: MonitorNfcUseCase
) : ViewModel() {

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _testResult = MutableStateFlow<ThreatResult?>(null)
    val testResult: StateFlow<ThreatResult?> = _testResult.asStateFlow()

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.activeThreatCount,
        repository.totalThreatCount,
        gemmaEngine.isReady,
        repository.allThreatLogs
    ) { activeCount, totalCount, isReady, logs ->
        DashboardUiState(
            activeThreatCount = activeCount,
            totalThreatCount = totalCount,
            isModelReady = isReady,
            recentThreats = logs.take(5)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    fun testUpiLink(url: String) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            _testResult.value = null
            try {
                val result = checkUpiLinkUseCase(url)
                _testResult.value = result
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    fun testSms(sender: String, body: String) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            _testResult.value = null
            try {
                val result = analyzeSmsUseCase(sender, body)
                _testResult.value = result
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    fun testCall(number: String, durationSec: Int) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            _testResult.value = null
            try {
                val result = fingerprintCallUseCase(number, durationSec)
                _testResult.value = result
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    fun testNfc(payloadUrl: String?, readLatencyMs: Long) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            _testResult.value = null
            try {
                val result = monitorNfcUseCase(
                    tagType = "NFC Tag",
                    dataSize = payloadUrl?.length ?: 32,
                    payloadUrl = payloadUrl,
                    readLatencyMs = readLatencyMs
                )
                _testResult.value = result
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    fun clearTestResult() {
        _testResult.value = null
    }
}
