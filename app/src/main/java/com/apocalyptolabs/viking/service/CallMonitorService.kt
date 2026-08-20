package com.apocalyptolabs.viking.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import timber.log.Timber
import androidx.core.app.NotificationCompat
import com.apocalyptolabs.viking.domain.usecase.FingerprintCallUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CallMonitorService : Service() {

    @Inject
    lateinit var fingerprintCallUseCase: FingerprintCallUseCase

    @Inject
    lateinit var threatRepository: com.apocalyptolabs.viking.data.repository.ThreatRepository

    private val serviceScope = CoroutineScope(Dispatchers.IO + kotlinx.coroutines.SupervisorJob())

    companion object {
        private const val TAG = "CallMonitorService"
        private const val NOTIFICATION_ID = 1002
        private const val CHANNEL_ID = "viking_call_service_channel"

        const val ACTION_ANALYZE_CALL = "com.apocalyptolabs.viking.ANALYZE_CALL"
        const val EXTRA_CALLER_NUMBER = "extra_caller_number"
        const val EXTRA_CALL_DURATION = "extra_call_duration"
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_ANALYZE_CALL) {
            val number = intent.getStringExtra(EXTRA_CALLER_NUMBER) ?: "Unknown"
            val duration = intent.getIntExtra(EXTRA_CALL_DURATION, 0)

            serviceScope.launch {
                try {
                    val callEnabled = threatRepository.moduleStatusMap.first()["CALL"] ?: true
                    if (!callEnabled) return@launch
                    fingerprintCallUseCase(number, duration)
                } catch (e: Exception) {
                    Timber.e(e, "Error fingerprinting call metadata")
                }
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification(): Notification {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Cipher Call Guard Active",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setContentTitle("CIPHER Call Guard Active")
            .setContentText("Monitoring call metadata for scam signatures")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }
}
