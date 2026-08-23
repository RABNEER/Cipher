package com.apocalyptolabs.viking.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.apocalyptolabs.viking.core.util.VikingLogger

/**
 * Restarts Viking's foreground monitoring shields after a device reboot so the
 * user never silently loses protection.
 */
class VikingBootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "VikingBootReceiver"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        if (!VikingServiceState.isMonitoringActive.value) return

        val appContext = context?.applicationContext ?: return
        for (serviceClass in VikingQuickTile.MONITORED_SERVICES) {
            try {
                val serviceIntent = Intent(appContext, serviceClass)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    appContext.startForegroundService(serviceIntent)
                } else {
                    appContext.startService(serviceIntent)
                }
            } catch (e: Exception) {
                VikingLogger.e("Failed to restart shield after boot: ${serviceClass.simpleName}", e, TAG)
            }
        }
    }
}
