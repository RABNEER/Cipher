package com.apocalyptolabs.viking.ui.screens.sandbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apocalyptolabs.viking.core.model.Severity
import com.apocalyptolabs.viking.core.model.ThreatResult
import com.apocalyptolabs.viking.core.util.VikingVoiceAssistant
import com.apocalyptolabs.viking.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AttackSandboxViewModel @Inject constructor(
    private val analyzeSmsUseCase: AnalyzeSmsUseCase,
    private val checkUpiLinkUseCase: CheckUpiLinkUseCase,
    private val fingerprintCallUseCase: FingerprintCallUseCase,
    private val monitorNfcUseCase: MonitorNfcUseCase,
    private val auditPermissionsUseCase: AuditPermissionsUseCase,
    private val voiceAssistant: VikingVoiceAssistant
) : ViewModel() {

    private val _lastSimulatedResult = MutableStateFlow<ThreatResult?>(null)
    val lastSimulatedResult: StateFlow<ThreatResult?> = _lastSimulatedResult.asStateFlow()

    fun simulateSbiPhishingSms() {
        viewModelScope.launch {
            val result = analyzeSmsUseCase(
                sender = "1409991234",
                messageBody = "URGENT: Your SBI account 4829 is suspended. Verify KYC immediately at https://sbi-kyc-update.com to avoid account closure."
            )
            _lastSimulatedResult.value = result
            voiceAssistant.speakThreatAlert(result.severity, result.target)
        }
    }

    fun simulateHomoglyphUpiLink() {
        viewModelScope.launch {
            val result = checkUpiLinkUseCase("https://paytm-0fficial.com/claim-cashback-500")
            _lastSimulatedResult.value = result
            voiceAssistant.speakThreatAlert(result.severity, result.target)
        }
    }

    fun simulateSpoofedBankCall() {
        viewModelScope.launch {
            val result = fingerprintCallUseCase(
                callerNumber = "+91180012345", // invalid length 1800 format spoof
                callDurationSeconds = 45
            )
            _lastSimulatedResult.value = result
            voiceAssistant.speakThreatAlert(result.severity, result.target)
        }
    }

    fun simulateNfcRelayAttack() {
        viewModelScope.launch {
            val result = monitorNfcUseCase(
                tagType = "IsoDep",
                dataSize = 512,
                payloadUrl = "https://relay-attack.xyz",
                isStandardFormat = false,
                readLatencyMs = 750L // >500ms trigger
            )
            _lastSimulatedResult.value = result
            voiceAssistant.speakThreatAlert(result.severity, result.target)
        }
    }

    fun simulateOverPrivilegedAppAudit() {
        viewModelScope.launch {
            val results = auditPermissionsUseCase()
            _lastSimulatedResult.value = results.firstOrNull() ?: ThreatResult(
                target = "Super Flashlight App",
                type = com.apocalyptolabs.viking.core.model.ThreatType.PERMISSION,
                severity = Severity.HIGH,
                explanation = "Over-privileged utility app detected requesting SMS & Audio recording.",
                action = "Revoke dangerous permissions in Settings."
            )
        }
    }
}
