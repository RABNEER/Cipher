package com.apocalyptolabs.viking

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.apocalyptolabs.viking.core.model.ThreatResult
import com.apocalyptolabs.viking.data.repository.ThreatRepository
import com.apocalyptolabs.viking.domain.usecase.CheckUpiLinkUseCase
import com.apocalyptolabs.viking.ui.navigation.VikingDestinations
import com.apocalyptolabs.viking.ui.navigation.VikingNavGraph
import com.apocalyptolabs.viking.ui.theme.VikingBlack
import com.apocalyptolabs.viking.ui.theme.VikingTeal
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: ThreatRepository,
    private val checkUpiLinkUseCase: CheckUpiLinkUseCase
) : ViewModel() {

    val startDestination: StateFlow<String?> = repository.isOnboardingComplete
        .map { onboarded ->
            if (onboarded) VikingDestinations.DASHBOARD else VikingDestinations.ONBOARDING
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _sharedScanResult = MutableStateFlow<ThreatResult?>(null)
    val sharedScanResult: StateFlow<ThreatResult?> = _sharedScanResult.asStateFlow()

    fun scanSharedText(text: String, onResult: (ThreatResult) -> Unit) {
        viewModelScope.launch {
            try {
                val result = checkUpiLinkUseCase(text)
                _sharedScanResult.value = result
                onResult(result)
            } catch (_: Exception) {
            }
        }
    }

    fun consumeSharedResult() {
        _sharedScanResult.value = null
    }
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleSharedIntent(intent)

        setContent {
            com.apocalyptolabs.viking.ui.theme.VikingTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = VikingBlack
                ) {
                    val startDestination: String? by mainViewModel.startDestination.collectAsState()
                    val resolvedStart = startDestination
                    if (resolvedStart == null) {
                        SplashLoader()
                    } else {
                        VikingNavGraph(startDestination = resolvedStart)
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleSharedIntent(intent)
    }

    private fun handleSharedIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT) ?: return
            mainViewModel.scanSharedText(sharedText) { result ->
                runOnUiThread {
                    Toast.makeText(
                        this,
                        "VIKING scan: ${result.severity.name} — ${result.explanation}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}

@Composable
private fun SplashLoader() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = VikingTeal)
    }
}
