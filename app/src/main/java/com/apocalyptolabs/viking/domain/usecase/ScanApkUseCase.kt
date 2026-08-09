package com.apocalyptolabs.viking.domain.usecase

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import com.apocalyptolabs.viking.core.ai.PromptBuilder
import com.apocalyptolabs.viking.core.ai.ThreatClassifier
import com.apocalyptolabs.viking.core.model.Severity
import com.apocalyptolabs.viking.core.model.ThreatResult
import com.apocalyptolabs.viking.core.model.ThreatType
import com.apocalyptolabs.viking.data.repository.ThreatRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScanApkUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val classifier: ThreatClassifier,
    private val repository: ThreatRepository
) {
    private val scanCache = ConcurrentHashMap<String, ThreatResult>()

    private val DANGEROUS_PERMISSIONS = setOf(
        "android.permission.RECORD_AUDIO",
        "android.permission.READ_CONTACTS",
        "android.permission.READ_SMS",
        "android.permission.BIND_ACCESSIBILITY_SERVICE",
        "android.permission.BIND_DEVICE_ADMIN",
        "android.permission.SYSTEM_ALERT_WINDOW",
        "android.permission.READ_CALL_LOG",
        "android.permission.PROCESS_OUTGOING_CALLS"
    )

    suspend operator fun invoke(apkUri: Uri): ThreatResult {
        val startTime = System.currentTimeMillis()
        var tempFile: File? = null
        try {
            val filePath = if (apkUri.scheme == "file") {
                apkUri.path ?: ""
            } else {
                tempFile = createTempFileFromUri(apkUri)
                tempFile.absolutePath
            }

            val pm = context.packageManager
            @Suppress("DEPRECATION")
            val flags = PackageManager.GET_PERMISSIONS or PackageManager.GET_SIGNATURES
            val pkgInfo = pm.getPackageArchiveInfo(filePath, flags)
                ?: throw IllegalArgumentException("Invalid APK file or manifest could not be read.")

            val packageName = pkgInfo.packageName ?: "unknown.pkg"
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pkgInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                pkgInfo.versionCode.toLong()
            }

            @Suppress("DEPRECATION")
            val signatures = pkgInfo.signatures
            val isUnsigned = signatures.isNullOrEmpty()
            val sigHash = if (!isUnsigned) getSignatureHash(signatures[0].toByteArray()) else "UNSIGNED"

            val cacheKey = "$packageName:$versionCode:$sigHash"
            scanCache[cacheKey]?.let { cached ->
                return cached
            }

            if (isUnsigned) {
                val unsignedResult = ThreatResult(
                    target = packageName,
                    type = ThreatType.APK,
                    severity = Severity.HIGH,
                    explanation = "Unsigned APK package detected. Contains no valid developer digital signature.",
                    action = "Do not install unsigned APK packages from untrusted sources."
                )
                repository.logThreat(unsignedResult, System.currentTimeMillis() - startTime)
                scanCache[cacheKey] = unsignedResult
                return unsignedResult
            }

            try {
                val installedInfo = pm.getPackageInfo(packageName, 0)
                val installedVersion = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    installedInfo.longVersionCode
                } else {
                    @Suppress("DEPRECATION")
                    installedInfo.versionCode.toLong()
                }

                if (versionCode < installedVersion) {
                    val downgradeResult = ThreatResult(
                        target = packageName,
                        type = ThreatType.APK,
                        severity = Severity.HIGH,
                        explanation = "Version downgrade attack detected (APK version $versionCode < installed $installedVersion).",
                        action = "Abort installation to prevent app state rollbacks."
                    )
                    repository.logThreat(downgradeResult, System.currentTimeMillis() - startTime)
                    scanCache[cacheKey] = downgradeResult
                    return downgradeResult
                }
            } catch (e: PackageManager.NameNotFoundException) {
            }

            val permissions = pkgInfo.requestedPermissions?.toList() ?: emptyList()
            val dangerousRequested = permissions.filter { it in DANGEROUS_PERMISSIONS }

            val hasInternet = permissions.contains("android.permission.INTERNET")
            val requestsAdmin = permissions.contains("android.permission.BIND_DEVICE_ADMIN")
            val requestsAccessibility = permissions.contains("android.permission.BIND_ACCESSIBILITY_SERVICE")

            val prompt = PromptBuilder.apkPrompt(
                packageName = packageName,
                permissions = dangerousRequested,
                hasInternetAccess = hasInternet,
                requestsAdminRights = requestsAdmin,
                requestsAccessibility = requestsAccessibility,
                installedFromUnknownSource = true,
                isSigned = !isUnsigned,
                versionDowngrade = false,
                dangerousPermCount = dangerousRequested.size
            )

            val result = classifier.classify(prompt, packageName, ThreatType.APK)
            val duration = System.currentTimeMillis() - startTime
            repository.logThreat(result, duration)
            scanCache[cacheKey] = result
            return result

        } catch (e: Exception) {
            val fallback = ThreatResult(
                target = apkUri.lastPathSegment ?: "Unknown APK",
                type = ThreatType.APK,
                severity = Severity.MEDIUM,
                explanation = "Failed to parse APK file: ${e.localizedMessage}",
                action = "Exercise caution before installing this APK package."
            )
            repository.logThreat(fallback)
            return fallback
        } finally {
            tempFile?.delete()
        }
    }

    private fun getSignatureHash(bytes: ByteArray): String {
        val md = MessageDigest.getInstance("SHA-256")
        return md.digest(bytes).joinToString("") { "%02x".format(it) }
    }

    private fun createTempFileFromUri(uri: Uri): File {
        val file = File(context.cacheDir, "temp_scan_${System.currentTimeMillis()}.apk")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        return file
    }
}
