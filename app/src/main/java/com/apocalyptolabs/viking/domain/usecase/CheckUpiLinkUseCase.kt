package com.apocalyptolabs.viking.domain.usecase

import android.content.Context
import android.net.Uri
import com.apocalyptolabs.viking.core.ai.PromptBuilder
import com.apocalyptolabs.viking.core.ai.ThreatClassifier
import com.apocalyptolabs.viking.core.model.Severity
import com.apocalyptolabs.viking.core.model.ThreatResult
import com.apocalyptolabs.viking.core.model.ThreatType
import com.apocalyptolabs.viking.core.util.VikingLogger
import com.apocalyptolabs.viking.data.repository.ThreatRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CheckUpiLinkUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val classifier: ThreatClassifier,
    private val repository: ThreatRepository
) {
    private val scamDomainsCache = mutableSetOf<String>()

    private val KNOWN_SHORTENERS = setOf(
        "bit.ly", "tinyurl.com", "t.co", "is.gd", "buff.ly", "ow.ly", "rebrand.ly", "cutt.ly"
    )

    private val UPI_KEYWORDS = listOf("upi", "pay", "collect", "refund", "cashback", "gpay", "phonepe", "paytm", "bhim")

    init {
        loadScamDomainsAsset()
    }

    private fun loadScamDomainsAsset() {
        try {
            context.assets.open("scam_domains.txt").bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    val domain = line.trim().lowercase()
                    if (domain.isNotBlank() && !domain.startsWith("#")) {
                        scamDomainsCache.add(domain)
                    }
                }
            }
        } catch (e: Exception) {
            VikingLogger.w("Could not load scam_domains.txt asset", "CheckUpiLinkUseCase")
        }
    }

    suspend operator fun invoke(rawUrl: String): ThreatResult {
        val startTime = System.currentTimeMillis()
        val formattedUrl = if (!rawUrl.startsWith("http://") && !rawUrl.startsWith("https://")) {
            "https://$rawUrl"
        } else rawUrl

        val uri = try {
            Uri.parse(formattedUrl)
        } catch (e: Exception) {
            null
        }

        val domain = uri?.host?.lowercase() ?: rawUrl.lowercase()
        val hasHttps = formattedUrl.startsWith("https://")

        // IDN Punycode check
        if (domain.startsWith("xn--") || domain.contains(".xn--")) {
            val punyResult = ThreatResult(
                target = domain,
                type = ThreatType.UPI,
                severity = Severity.HIGH,
                explanation = "Internationalized Domain Name (Punycode xn--) detected. High risk of homograph attack.",
                action = "Do not open punycode domain links."
            )
            repository.logThreat(punyResult, System.currentTimeMillis() - startTime)
            return punyResult
        }

        val isShortener = KNOWN_SHORTENERS.contains(domain)

        // Unicode homoglyph normalization
        val normalizedDomain = domain
            .replace('а', 'a')
            .replace('е', 'e')
            .replace('о', 'o')
            .replace('р', 'p')
            .replace('с', 'c')
            .replace("0", "o")
            .replace("1", "l")
            .replace("rn", "m")

        val isKnownScam = scamDomainsCache.contains(domain) || scamDomainsCache.contains(normalizedDomain)
        val subdomainCount = (domain.split(".").size - 2).coerceAtLeast(0)
        val containsUpiKeywords = UPI_KEYWORDS.any { domain.contains(it) || formattedUrl.lowercase().contains(it) }

        // 1. Direct return check
        val directReturn = PromptBuilder.checkUpiDirectReturn(
            domain = domain,
            isUrlShortener = isShortener,
            blocklisted = isKnownScam
        )
        if (directReturn != null) {
            repository.logThreat(directReturn, System.currentTimeMillis() - startTime)
            return directReturn
        }

        val prompt = PromptBuilder.upiPrompt(
            domain = domain,
            hasHttps = hasHttps,
            subdomainDepth = subdomainCount,
            isUrlShortener = isShortener,
            homoglyphDetected = normalizedDomain != domain,
            isIdnDomain = domain.startsWith("xn--"),
            blocklisted = isKnownScam,
            containsUpiKeywords = containsUpiKeywords,
            containsBankKeywords = domain.contains("sbi") || domain.contains("hdfc") || domain.contains("icici")
        )

        val result = classifier.classify(prompt, domain, ThreatType.UPI)
        
        val finalResult = if (subdomainCount > 2 && result.severity < Severity.HIGH) {
            result.copy(
                severity = Severity.HIGH,
                explanation = "Deep subdomain count detected. ${result.explanation}",
                action = "Verify website authenticity before entering any UPI PIN or payment details."
            )
        } else {
            result
        }

        repository.logThreat(finalResult, System.currentTimeMillis() - startTime)
        return finalResult
    }
}
