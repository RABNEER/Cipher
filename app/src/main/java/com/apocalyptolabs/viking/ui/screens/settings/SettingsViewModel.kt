package com.apocalyptolabs.viking.ui.screens.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apocalyptolabs.viking.core.ai.DownloadState
import com.apocalyptolabs.viking.core.ai.GemmaDownloader
import com.apocalyptolabs.viking.core.util.VikingLogger
import com.apocalyptolabs.viking.data.repository.ThreatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: ThreatRepository,
    private val gemmaDownloader: GemmaDownloader
) : ViewModel() {

    val downloadState: StateFlow<DownloadState> = gemmaDownloader.downloadState

    val moduleStatus: StateFlow<Map<String, Boolean>> = repository.moduleStatusMap
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = mapOf(
                "APK" to true, "UPI" to true, "SMS" to true,
                "CALL" to true, "NFC" to true, "PERM" to true
            )
        )

    fun startModelDownload() {
        viewModelScope.launch {
            gemmaDownloader.downloadModel()
        }
    }

    fun deleteModel() {
        gemmaDownloader.deleteModel()
    }

    fun toggleModule(moduleKey: String, enabled: Boolean) {
        viewModelScope.launch {
            repository.toggleModule(moduleKey, enabled)
        }
    }

    fun exportDiagnosticReport(onIntentReady: (Intent) -> Unit) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val zipFile = File(context.cacheDir, "cipher_diagnostic_report.zip")
                ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
                    val deviceInfo = """
                        CIPHER Security Agent Diagnostic Report
                        ----------------------------------------
                        App Version: 1.0.0
                        Device Model: ${Build.MODEL} (${Build.MANUFACTURER})
                        Android Version: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})
                        Timestamp: ${System.currentTimeMillis()}
                    """.trimIndent()
                    zos.putNextEntry(ZipEntry("device_info.txt"))
                    zos.write(deviceInfo.toByteArray())
                    zos.closeEntry()

                    val logsDir = File(context.filesDir, "logs")
                    if (logsDir.exists() && logsDir.isDirectory) {
                        logsDir.listFiles()?.forEach { logFile ->
                            zos.putNextEntry(ZipEntry("logs/${logFile.name}"))
                            logFile.inputStream().use { input -> input.copyTo(zos) }
                            zos.closeEntry()
                        }
                    }
                }

                val uri: Uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    zipFile
                )

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/zip"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "CIPHER Security Agent Diagnostic Report")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                onIntentReady(Intent.createChooser(intent, "Share Diagnostic Report"))
            } catch (e: Exception) {
                VikingLogger.e("Failed to generate diagnostic report", e, "SettingsViewModel")
            }
        }
    }

    fun generateCertificatePdf(onReady: (java.io.File?) -> Unit) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val file = try {
                com.apocalyptolabs.viking.core.util.PdfReportGenerator.generateSecurityCertificatePdf(
                    context, emptyList(), 0
                )
            } catch (_: Exception) {
                null
            }
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                onReady(file)
            }
        }
    }
}
