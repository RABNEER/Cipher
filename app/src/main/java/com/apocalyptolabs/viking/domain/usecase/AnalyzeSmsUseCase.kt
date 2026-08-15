package com.apocalyptolabs.viking.domain.usecase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.apocalyptolabs.viking.core.ai.PromptBuilder
import com.apocalyptolabs.viking.core.ai.ThreatClassifier
import com.apocalyptolabs.viking.core.model.SenderType
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
class AnalyzeSmsUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val classifier: ThreatClassifier,
    private val repository: ThreatRepository
) {
    private data class UrgencyEntry(val phrase: String, val weight: Int)

    private val urgencyEntries = mutableListOf<UrgencyEntry>()
    private val lastNotificationMap = ConcurrentHashMap<String, Long>()

    // DLT transactional headers are always UPPERCASE (e.g. XX-HDFCBK).
    // A lowercase spoofed header must never be whitelisted blindly.
    private val BANK_SHORTCODE_REGEX = Regex("^[A-Z]{2}-[A-Z0-9]{6}$")
    private val OTP_REGEX = Regex("\\b\\d{4,8}\\b")
    private val UPI_ID_REGEX = Regex("[a-zA-Z0-9.\\-_]{2,256}@[a-zA-Z]{2,64}")
    private val URL_REGEX = Regex("https?://\\S+|www\\.\\S+")

    private val GOVT_SCHEME_KEYWORDS = listOf(
        "pm kisan", "ayushman", "dbt", "scholarship", "kisan yojana", "aadhaar link",
        "pan card update", "ration card", "ujjwala", "jan dhan"
    )

    init {
        loadUrgencyWordsAsset()
    }

    private fun loadUrgencyWordsAsset() {
        try {
            context.assets.open("urgency_words.txt").bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    if (line.isBlank() || line.trimStart().startsWith("#")) return@forEach
                    val parts = line.split(",")
                    if (parts.isNotEmpty()) {
                        val phrase = parts[0].trim().lowercase()
                        val weight = parts.getOrNull(2)?.trim()?.toIntOrNull() ?: 1
                        if (phrase.isNotBlank()) {
                            urgencyEntries.add(UrgencyEntry(phrase, weight.coerceIn(1, 10)))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            VikingLogger.w("Could not load urgency_words.txt asset", "AnalyzeSmsUseCase")
        }
    }

    private fun computeUrgencyScore(messageBody: String): Int {
        val lowerBody = messageBody.lowercase()
        var score = 0
        for ((phrase, weight) in urgencyEntries) {
            val matched = if (phrase.contains(' ')) {
                lowerBody.contains(phrase)
            } else {
                lowerBody.split("\\s+".toRegex()).contains(phrase)
            }
            if (matched) score += weight
        }
        return score.coerceAtMost(10)
    }

    suspend operator fun invoke(sender: String, messageBody: String): ThreatResult {
        val startTime = System.currentTimeMillis()

        val isBankShortCode = BANK_SHORTCODE_REGEX.matches(sender.trim())
        val senderType = when {
            isBankShortCode -> SenderType.BANK_SHORTCODE
            sender.startsWith("140") -> SenderType.TELECOM
            sender.startsWith("+") && !sender.startsWith("+91") -> SenderType.INTERNATIONAL
            else -> SenderType.UNKNOWN_NUMBER
        }

        val directReturn = PromptBuilder.checkSmsDirectReturn(
            senderType = senderType,
            sender = sender
        )
        if (directReturn != null) {
            safeLog(directReturn, System.currentTimeMillis() - startTime)
            return directReturn
        }

        val hasUrl = URL_REGEX.containsMatchIn(messageBody)
        val hasUpiId = UPI_ID_REGEX.containsMatchIn(messageBody)
        val hasOtp = OTP_REGEX.containsMatchIn(messageBody)
        val urgencyScore = computeUrgencyScore(messageBody)
        val languageDetected = detectLanguage(messageBody)

        val extractedFeatures = mutableListOf<String>()
        if (hasUrl) extractedFeatures.add("CONTAINS_URL")
        if (hasUpiId) extractedFeatures.add("CONTAINS_UPI_ID")
        if (hasOtp) extractedFeatures.add("CONTAINS_OTP_PATTERN")
        if (sender.startsWith("140")) extractedFeatures.add("TELEMARKETER_PREFIX_140")
        if (urgencyScore > 0) extractedFeatures.add("URGENCY_KEYWORDS_SCORE_$urgencyScore")

        val prompt = PromptBuilder.smsPrompt(
            senderType = senderType,
            hasUrl = hasUrl,
            hasUpiId = hasUpiId,
            hasOtpPattern = hasOtp,
            urgencyScore = urgencyScore,
            languageDetected = languageDetected,
            mentionsBankName = messageBody.contains("bank", ignoreCase = true) ||
                listOf("sbi", "hdfc", "icici", "axis", "kotak", "pnb").any { messageBody.contains(it, ignoreCase = true) },
            mentionsGovtScheme = GOVT_SCHEME_KEYWORDS.any { messageBody.lowercase().contains(it) },
            featureCount = extractedFeatures.size
        )

        val result = classifier.classify(prompt, "SMS from $sender", ThreatType.SMS)
        val duration = System.currentTimeMillis() - startTime
        safeLog(result, duration)

        val fingerprint = "$sender:${extractedFeatures.joinToString(",")}"
        val lastTime = lastNotificationMap[fingerprint] ?: 0L

        if ((result.severity == Severity.HIGH || result.severity == Severity.CRITICAL) &&
            (startTime - lastTime > 60_000L)
        ) {
            lastNotificationMap[fingerprint] = startTime
            postThreatNotification(result)
        }

        return result
    }

    private fun detectLanguage(text: String): String {
        return if (text.any { it.code in 0x0900..0x097F }) "hi" else "en"
    }

    private suspend fun safeLog(threat: ThreatResult, duration: Long) {
        try {
            repository.logThreat(threat, duration)
        } catch (_: Throwable) {
        }
    }

    private fun postThreatNotification(result: ThreatResult) {
        val channelId = "viking_threat_alerts"
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Cipher Threat Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Real-time security notifications for detected threats"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("CIPHER Security Alert: ${result.severity.name}")
            .setContentText(result.explanation)
            .setStyle(NotificationCompat.BigTextStyle().bigText("${result.explanation}\nAction: ${result.action}"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
