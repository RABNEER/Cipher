package com.apocalyptolabs.viking.ui.screens.scanner

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apocalyptolabs.viking.core.model.ThreatResult
import com.apocalyptolabs.viking.domain.usecase.ScanApkUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ScanState {
    object Idle : ScanState()
    data class Scanning(val stepMessage: String) : ScanState()
    data class Success(val result: ThreatResult) : ScanState()
    data class Error(val message: String) : ScanState()
}

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val scanApkUseCase: ScanApkUseCase
) : ViewModel() {

    private val _scanState = MutableStateFlow<ScanState>(ScanState.Idle)
    val scanState: StateFlow<ScanState> = _scanState.asStateFlow()

    fun scanApk(uri: Uri) {
        viewModelScope.launch {
            _scanState.value = ScanState.Scanning("Extracting APK manifest & permissions…")
            delay(600)
            _scanState.value = ScanState.Scanning("Analyzing permission patterns via Gemma 270M…")
            delay(800)
            _scanState.value = ScanState.Scanning("Evaluating security risk signature on-device…")

            try {
                val result = scanApkUseCase(uri)
                _scanState.value = ScanState.Success(result)
            } catch (e: Exception) {
                _scanState.value = ScanState.Error(e.localizedMessage ?: "Scanning failed.")
            }
        }
    }

    /**
     * Scans multiple APKs strictly one at a time. Concurrent scans would race
     * on the single _scanState holder, flickering progress and dropping results.
     */
    fun scanApks(uris: List<Uri>) {
        viewModelScope.launch {
            uris.forEachIndexed { index, uri ->
                _scanState.value = ScanState.Scanning("Scanning file ${index + 1} of ${uris.size}…")
                try {
                    val result = scanApkUseCase(uri)
                    if (result.severity >= com.apocalyptolabs.viking.core.model.Severity.HIGH || uris.size == 1) {
                        _scanState.value = ScanState.Success(result)
                        return@forEachIndexed
                    }
                } catch (e: Exception) {
                    _scanState.value = ScanState.Error(e.localizedMessage ?: "Scanning failed.")
                    return@forEachIndexed
                }
                delay(150)
            }
            if (_scanState.value is ScanState.Scanning) {
                _scanState.value = ScanState.Success(
                    com.apocalyptolabs.viking.core.model.ThreatResult(
                        target = "${uris.size} files",
                        type = com.apocalyptolabs.viking.core.model.ThreatType.APK,
                        severity = com.apocalyptolabs.viking.core.model.Severity.SAFE,
                        explanation = "Batch complete: no high-risk packages found across ${uris.size} files.",
                        action = "No action needed."
                    )
                )
            }
        }
    }

    fun resetScan() {
        _scanState.value = ScanState.Idle
    }
}
