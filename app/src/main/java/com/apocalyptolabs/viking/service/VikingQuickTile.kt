package com.apocalyptolabs.viking.service

import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast
import com.apocalyptolabs.viking.R
import com.apocalyptolabs.viking.core.util.VikingLogger

/**
 * Android Quick Settings Tile for Viking On-Device AI Security Agent.
 */
class VikingQuickTile : TileService() {

    companion object {
        private const val TAG = "VikingQuickTile"
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
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
        VikingLogger.i("Quick Settings tile removed by user. Stopped services.", TAG)
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
            val nfcIntent = Intent(this, NfcMonitorService::class.java)
            val callIntent = Intent(this, CallMonitorService::class.java)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(nfcIntent)
                startForegroundService(callIntent)
            } else {
                startService(nfcIntent)
                startService(callIntent)
            }
        } catch (e: Exception) {
            VikingLogger.e("Failed to start Viking foreground services from QS tile", e, TAG)
        }
    }

    private fun stopForegroundServices() {
        try {
            stopService(Intent(this, NfcMonitorService::class.java))
            stopService(Intent(this, CallMonitorService::class.java))
        } catch (e: Exception) {
            VikingLogger.e("Failed to stop Viking foreground services from QS tile", e, TAG)
        }
    }
}
