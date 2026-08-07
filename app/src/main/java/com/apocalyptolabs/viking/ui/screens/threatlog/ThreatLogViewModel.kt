package com.apocalyptolabs.viking.ui.screens.threatlog

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apocalyptolabs.viking.core.model.ThreatResult
import com.apocalyptolabs.viking.core.model.ThreatType
import com.apocalyptolabs.viking.data.repository.ThreatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThreatLogViewModel @Inject constructor(
    private val repository: ThreatRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val initialTypeParam: String? = savedStateHandle.get<String>("type")

    private val _selectedTypeFilter = MutableStateFlow<ThreatType?>(
        initialTypeParam?.let { param ->
            try { ThreatType.valueOf(param.uppercase()) } catch (e: Exception) { null }
        }
    )
    val selectedTypeFilter: StateFlow<ThreatType?> = _selectedTypeFilter.asStateFlow()

    val threatLogs: StateFlow<List<ThreatResult>> = repository.allThreatLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun setTypeFilter(type: ThreatType?) {
        _selectedTypeFilter.value = type
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearAllLogs()
        }
    }
}
