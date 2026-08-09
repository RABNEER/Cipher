package com.apocalyptolabs.viking.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.apocalyptolabs.viking.core.model.Severity
import com.apocalyptolabs.viking.core.util.VikingLogger
import com.apocalyptolabs.viking.domain.usecase.AnalyzeSmsUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SmsMonitorReceiver : BroadcastReceiver() {

    @Inject
    lateinit var analyzeSmsUseCase: AnalyzeSmsUseCase

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
                val result = analyzeSmsUseCase(sender, fullBody)
                if (result.severity == Severity.CRITICAL && isOrderedBroadcast) {
                    VikingLogger.w("CRITICAL threat detected on incoming SMS. Aborting broadcast.", TAG)
                    abortBroadcast()
                }
            } catch (e: Exception) {
                VikingLogger.e("Failed to analyze incoming SMS", e, TAG)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
