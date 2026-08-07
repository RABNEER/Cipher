package com.apocalyptolabs.viking.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
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

    private val scope = CoroutineScope(Dispatchers.IO)

    companion object {
        private const val TAG = "SimStateReceiver"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != "android.intent.action.SIM_STATE_CHANGED") return

        val state = intent.getStringExtra("ss") ?: return
        VikingLogger.w("SIM State Change Detected: $state", TAG)

        if (state == "ABSENT" || state == "LOADED" || state == "CARD_IO_ERROR") {
            scope.launch {
                val result = ThreatResult(
                    target = "SIM Identity (State: $state)",
                    type = ThreatType.CALL,
                    severity = Severity.HIGH,
                    explanation = "SIM card state changed dynamically. Potential SIM Swap / IMSI takeover event detected.",
                    action = "Verify account security with your mobile network operator."
                )
                repository.logThreat(result)
            }
        }
    }
}
