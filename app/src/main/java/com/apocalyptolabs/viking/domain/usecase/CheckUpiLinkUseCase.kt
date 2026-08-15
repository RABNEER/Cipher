package com.apocalyptolabs.viking.domain.usecase

import android.content.Context
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
            val assets = context.assets ?: return
            assets.open("scam_domains.txt").bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    val domain = line.trim().lowercase()
                    if (domain.isNotBlank() && !domain.startsWith("#")) {
                        scamDomainsCache.add(domain)
                    }
                }
            }
        } catch (e: Throwable) {
            VikingLogger.w("Could not load scam_domains.txt asset", "CheckUpiLinkUseCase")
        }
    }

    suspend operator fun invoke(rawUrl: String): ThreatResult {
        val startTime = System.currentTimeMillis()
        val cleanUrl = rawUrl.trim()

        // UPI deep links (upi://pay?pa=vpa@bank&...) carry a payee VPA, not a web domain.
        val isUpiScheme = cleanUrl.startsWith("upi://", ignoreCase = true)
        val domain = if (isUpiScheme) {
            extractUpiPayeeDomain(cleanUrl)
        } else {
            cleanUrl
                .removePrefix("http://")
                .removePrefix("https://")
                .split("/", "?", "#", "&", ":")[0]
                .lowercase()
        }

        val hasHttps = cleanUrl.startsWith("https://", ignoreCase = true)

        if (!isUpiScheme && (domain.startsWith("xn--") || domain.contains(".xn--"))) {
            val punyResult = ThreatResult(
                target = domain,
                type = ThreatType.UPI,
                severity = Severity.HIGH,
                explanation = "Internationalized Domain Name (Punycode xn--) detected. High risk of homograph attack.",
                action = "Do not open punycode domain links."
            )
            safeLog(punyResult, System.currentTimeMillis() - startTime)
            return punyResult
        }

        if (isUpiScheme && domain.isBlank()) {
            val opaqueResult = ThreatResult(
                target = "UPI Deep Link",
                type = ThreatType.UPI,
                severity = Severity.MEDIUM,
                explanation = "UPI payment link with an unreadable payee address. Payee identity cannot be verified on-device.",
                action = "Verify the payee name inside your UPI app before entering any PIN."
            )
            safeLog(opaqueResult, System.currentTimeMillis() - startTime)
            return opaqueResult
        }

        val isShortener = KNOWN_SHORTENERS.contains(domain)

        // Homoglyph normalization must ONLY map visually confusable Unicode
        // characters. Mapping ASCII digits ("0"->"o") or substrings ("rn"->"m")
        // flags legitimate domains like modernpay.com as fake.
        val normalizedDomain = domain
            .replace('а', 'a')
            .replace('е', 'e')
            .replace('о', 'o')
            .replace('р', 'p')
            .replace('с', 'c')
            .replace('х', 'x')

        val isKnownScam = scamDomainsCache.contains(domain) || scamDomainsCache.contains(normalizedDomain)
        val subdomainCount = if (domain.isNotBlank()) (domain.split(".").size - 2).coerceAtLeast(0) else 0
        val containsUpiKeywords = UPI_KEYWORDS.any { domain.contains(it) || cleanUrl.lowercase().contains(it) }

        val directReturn = PromptBuilder.checkUpiDirectReturn(
            domain = domain,
            isUrlShortener = isShortener,
            blocklisted = isKnownScam
        )
        if (directReturn != null) {
            safeLog(directReturn, System.currentTimeMillis() - startTime)
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

        safeLog(finalResult, System.currentTimeMillis() - startTime)
        return finalResult
    }

    private suspend fun safeLog(threat: ThreatResult, duration: Long = 0L) {
        try {
            repository.logThreat(threat, duration)
        } catch (e: Throwable) {
            // Ignored in test doubles
        }
    }

    private fun extractUpiPayeeDomain(upiUrl: String): String {
        return try {
            val uri = android.net.Uri.parse(upiUrl)
            val vpa = uri.getQueryParameter("pa") ?: return ""
            val domainPart = vpa.substringAfter('@', "").trim()
            if (domainPart.isBlank()) "" else domainPart.lowercase()
        } catch (_: Exception) {
            ""
        }
    }
}
