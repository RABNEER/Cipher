package com.apocalyptolabs.viking.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object VikingServiceState {
    private val _isMonitoringActive = MutableStateFlow(true)
    val isMonitoringActive: StateFlow<Boolean> = _isMonitoringActive.asStateFlow()

    private val _blockedThreatCount = MutableStateFlow(0)
    val blockedThreatCount: StateFlow<Int> = _blockedThreatCount.asStateFlow()

    fun setMonitoringActive(active: Boolean) {
        _isMonitoringActive.value = active
    }

    fun updateBlockedCount(count: Int) {
        _blockedThreatCount.value = count
    }
}
