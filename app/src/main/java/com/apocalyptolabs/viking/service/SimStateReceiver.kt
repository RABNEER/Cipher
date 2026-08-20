package com.apocalyptolabs.viking.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.apocalyptolabs.viking.core.model.Severity
import com.apocalyptolabs.viking.core.model.ThreatResult
import com.apocalyptolabs.viking.core.model.ThreatType
import com.apocalyptolabs.viking.core.util.VikingLogger
import com.apocalyptolabs.viking.data.repository.ThreatRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SimStateReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: ThreatRepository

    private val scope = CoroutineScope(Dispatchers.IO + kotlinx.coroutines.SupervisorJob())

    companion object {
        private const val TAG = "SimStateReceiver"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != "android.intent.action.SIM_STATE_CHANGED") return

        val state = intent.getStringExtra("ss") ?: return
        VikingLogger.w("SIM State Change Detected: $state", TAG)

        if (state == "ABSENT" || state == "CARD_IO_ERROR") {
            val pendingResult = goAsync()
            scope.launch {
                try {
                    val result = ThreatResult(
                        target = "SIM Identity (State: $state)",
                        type = ThreatType.CALL,
                        severity = Severity.HIGH,
                        explanation = "SIM card removed or hardware I/O error detected. Potential physical tampering or SIM swap in progress.",
                        action = "Verify account security with your mobile network operator immediately."
                    )
                    repository.logThreat(result)
                } catch (e: Exception) {
                    VikingLogger.e("Failed to record SIM state threat", e, TAG)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
