package com.apocalyptolabs.viking.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.FileObserver
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.apocalyptolabs.viking.core.model.Severity
import com.apocalyptolabs.viking.core.util.VikingLogger
import com.apocalyptolabs.viking.domain.usecase.ScanApkUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class MediaSideloadObserverService : Service() {

    @Inject
    lateinit var scanApkUseCase: ScanApkUseCase

    @Inject
    lateinit var threatRepository: com.apocalyptolabs.viking.data.repository.ThreatRepository

    private val serviceScope = CoroutineScope(Dispatchers.IO + kotlinx.coroutines.SupervisorJob())
    private var fileObserver: FileObserver? = null

    companion object {
        private const val TAG = "MediaSideloadObserver"
        private const val NOTIFICATION_ID = 4001
        private const val CHANNEL_ID = "viking_sideload_channel"
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, createNotification("Monitoring downloads folder"))
        startWatchingMediaFolders()
    }

    private fun startWatchingMediaFolders() {
        val downloadsDir = File("/sdcard/Download")
        if (!downloadsDir.exists()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            fileObserver = object : FileObserver(downloadsDir, CREATE or CLOSE_WRITE) {
                override fun onEvent(event: Int, path: String?) {
                    if (path != null && path.endsWith(".apk", ignoreCase = true)) {
                        VikingLogger.w("New APK detected in Downloads: $path", TAG)
                        val apkFile = File(downloadsDir, path)
                        scanDetectedApk(apkFile)
                    }
                }
            }
        } else {
            @Suppress("DEPRECATION")
            fileObserver = object : FileObserver(downloadsDir.absolutePath, CREATE or CLOSE_WRITE) {
                override fun onEvent(event: Int, path: String?) {
                    if (path != null && path.endsWith(".apk", ignoreCase = true)) {
                        VikingLogger.w("New APK detected in Downloads: $path", TAG)
                        val apkFile = File(downloadsDir, path)
                        scanDetectedApk(apkFile)
                    }
                }
            }
        }
        fileObserver?.startWatching()
        VikingLogger.i("MediaSideloadObserverService watching Downloads directory.", TAG)
    }

    private fun scanDetectedApk(file: File) {
        serviceScope.launch {
            try {
                val apkScanEnabled = threatRepository.moduleStatusMap.first()["APK"] ?: true
                if (!apkScanEnabled) return@launch
                val uri = Uri.fromFile(file)
                val result = scanApkUseCase(uri)
                if (result.severity == Severity.HIGH || result.severity == Severity.CRITICAL) {
                    postSideloadAlertNotification(file.name, result.explanation)
                }
            } catch (e: Exception) {
                VikingLogger.e("Failed scanning sideloaded APK", e, TAG)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fileObserver?.stopWatching()
        fileObserver = null
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification(text: String) = NotificationCompat.Builder(this, CHANNEL_ID).apply {
        createChannel()
        setSmallIcon(android.R.drawable.ic_dialog_alert)
        setContentTitle("CIPHER Sideload Shield Active")
        setContentText(text)
        setPriority(NotificationCompat.PRIORITY_LOW)
        setOngoing(true)
    }.build()

    private fun postSideloadAlertNotification(fileName: String, explanation: String) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("CIPHER Sideload Alert: DANGEROUS APK")
            .setContentText("Downloaded $fileName contains high risk threat signatures.")
            .setStyle(NotificationCompat.BigTextStyle().bigText("File: $fileName\n$explanation\nDo not install this package."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        nm.notify(NOTIFICATION_ID + 20, notification)
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(CHANNEL_ID, "Cipher Sideload Shield", NotificationManager.IMPORTANCE_LOW)
            nm.createNotificationChannel(channel)
        }
    }
}
