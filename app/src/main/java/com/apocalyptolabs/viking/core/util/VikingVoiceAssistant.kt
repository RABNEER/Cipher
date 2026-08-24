package com.apocalyptolabs.viking.core.util

import android.content.Context
import android.speech.tts.TextToSpeech
import com.apocalyptolabs.viking.core.model.Severity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VikingVoiceAssistant @Inject constructor(
    @ApplicationContext private val context: Context
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val hindiResult = tts?.setLanguage(Locale("hi", "IN"))
            if (hindiResult == TextToSpeech.LANG_MISSING_DATA || hindiResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.language = Locale.ENGLISH
            }
            isInitialized = true
            VikingLogger.i("VikingVoiceAssistant TextToSpeech initialized.", "VikingVoice")
        } else {
            VikingLogger.w("TextToSpeech initialization failed.", "VikingVoice")
        }
    }

    fun speakThreatAlert(severity: Severity, target: String, languageCode: String = "hi") {
        if (!isInitialized || tts == null) return

        val locale = when (languageCode) {
            "hi" -> Locale("hi", "IN")
            "ta" -> Locale("ta", "IN")
            "te" -> Locale("te", "IN")
            "mr" -> Locale("mr", "IN")
            "bn" -> Locale("bn", "IN")
            else -> Locale.ENGLISH
        }

        tts?.language = locale

        val spokenMessage = when (severity) {
            Severity.CRITICAL -> when (languageCode) {
                "hi" -> "सावधान! गंभीर खतरा पाया गया है। कृपया इस लिंक या ऐप को तुरंत बंद करें।"
                "ta" -> "எச்சரிக்கை! கடுமையான அச்சுறுத்தல் கண்டறியப்பட்டது."
                else -> "Warning! Critical security threat detected. Exercise caution immediately."
            }
            Severity.HIGH -> when (languageCode) {
                "hi" -> "चेतावनी! संदिग्ध धोखाधड़ी का प्रयास पहचाना गया है।"
                else -> "Caution! Suspicious scam pattern detected on device."
            }
            else -> "Cipher: Device threat scan completed."
        }

        tts?.speak(spokenMessage, TextToSpeech.QUEUE_FLUSH, null, "CipherAlertId")
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
