package com.apocalyptolabs.viking.ui.screens.sandbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apocalyptolabs.viking.core.model.Severity
import com.apocalyptolabs.viking.core.model.ThreatResult
import com.apocalyptolabs.viking.core.util.VikingVoiceAssistant
import com.apocalyptolabs.viking.domain.usecase.AnalyzeSmsUseCase
import com.apocalyptolabs.viking.domain.usecase.AuditPermissionsUseCase
import com.apocalyptolabs.viking.domain.usecase.CheckUpiLinkUseCase
import com.apocalyptolabs.viking.domain.usecase.DigitalArrestDetectorUseCase
import com.apocalyptolabs.viking.domain.usecase.FingerprintCallUseCase
import com.apocalyptolabs.viking.domain.usecase.MonitorNfcUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
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
    private val digitalArrestDetectorUseCase: DigitalArrestDetectorUseCase,
    private val voiceAssistant: VikingVoiceAssistant
) : ViewModel() {

    private val _lastSimulatedResult = MutableStateFlow<ThreatResult?>(null)
    val lastSimulatedResult: StateFlow<ThreatResult?> = _lastSimulatedResult.asStateFlow()

    // Simulations feed adversarial payloads into the pipeline; a sandbox crash
    // would defeat the purpose. Any failure degrades to a caution result.
    private val sandboxExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        _lastSimulatedResult.value = ThreatResult(
            target = "Sandbox Simulation",
            type = com.apocalyptolabs.viking.core.model.ThreatType.UPI,
            severity = Severity.MEDIUM,
            explanation = "Simulation could not complete: ${throwable.localizedMessage ?: "unknown error"}. Exercise caution.",
            action = "Manual review recommended."
        )
    }

    private fun simulate(block: suspend () -> ThreatResult) {
        viewModelScope.launch(sandboxExceptionHandler) {
            val result = block()
            _lastSimulatedResult.value = result
            voiceAssistant.speakThreatAlert(result.severity, result.target)
        }
    }

    fun simulateSbiPhishingSms() = simulate {
        analyzeSmsUseCase(
            sender = "1409991234",
            messageBody = "URGENT: Your SBI account 4829 is suspended. Verify KYC immediately at https://sbi-kyc-update.com to avoid account closure."
        )
    }

    fun simulateHomoglyphUpiLink() = simulate {
        checkUpiLinkUseCase("https://paytm-0fficial.com/claim-cashback-500")
    }

    fun simulateSpoofedBankCall() = simulate {
        fingerprintCallUseCase(
            callerNumber = "+91180012345",
            callDurationSeconds = 45
        )
    }

    fun simulateDigitalArrestScam() = simulate {
        digitalArrestDetectorUseCase(
            callerOrSender = "+91 98765 43210",
            textOrTranscript = "This is CBI officer Sharma. A parcel with illegal items was seized in your name. You are under digital arrest. Stay on this video call."
        )
    }

    fun simulateNfcRelayAttack() = simulate {
        monitorNfcUseCase(
            tagType = "IsoDep",
            dataSize = 512,
            payloadUrl = "https://relay-attack.xyz",
            isStandardFormat = false,
            readLatencyMs = 750L
        )
    }

    fun simulateOverPrivilegedAppAudit() {
        viewModelScope.launch(sandboxExceptionHandler) {
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
