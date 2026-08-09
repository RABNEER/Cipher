package com.apocalyptolabs.viking

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.apocalyptolabs.viking.domain.usecase.CheckUpiLinkUseCase
import com.apocalyptolabs.viking.ui.navigation.VikingDestinations
import com.apocalyptolabs.viking.ui.navigation.VikingNavGraph
import com.apocalyptolabs.viking.ui.theme.VikingBlack
import com.apocalyptolabs.viking.ui.theme.VikingTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var checkUpiLinkUseCase: CheckUpiLinkUseCase

    private val activityScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleSharedIntent(intent)

        setContent {
            VikingTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = VikingBlack
                ) {
                    VikingNavGraph(
                        startDestination = VikingDestinations.ONBOARDING
                    )
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
            activityScope.launch {
                try {
                    checkUpiLinkUseCase(sharedText)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
