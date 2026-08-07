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

    fun resetScan() {
        _scanState.value = ScanState.Idle
    }
}
