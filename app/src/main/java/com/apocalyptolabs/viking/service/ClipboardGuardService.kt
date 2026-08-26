package com.apocalyptolabs.viking.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.apocalyptolabs.viking.core.model.Severity
import com.apocalyptolabs.viking.core.util.VikingLogger
import com.apocalyptolabs.viking.domain.usecase.CheckUpiLinkUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ClipboardGuardService : Service(), ClipboardManager.OnPrimaryClipChangedListener {

    @Inject
    lateinit var checkUpiLinkUseCase: CheckUpiLinkUseCase

    @Inject
    lateinit var threatRepository: com.apocalyptolabs.viking.data.repository.ThreatRepository

    private val serviceScope = CoroutineScope(Dispatchers.IO + kotlinx.coroutines.SupervisorJob())
    private var clipboardManager: ClipboardManager? = null

    companion object {
        private const val TAG = "ClipboardGuardService"
        private const val NOTIFICATION_ID = 3001
        private const val CHANNEL_ID = "viking_clipboard_channel"
    }

    override fun onCreate() {
        super.onCreate()
        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboardManager?.addPrimaryClipChangedListener(this)
        startForeground(NOTIFICATION_ID, createNotification("Monitoring clipboard for phishing links"))
        VikingLogger.i("ClipboardGuardService registered.", TAG)
    }

    override fun onPrimaryClipChanged() {
        val clipData = clipboardManager?.primaryClip ?: return
        if (clipData.itemCount == 0) return

        val text = clipData.getItemAt(0).text?.toString() ?: return
        if (text.length > 500) return

        if (text.startsWith("http://") || text.startsWith("https://") || text.contains("@") || text.contains("upi://")) {
            VikingLogger.d("Clipboard URL/UPI detected: $text", TAG)
            serviceScope.launch {
                try {
                    val upiEnabled = threatRepository.moduleStatusMap.first()["UPI"] ?: true
                    if (!upiEnabled) return@launch
                    val result = checkUpiLinkUseCase(text)
                    if (result.severity == Severity.HIGH || result.severity == Severity.CRITICAL) {
                        postClipboardAlertNotification(result.explanation, result.action)
                    }
                } catch (e: Exception) {
                    VikingLogger.e("Clipboard evaluation error", e, TAG)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        clipboardManager?.removePrimaryClipChangedListener(this)
        serviceScope.cancel()
        VikingLogger.i("ClipboardGuardService destroyed.", TAG)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification(text: String) = NotificationCompat.Builder(this, CHANNEL_ID).apply {
        createChannel()
        setSmallIcon(com.apocalyptolabs.viking.R.drawable.ic_viking_shield)
        setContentTitle("VIKING Clipboard Guard")
        setContentText(text)
        setPriority(NotificationCompat.PRIORITY_LOW)
        setOngoing(true)
    }.build()

    private fun postClipboardAlertNotification(explanation: String, action: String) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("VIKING Copied Link Alert: PHISHING")
            .setContentText(explanation)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$explanation\nRecommended Action: $action"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        nm.notify(NOTIFICATION_ID + 10, notification)
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(CHANNEL_ID, "Viking Clipboard Guard", NotificationManager.IMPORTANCE_LOW)
            nm.createNotificationChannel(channel)
        }
    }
}
