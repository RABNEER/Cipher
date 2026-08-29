package com.apocalyptolabs.viking.ui.screens.permissions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apocalyptolabs.viking.core.model.ThreatResult
import com.apocalyptolabs.viking.domain.usecase.AuditPermissionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuditUiState {
    object Idle : AuditUiState()
    object Auditing : AuditUiState()
    data class Success(val flaggedApps: List<ThreatResult>) : AuditUiState()
    data class Error(val message: String) : AuditUiState()
}

@HiltViewModel
class PermissionAuditViewModel @Inject constructor(
    private val auditPermissionsUseCase: AuditPermissionsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuditUiState>(AuditUiState.Idle)
    val uiState: StateFlow<AuditUiState> = _uiState.asStateFlow()

    fun runAudit() {
        viewModelScope.launch {
            _uiState.value = AuditUiState.Auditing
            try {
                val results = auditPermissionsUseCase()
                _uiState.value = AuditUiState.Success(results)
            } catch (e: Exception) {
                _uiState.value = AuditUiState.Error(e.localizedMessage ?: "Permission audit failed.")
            }
        }
    }
}
