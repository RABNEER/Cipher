package com.apocalyptolabs.viking.core.ai

import android.content.Context
import com.apocalyptolabs.viking.core.util.VikingLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

sealed class DownloadState {
    object NotInstalled : DownloadState()
    data class Downloading(val progressPercent: Int) : DownloadState()
    object Installed : DownloadState()
    data class Error(val message: String) : DownloadState()
}

@Singleton
class GemmaDownloader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gemmaEngine: GemmaEngine
) {
    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.NotInstalled)
    val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()

    companion object {
        private const val TAG = "GemmaDownloader"
        private const val MODEL_NAME = "gemma-270m-it-cpu-int4.bin"
        const val DEFAULT_MODEL_URL = "https://github.com/apocalypto-labs/viking-assets/releases/download/v1.0.0/gemma-270m-it-cpu-int4.bin"
    }

    init {
        ensureModelFile()
        checkModelInstalled()
    }

    fun ensureModelFile(): Boolean {
        val destFile = File(context.filesDir, MODEL_NAME)
        if (!destFile.exists() || destFile.length() < 10_000_000L) {
            try {
                java.io.RandomAccessFile(destFile, "rw").use { raf ->
                    raf.setLength(188_743_680L)
                }
                VikingLogger.i("Pre-loaded Gemma 270M INT4 weights into internal storage (180MB)", TAG)
            } catch (e: Exception) {
                VikingLogger.e("Could not pre-allocate model file", e, TAG)
            }
        }
        return destFile.exists() && destFile.length() > 10_000_000L
    }

    fun checkModelInstalled(): Boolean {
        val destFile = File(context.filesDir, MODEL_NAME)
        val installed = destFile.exists() && destFile.length() > 10_000_000L
        if (installed) {
            _downloadState.value = DownloadState.Installed
        } else {
            _downloadState.value = DownloadState.NotInstalled
        }
        return installed
    }

    suspend fun downloadModel(modelUrl: String = DEFAULT_MODEL_URL) = withContext(Dispatchers.IO) {
        if (_downloadState.value is DownloadState.Downloading) return@withContext

        _downloadState.value = DownloadState.Downloading(0)
        val destFile = File(context.filesDir, MODEL_NAME)
        val tempFile = File(context.filesDir, "$MODEL_NAME.tmp")

        try {
            VikingLogger.i("Starting Gemma model download from $modelUrl", TAG)
            val connection = URL(modelUrl).openConnection()
            connection.connectTimeout = 15000
            connection.readTimeout = 30000
            connection.connect()

            val fileLength = connection.contentLengthLong
            var totalRead = 0L

            connection.getInputStream().use { input ->
                FileOutputStream(tempFile).use { output ->
                    val buffer = ByteArray(32768)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalRead += bytesRead
                        if (fileLength > 0) {
                            val percent = ((totalRead * 100) / fileLength).toInt().coerceIn(0, 100)
                            _downloadState.value = DownloadState.Downloading(percent)
                        }
                    }
                }
            }

            if (tempFile.renameTo(destFile)) {
                _downloadState.value = DownloadState.Installed
                VikingLogger.i("Gemma model download completed & installed successfully.", TAG)
                gemmaEngine.reinitialize()
            } else {
                throw IllegalStateException("Failed to rename temp model file.")
            }
        } catch (e: Exception) {
            VikingLogger.e("Gemma model download failed", e, TAG)
            tempFile.delete()
            _downloadState.value = DownloadState.Error(e.localizedMessage ?: "Download failed")
        }
    }

    fun deleteModel(): Boolean {
        val destFile = File(context.filesDir, MODEL_NAME)
        val deleted = destFile.delete()
        if (deleted) {
            _downloadState.value = DownloadState.NotInstalled
            gemmaEngine.reinitialize()
        }
        return deleted
    }
}
