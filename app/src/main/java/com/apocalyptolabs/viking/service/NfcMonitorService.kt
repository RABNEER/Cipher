package com.apocalyptolabs.viking.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.apocalyptolabs.viking.core.util.VikingLogger
import com.apocalyptolabs.viking.domain.usecase.MonitorNfcUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NfcMonitorService : Service() {

    @Inject
    lateinit var monitorNfcUseCase: MonitorNfcUseCase

    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private var nfcAdapter: NfcAdapter? = null

    companion object {
        private const val TAG = "NfcMonitorService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "viking_nfc_service_channel"

        const val ACTION_ANALYZE_NFC = "com.apocalyptolabs.viking.ANALYZE_NFC"
        const val EXTRA_TAG_TYPE = "extra_tag_type"
        const val EXTRA_DATA_SIZE = "extra_data_size"
        const val EXTRA_PAYLOAD_URL = "extra_payload_url"
        const val EXTRA_LATENCY_MS = "extra_latency_ms"
    }

    override fun onCreate() {
        super.onCreate()
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        startForeground(NOTIFICATION_ID, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (nfcAdapter == null || !nfcAdapter!!.isEnabled) {
            VikingLogger.w("NFC Adapter disabled mid-session", TAG)
        }

        if (intent?.action == ACTION_ANALYZE_NFC) {
            val tagType = intent.getStringExtra(EXTRA_TAG_TYPE) ?: "IsoDep"
            val dataSize = intent.getIntExtra(EXTRA_DATA_SIZE, 0)
            val payloadUrl = intent.getStringExtra(EXTRA_PAYLOAD_URL)
            val latency = intent.getLongExtra(EXTRA_LATENCY_MS, 0L)

            serviceScope.launch {
                try {
                    monitorNfcUseCase(tagType, dataSize, payloadUrl, true, latency)
                } catch (e: Exception) {
                    VikingLogger.e("Error analyzing NFC event", e, TAG)
                }
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        nfcAdapter = null
        VikingLogger.i("NfcMonitorService destroyed and callback references released.", TAG)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification(): Notification {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Viking NFC Active Monitor",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Monitors active NFC tag interactions for relay attacks & payloads"
            }
            notificationManager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("VIKING NFC Guard Active")
            .setContentText("Monitoring NFC tag interactions on-device")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .build()
    }
}
