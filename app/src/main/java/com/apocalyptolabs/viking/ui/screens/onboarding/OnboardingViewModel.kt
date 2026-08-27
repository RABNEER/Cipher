package com.apocalyptolabs.viking.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apocalyptolabs.viking.core.ai.GemmaEngine
import com.apocalyptolabs.viking.data.repository.ThreatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val gemmaEngine: GemmaEngine,
    private val repository: ThreatRepository
) : ViewModel() {

    val isModelReady: StateFlow<Boolean> = gemmaEngine.isReady

    fun completeOnboarding() {
        viewModelScope.launch {
            repository.setOnboardingComplete(true)
        }
    }
}
