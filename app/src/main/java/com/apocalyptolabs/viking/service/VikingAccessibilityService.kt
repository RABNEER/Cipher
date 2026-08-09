package com.apocalyptolabs.viking.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.view.accessibility.AccessibilityEvent
import com.apocalyptolabs.viking.core.util.VikingLogger
import com.apocalyptolabs.viking.domain.usecase.CheckUpiLinkUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

@AndroidEntryPoint
class VikingAccessibilityService : AccessibilityService() {

    @Inject
    lateinit var checkUpiLinkUseCase: CheckUpiLinkUseCase

    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private val lastScanMap = ConcurrentHashMap<String, Long>()

    companion object {
        private const val TAG = "VikingAccessibility"
        private const val RATE_LIMIT_MS = 30_000L
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100
        }
        this.serviceInfo = info
        VikingLogger.i("VikingAccessibilityService connected and configured.", TAG)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return
            val className = event.className?.toString() ?: ""

            val now = System.currentTimeMillis()
            val lastScan = lastScanMap[packageName] ?: 0L

            if (now - lastScan < RATE_LIMIT_MS) {
                return
            }

            lastScanMap[packageName] = now

            if (className.contains("PackageInstaller", ignoreCase = true) ||
                className.contains("GrantPermissions", ignoreCase = true) ||
                className.contains("Sideload", ignoreCase = true)
            ) {
                VikingLogger.w("Sideloading or permission elevation window detected for $packageName", TAG)
            }
        }
    }

    override fun onInterrupt() {
        VikingLogger.w("Viking Accessibility Service Interrupted", TAG)
    }
}
