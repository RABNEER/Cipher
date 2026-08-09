package com.apocalyptolabs.viking.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.apocalyptolabs.viking.core.util.VikingLogger
import com.apocalyptolabs.viking.domain.usecase.AuditPermissionsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class PermissionAuditWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val auditPermissionsUseCase: AuditPermissionsUseCase
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "PermissionAuditWorker"
        private const val WORK_NAME = "viking_weekly_permission_audit"
        private const val NOTIFICATION_ID = 2002
        private const val CHANNEL_ID = "viking_audit_channel"

        fun scheduleWeeklyAudit(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()

            val request = PeriodicWorkRequestBuilder<PermissionAuditWorker>(7, TimeUnit.DAYS)
                .setConstraints(constraints)
                .setBackoffCriteria(
                    androidx.work.BackoffPolicy.EXPONENTIAL,
                    30,
                    TimeUnit.MINUTES
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
            VikingLogger.i("Scheduled weekly permission audit worker with BATTERY_NOT_LOW constraint.", TAG)
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        createNotificationChannel()
        val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentTitle("VIKING Permission Audit")
            .setContentText("Auditing application permissions on-device")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(NOTIFICATION_ID, notification)
        }
    }

    override suspend fun doWork(): Result {
        return try {
            if (runAttemptCount > 3) {
                VikingLogger.w("Max retry attempts reached for permission audit worker.", TAG)
                return Result.failure()
            }

            VikingLogger.d("Executing background permission audit work...", TAG)
            val newThreats = auditPermissionsUseCase()
            VikingLogger.i("Background permission audit completed. New suspicious apps flagged: ${newThreats.size}", TAG)

            if (newThreats.isNotEmpty()) {
                postSummaryNotification(newThreats.size)
            }

            Result.success()
        } catch (e: Exception) {
            VikingLogger.e("Failed executing permission audit work", e, TAG)
            Result.retry()
        }
    }

    private fun postSummaryNotification(count: Int) {
        createNotificationChannel()
        val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("VIKING Permission Audit Alert")
            .setContentText("Flagged $count new over-privileged application(s).")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID + 10, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Viking Permission Audit",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
    }
}
