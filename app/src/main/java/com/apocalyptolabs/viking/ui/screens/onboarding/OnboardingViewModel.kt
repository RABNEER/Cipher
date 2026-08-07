package com.apocalyptolabs.viking.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import com.apocalyptolabs.viking.core.ai.GemmaEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val gemmaEngine: GemmaEngine
) : ViewModel() {

    val isModelReady: StateFlow<Boolean> = gemmaEngine.isReady
}
