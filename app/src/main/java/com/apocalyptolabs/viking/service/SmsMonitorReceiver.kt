package com.apocalyptolabs.viking.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.apocalyptolabs.viking.core.util.VikingLogger
import com.apocalyptolabs.viking.data.repository.ThreatRepository
import com.apocalyptolabs.viking.domain.usecase.AnalyzeSmsUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SmsMonitorReceiver : BroadcastReceiver() {

    @Inject
    lateinit var analyzeSmsUseCase: AnalyzeSmsUseCase

    @Inject
    lateinit var repository: ThreatRepository

    private val receiverScope = CoroutineScope(Dispatchers.IO)

    companion object {
        private const val TAG = "SmsMonitorReceiver"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        val sender = messages[0].originatingAddress ?: "Unknown"
        val fullBody = messages.joinToString("") { it.messageBody ?: "" }

        VikingLogger.d("Multi-part SMS concatenated (${messages.size} parts) from $sender. Triggering local feature analysis...", TAG)

        val pendingResult = goAsync()
        receiverScope.launch {
            try {
                // Respect the user's SMS Shield toggle from Settings.
                val smsEnabled = repository.moduleStatusMap.first()["SMS"] ?: true
                if (!smsEnabled) return@launch
                // Analysis already raises a HIGH notification for CRITICAL threats.
                // Never abortBroadcast(): swallowing the SMS deletes the user's own
                // evidence message from their inbox.
                analyzeSmsUseCase(sender, fullBody)
            } catch (e: Exception) {
                VikingLogger.e("Failed to analyze incoming SMS", e, TAG)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
