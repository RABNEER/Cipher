package com.apocalyptolabs.viking.service

import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast
import com.apocalyptolabs.viking.R
import com.apocalyptolabs.viking.core.util.VikingLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch

class VikingQuickTile : TileService() {

    private val tileScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    companion object {
        private const val TAG = "VikingQuickTile"

        val MONITORED_SERVICES = listOf(
            NfcMonitorService::class.java,
            CallMonitorService::class.java,
            ClipboardGuardService::class.java,
            MediaSideloadObserverService::class.java
        )
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
        tileScope.launch {
            VikingServiceState.blockedThreatCount.collect {
                qsTile ?: return@collect
                updateTileState()
            }
        }
    }

    override fun onStopListening() {
        super.onStopListening()
        tileScope.coroutineContext.cancelChildren()
    }

    override fun onDestroy() {
        super.onDestroy()
        tileScope.cancel()
    }

    override fun onClick() {
        super.onClick()
        val isActive = VikingServiceState.isMonitoringActive.value
        val newState = !isActive

        VikingServiceState.setMonitoringActive(newState)

        if (newState) {
            startForegroundServices()
        } else {
            stopForegroundServices()
        }

        updateTileState()
    }

    override fun onTileAdded() {
        super.onTileAdded()
        Toast.makeText(this, "Viking is now in your Quick Settings", Toast.LENGTH_SHORT).show()
        VikingLogger.i("Quick Settings tile added by user.", TAG)
    }

    override fun onTileRemoved() {
        super.onTileRemoved()
        stopForegroundServices()
        VikingLogger.i("Quick Settings tile removed by user.", TAG)
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        val isActive = VikingServiceState.isMonitoringActive.value
        val blockedCount = VikingServiceState.blockedThreatCount.value

        if (isActive) {
            tile.state = Tile.STATE_ACTIVE
            tile.label = "Viking"
            tile.icon = Icon.createWithResource(this, R.drawable.ic_viking_shield)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.subtitle = if (blockedCount > 0) "$blockedCount threats blocked" else "All clear"
            }
        } else {
            tile.state = Tile.STATE_INACTIVE
            tile.label = "Viking Off"
            tile.icon = Icon.createWithResource(this, R.drawable.ic_viking_shield)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.subtitle = "Protection paused"
            }
        }

        tile.updateTile()
    }

    private fun startForegroundServices() {
        try {
            for (serviceClass in MONITORED_SERVICES) {
                val intent = Intent(this, serviceClass)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent)
                } else {
                    startService(intent)
                }
            }
        } catch (e: Exception) {
            VikingLogger.e("Failed to start Viking foreground services from QS tile", e, TAG)
        }
    }

    private fun stopForegroundServices() {
        try {
            for (serviceClass in MONITORED_SERVICES) {
                stopService(Intent(this, serviceClass))
            }
        } catch (e: Exception) {
            VikingLogger.e("Failed to stop Viking foreground services from QS tile", e, TAG)
        }
    }
}
