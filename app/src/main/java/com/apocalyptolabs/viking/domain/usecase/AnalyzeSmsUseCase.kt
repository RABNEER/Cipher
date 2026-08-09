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
    private val urgencyWordsCache = mutableSetOf<String>()
    private val lastNotificationMap = ConcurrentHashMap<String, Long>()

    private val BANK_SHORTCODE_REGEX = Regex("^[A-Z]{2}-[A-Z0-9]{6}$", RegexOption.IGNORE_CASE)
    private val OTP_REGEX = Regex("\\b\\d{4,8}\\b")
    private val UPI_ID_REGEX = Regex("[a-zA-Z0-9.\\-_]{2,256}@[a-zA-Z]{2,64}")
    private val URL_REGEX = Regex("https?://\\S+|www\\.\\S+")

    init {
        loadUrgencyWordsAsset()
    }

    private fun loadUrgencyWordsAsset() {
        try {
            context.assets.open("urgency_words.txt").bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    val parts = line.split(",")
                    if (parts.isNotEmpty()) {
                        val word = parts[0].trim().lowercase()
                        if (word.isNotBlank() && !word.startsWith("#")) {
                            urgencyWordsCache.add(word)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            VikingLogger.w("Could not load urgency_words.txt asset", "AnalyzeSmsUseCase")
        }
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
            repository.logThreat(directReturn, System.currentTimeMillis() - startTime)
            return directReturn
        }

        val hasUrl = URL_REGEX.containsMatchIn(messageBody)
        val hasUpiId = UPI_ID_REGEX.containsMatchIn(messageBody)
        val hasOtp = OTP_REGEX.containsMatchIn(messageBody)

        val words = messageBody.lowercase().split("\\s+".toRegex())
        val urgencyScore = words.count { urgencyWordsCache.contains(it) }.coerceAtMost(10)

        val extractedFeatures = mutableListOf<String>()
        if (hasUrl) extractedFeatures.add("CONTAINS_URL")
        if (hasUpiId) extractedFeatures.add("CONTAINS_UPI_ID")
        if (hasOtp) extractedFeatures.add("CONTAINS_OTP_PATTERN")
        if (sender.startsWith("140")) extractedFeatures.add("TELEMARKETER_PREFIX_140")
        if (urgencyScore > 0) extractedFeatures.add("URGENCY_KEYWORDS_COUNT_$urgencyScore")

        val prompt = PromptBuilder.smsPrompt(
            senderType = senderType,
            hasUrl = hasUrl,
            hasUpiId = hasUpiId,
            hasOtpPattern = hasOtp,
            urgencyScore = urgencyScore,
            languageDetected = "hi",
            mentionsBankName = messageBody.contains("bank", ignoreCase = true) || messageBody.contains("sbi", ignoreCase = true),
            mentionsGovtScheme = false,
            featureCount = extractedFeatures.size
        )

        val result = classifier.classify(prompt, "SMS from $sender", ThreatType.SMS)
        val duration = System.currentTimeMillis() - startTime
        try {
            repository.logThreat(result, duration)
        } catch (e: Throwable) {}

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

    private fun postThreatNotification(result: ThreatResult) {
        val channelId = "viking_threat_alerts"
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Viking Threat Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Real-time security notifications for detected threats"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(com.apocalyptolabs.viking.R.drawable.ic_viking_shield)
            .setContentTitle("VIKING Security Alert: ${result.severity.name}")
            .setContentText(result.explanation)
            .setStyle(NotificationCompat.BigTextStyle().bigText("${result.explanation}\nAction: ${result.action}"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
