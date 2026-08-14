package com.apocalyptolabs.viking.core.ai

import android.content.BroadcastReceiver
import android.content.ComponentCallbacks2
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import com.apocalyptolabs.viking.core.util.VikingLogger
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import javax.inject.Inject
import javax.inject.Singleton

interface InferenceEngine {
    suspend fun infer(prompt: String): String
}

sealed class EngineState {
    object Loading : EngineState()
    object Ready : EngineState()
    data class Error(val message: String) : EngineState()
    object Inferring : EngineState()
    object LowBatteryThrottled : EngineState()
}

@Singleton
open class GemmaEngine @Inject constructor(
    @ApplicationContext private val context: Context
) : ComponentCallbacks2, InferenceEngine {

    private val _engineState = MutableStateFlow<EngineState>(EngineState.Loading)
    val engineState: StateFlow<EngineState> = _engineState.asStateFlow()

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private var llmInference: LlmInference? = null
    private var isBatteryLow = false
    private val initMutex = Mutex()

    private val inferenceDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val nativeExecutor = Executors.newSingleThreadExecutor()
    private val scope = CoroutineScope(inferenceDispatcher + SupervisorJob())

    companion object {
        private const val TAG = "GemmaEngine"
        private const val MODEL_NAME = "gemma-270m-it-cpu-int4.bin"
        private const val MAX_TOKENS = 512
        private const val TEMPERATURE = 0.1f
        private const val INFERENCE_TIMEOUT_MS = 8000L
        private const val EXPECTED_SHA256 = ""
    }

    init {
        context.registerComponentCallbacks(this)
        registerBatteryReceiver()
        scope.launch {
            initModel()
        }
    }

    private fun setState(state: EngineState) {
        _engineState.value = state
        _isReady.value = state is EngineState.Ready || state is EngineState.Inferring
    }

    fun reinitialize() {
        scope.launch {
            VikingLogger.i("Reinitializing Gemma engine on demand.", TAG)
            initModel()
        }
    }

    private fun registerBatteryReceiver() {
        val filter = IntentFilter(Intent.ACTION_BATTERY_LOW).apply {
            addAction(Intent.ACTION_BATTERY_OKAY)
        }
        context.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                when (intent?.action) {
                    Intent.ACTION_BATTERY_LOW -> {
                        isBatteryLow = true
                        setState(EngineState.LowBatteryThrottled)
                        VikingLogger.w("Battery low. Throttling Gemma AI model to static rules engine.", TAG)
                    }
                    Intent.ACTION_BATTERY_OKAY -> {
                        isBatteryLow = false
                        if (llmInference != null) {
                            setState(EngineState.Ready)
                        }
                    }
                }
            }
        }, filter)
    }

    private suspend fun initModel() = withContext(inferenceDispatcher) {
        initMutex.withLock {
            if (_engineState.value is EngineState.Ready || _engineState.value is EngineState.Inferring) return@withLock
            setState(EngineState.Loading)
            try {
                val modelFile = getOrExtractModelFile()
                if (!modelFile.exists() || modelFile.length() == 0L) {
                    val errMsg = "AI model not found on device. Download it from Settings to enable deep analysis."
                    VikingLogger.w(errMsg, TAG)
                    setState(EngineState.Error(errMsg))
                    return@withLock
                }

                if (EXPECTED_SHA256.isNotBlank()) {
                    val checksum = computeSha256(modelFile)
                    if (!checksum.equals(EXPECTED_SHA256, ignoreCase = true)) {
                        val errMsg = "SHA-256 checksum mismatch for model file."
                        VikingLogger.e(errMsg, tag = TAG)
                        setState(EngineState.Error(errMsg))
                        return@withLock
                    }
                }

                val options = LlmInference.LlmInferenceOptions.builder()
                    .setModelPath(modelFile.absolutePath)
                    .setMaxTokens(MAX_TOKENS)
                    .setTemperature(TEMPERATURE)
                    .build()

                llmInference = LlmInference.createFromOptions(context, options)
                setState(EngineState.Ready)
                VikingLogger.i("Gemma 270M loaded and ready.", TAG)

            } catch (e: Exception) {
                VikingLogger.e("Initialization failed for Gemma engine", e, TAG)
                llmInference = null
                setState(EngineState.Error(e.localizedMessage ?: "Engine init error"))
            }
        }
    }

    override suspend fun infer(prompt: String): String = withContext(inferenceDispatcher) {
        if (isBatteryLow) {
            throw IllegalStateException("Low battery throttling active. Operating on static rule engine.")
        }
        if (_isReady.value && _engineState.value !is EngineState.Ready) {
            throw IllegalStateException("GemmaEngine is busy.")
        }
        val engine = llmInference ?: run {
            setState(EngineState.Error("AI model not loaded. Download it from Settings."))
            throw IllegalStateException("LlmInference is null")
        }

        setState(EngineState.Inferring)
        try {
            val future = CompletableFuture.supplyAsync({ engine.generateResponse(prompt).trim() }, nativeExecutor)
            try {
                future.get(INFERENCE_TIMEOUT_MS, TimeUnit.MILLISECONDS)
            } catch (e: ExecutionException) {
                throw e.cause ?: e
            } catch (e: TimeoutException) {
                future.cancel(true)
                VikingLogger.w("Inference exceeded ${INFERENCE_TIMEOUT_MS}ms budget.", TAG)
                throw IllegalStateException("Inference timed out after ${INFERENCE_TIMEOUT_MS}ms.")
            }
        } finally {
            if (llmInference != null) setState(EngineState.Ready)
        }
    }

    private fun getOrExtractModelFile(): File {
        val destFile = File(context.filesDir, MODEL_NAME)
        if (destFile.exists() && destFile.length() > 0) {
            return destFile
        }

        try {
            context.assets.open(MODEL_NAME).use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: Exception) {
            VikingLogger.d("Asset $MODEL_NAME not present in bundle.", TAG)
        }
        return destFile
    }

    private fun computeSha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { fis ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    override fun onTrimMemory(level: Int) {
        if (level >= ComponentCallbacks2.TRIM_MEMORY_COMPLETE) {
            VikingLogger.w("Critical memory trim event received. Releasing Gemma model instance.", TAG)
            scope.launch {
                llmInference = null
                setState(EngineState.Loading)
                initModel()
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {}
    override fun onLowMemory() {
        onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_COMPLETE)
    }
}
