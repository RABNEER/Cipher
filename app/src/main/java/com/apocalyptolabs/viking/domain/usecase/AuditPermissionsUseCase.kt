package com.apocalyptolabs.viking.domain.usecase

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.apocalyptolabs.viking.core.ai.PromptBuilder
import com.apocalyptolabs.viking.core.ai.ThreatClassifier
import com.apocalyptolabs.viking.core.model.AppCategory
import com.apocalyptolabs.viking.core.model.Severity
import com.apocalyptolabs.viking.core.model.ThreatResult
import com.apocalyptolabs.viking.core.model.ThreatType
import com.apocalyptolabs.viking.core.util.VikingLogger
import com.apocalyptolabs.viking.data.repository.ThreatRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuditPermissionsUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val classifier: ThreatClassifier,
    private val repository: ThreatRepository
) {
    private val trustedPackagesCache = mutableSetOf<String>()
    private val previouslyFlaggedCache = ConcurrentHashMap<String, String>()

    init {
        loadTrustedPackagesAsset()
    }

    private fun loadTrustedPackagesAsset() {
        try {
            context.assets.open("trusted_packages.txt").bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    val pkg = line.trim()
                    if (pkg.isNotBlank() && !pkg.startsWith("#")) {
                        trustedPackagesCache.add(pkg)
                    }
                }
            }
        } catch (e: Exception) {
            VikingLogger.w("Could not load trusted_packages.txt asset", "AuditPermissionsUseCase")
        }
    }

    suspend operator fun invoke(): List<ThreatResult> {
        val startTime = System.currentTimeMillis()
        val pm = context.packageManager
        @Suppress("DEPRECATION")
        val installedApps = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
        val newSuspiciousResults = mutableListOf<ThreatResult>()

        for (pkg in installedApps) {
            val packageName = pkg.packageName
            val appInfo = pkg.applicationInfo ?: continue

            if ((appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0) continue
            if (packageName.startsWith("com.google.") || packageName.startsWith("com.android.")) continue
            if (trustedPackagesCache.contains(packageName)) continue

            val appName = pm.getApplicationLabel(appInfo).toString()
            val requested = pkg.requestedPermissions?.toList() ?: emptyList()

            @Suppress("DEPRECATION")
            val granted = requested.filter { perm ->
                pm.checkPermission(perm, packageName) == PackageManager.PERMISSION_GRANTED
            }

            val (riskSeverity, riskReason) = evaluateRiskMatrix(granted)
            if (riskSeverity != Severity.SAFE) {
                val fingerprint = "$packageName:${granted.sorted().joinToString(",")}"
                if (previouslyFlaggedCache[packageName] == fingerprint) {
                    continue
                }

                previouslyFlaggedCache[packageName] = fingerprint

                val highRiskList = mutableListOf<String>()
                if (granted.contains("android.permission.READ_SMS") && granted.contains("android.permission.SEND_SMS")) highRiskList.add("READ_SMS+SEND_SMS")
                if (granted.contains("android.permission.RECORD_AUDIO") && granted.contains("android.permission.INTERNET")) highRiskList.add("RECORD_AUDIO+INTERNET")

                val critList = mutableListOf<String>()
                if (granted.contains("android.permission.BIND_ACCESSIBILITY_SERVICE")) critList.add("ACCESSIBILITY")
                if (granted.contains("android.permission.BIND_DEVICE_ADMIN")) critList.add("DEVICE_ADMIN")

                val directReturn = PromptBuilder.checkPermissionDirectReturn(
                    appName = appName,
                    highRiskCombos = highRiskList,
                    criticalCombos = critList
                )

                if (directReturn != null) {
                    repository.logThreat(directReturn, System.currentTimeMillis() - startTime)
                    newSuspiciousResults.add(directReturn)
                    continue
                }

                val prompt = PromptBuilder.permissionPrompt(
                    appName = appName,
                    appCategory = AppCategory.UTILITY,
                    highRiskCombos = highRiskList,
                    criticalCombos = critList,
                    totalDangerousPerms = granted.size,
                    isNewlyInstalled = false
                )

                val result = classifier.classify(prompt, appName, ThreatType.PERMISSION)
                val finalResult = result.copy(
                    target = appName,
                    severity = if (result.severity < riskSeverity) riskSeverity else result.severity,
                    explanation = "$riskReason ($appName). ${result.explanation}",
                    action = "Revoke unnecessary permissions in Android Settings > Apps > $appName."
                )

                repository.logThreat(finalResult, System.currentTimeMillis() - startTime)
                newSuspiciousResults.add(finalResult)
            }
        }

        repository.updateLastAuditTimestamp(System.currentTimeMillis())
        return newSuspiciousResults
    }

    private fun evaluateRiskMatrix(permissions: List<String>): Pair<Severity, String> {
        val permSet = permissions.toSet()

        val hasInternet = permSet.contains("android.permission.INTERNET")
        val hasAccessibility = permSet.contains("android.permission.BIND_ACCESSIBILITY_SERVICE")
        val hasAdmin = permSet.contains("android.permission.BIND_DEVICE_ADMIN")

        val hasReadSms = permSet.contains("android.permission.READ_SMS")
        val hasSendSms = permSet.contains("android.permission.SEND_SMS")

        val hasAudio = permSet.contains("android.permission.RECORD_AUDIO")
        val hasContacts = permSet.contains("android.permission.READ_CONTACTS")
        val hasCallLog = permSet.contains("android.permission.READ_CALL_LOG")

        if (hasAccessibility && hasInternet) {
            return Pair(Severity.CRITICAL, "CRITICAL RISK: Accessibility Service paired with Internet Access")
        }
        if (hasAdmin) {
            return Pair(Severity.CRITICAL, "CRITICAL RISK: Device Administrator Rights Granted")
        }
        if (hasReadSms && hasSendSms) {
            return Pair(Severity.HIGH, "HIGH RISK: SMS Read + Send Combo")
        }
        if (hasAudio && hasInternet) {
            return Pair(Severity.HIGH, "HIGH RISK: Audio Recorder with Internet Access")
        }
        if (hasContacts && hasCallLog && hasInternet) {
            return Pair(Severity.HIGH, "HIGH RISK: Contacts + Call Log + Internet Combo")
        }

        return Pair(Severity.SAFE, "")
    }
}
