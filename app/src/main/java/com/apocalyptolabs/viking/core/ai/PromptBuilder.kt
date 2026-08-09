package com.apocalyptolabs.viking.core.ai

import com.apocalyptolabs.viking.core.model.AppCategory
import com.apocalyptolabs.viking.core.model.CallerType
import com.apocalyptolabs.viking.core.model.NfcRecordType
import com.apocalyptolabs.viking.core.model.SenderType
import com.apocalyptolabs.viking.core.model.Severity
import com.apocalyptolabs.viking.core.model.ThreatResult
import com.apocalyptolabs.viking.core.model.ThreatType

class PromptInjectionException(val injectionTerm: String) : Exception("Prompt injection detected: $injectionTerm")

object PromptBuilder {

    private const val ROLE_LINE = "You are Viking, a cybersecurity AI running on-device on an Android phone in India."
    private const val OUTPUT_INSTRUCTION = """Respond ONLY with valid JSON, no explanation, no markdown: {"severity":"CRITICAL|HIGH|MEDIUM|LOW|SAFE","threat":"short threat name","explanation":"one plain sentence in simple English","action":"one concrete action for the user"}"""

    private val INJECTION_PATTERNS = listOf(
        "ignore previous", "system:", "you are now", "disregard", "forget your", "new instructions"
    )

    fun sanitize(input: String): String {
        val lower = input.lowercase()
        for (pattern in INJECTION_PATTERNS) {
            if (lower.contains(pattern)) {
                throw PromptInjectionException(pattern)
            }
        }

        val sb = StringBuilder()
        for (char in input) {
            val code = char.code
            if ((code in 32..126) || (code in 0x0900..0x097F)) {
                sb.append(char)
            }
        }
        return sb.toString().take(120)
    }

    fun buildInjectionDetectedResult(target: String, type: ThreatType): ThreatResult {
        return ThreatResult(
            target = target,
            type = type,
            severity = Severity.HIGH,
            explanation = "Suspicious input detected: Prompt injection attempt blocked before model inference.",
            action = "Exercise extreme caution and do not follow untrusted commands."
        )
    }

    fun apkPrompt(
        packageName: String,
        permissions: List<String>,
        hasInternetAccess: Boolean,
        requestsAdminRights: Boolean,
        requestsAccessibility: Boolean,
        installedFromUnknownSource: Boolean,
        isSigned: Boolean = true,
        versionDowngrade: Boolean = false,
        dangerousPermCount: Int = permissions.size
    ): String {
        val cleanPkg = sanitize(packageName)
        val cleanPerms = permissions.map { sanitize(it) }

        val criticalFlags = mutableListOf<String>()
        if (requestsAdminRights && hasInternetAccess) criticalFlags.add("CRITICAL: requestsAdminRights=true + hasInternetAccess=true")
        if (requestsAccessibility && hasInternetAccess) criticalFlags.add("CRITICAL: requestsAccessibility=true + hasInternetAccess=true")

        val highFlags = mutableListOf<String>()
        if (installedFromUnknownSource && dangerousPermCount > 3) highFlags.add("HIGH: installedFromUnknownSource=true + dangerousPermCount>3")
        if (!isSigned) highFlags.add("HIGH: isSigned=false (unsigned APK)")
        if (versionDowngrade) highFlags.add("HIGH: versionDowngrade=true")

        return """
            |$ROLE_LINE
            |An Indian user is about to install this app. Assess the risk.
            |Target: APK ($cleanPkg)
            |Features:
            |- Dangerous Permissions: ${cleanPerms.joinToString(", ")}
            |- Internet Access: $hasInternetAccess
            |- Admin Rights: $requestsAdminRights
            |- Accessibility Service: $requestsAccessibility
            |- Unknown Source: $installedFromUnknownSource
            |- Signed: $isSigned
            |- Version Downgrade: $versionDowngrade
            |- Dangerous Perm Count: $dangerousPermCount
            |- Flagged Combos: ${(criticalFlags + highFlags).ifEmpty { listOf("None") }.joinToString("; ")}
            |$OUTPUT_INSTRUCTION
        """.trimMargin()
    }

    fun checkUpiDirectReturn(
        domain: String,
        isUrlShortener: Boolean,
        blocklisted: Boolean
    ): ThreatResult? {
        if (blocklisted) {
            return ThreatResult(
                target = domain,
                type = ThreatType.UPI,
                severity = Severity.CRITICAL,
                explanation = "Domain matched known malicious UPI/Banking phishing blocklist.",
                action = "DO NOT open this link or approve any UPI collect request."
            )
        }
        if (isUrlShortener) {
            return ThreatResult(
                target = domain,
                type = ThreatType.UPI,
                severity = Severity.HIGH,
                explanation = "Shortened URL ($domain) detected — cannot verify safety without external resolution.",
                action = "Do not open shortened links in payment apps."
            )
        }
        return null
    }

    fun upiPrompt(
        domain: String,
        hasHttps: Boolean,
        subdomainDepth: Int,
        isUrlShortener: Boolean,
        homoglyphDetected: Boolean,
        isIdnDomain: Boolean,
        blocklisted: Boolean,
        containsUpiKeywords: Boolean,
        containsBankKeywords: Boolean
    ): String {
        val cleanDomain = sanitize(domain)

        return """
            |$ROLE_LINE
            |An Indian user received this payment link. Is it safe to pay?
            |Target: Payment Link ($cleanDomain)
            |Features:
            |- HTTPS Enabled: $hasHttps
            |- Subdomain Depth: $subdomainDepth
            |- Homoglyph Detected: $homoglyphDetected
            |- IDN Punycode Domain: $isIdnDomain
            |- Contains UPI Keywords: $containsUpiKeywords
            |- Contains Bank Keywords: $containsBankKeywords
            |$OUTPUT_INSTRUCTION
        """.trimMargin()
    }

    fun checkSmsDirectReturn(
        senderType: SenderType,
        sender: String
    ): ThreatResult? {
        if (senderType == SenderType.BANK_SHORTCODE) {
            return ThreatResult(
                target = "SMS from $sender",
                type = ThreatType.SMS,
                severity = Severity.SAFE,
                explanation = "Legitimate financial institution short code ($sender).",
                action = "No action needed."
            )
        }
        return null
    }

    fun smsPrompt(
        senderType: SenderType,
        hasUrl: Boolean,
        hasUpiId: Boolean,
        hasOtpPattern: Boolean,
        urgencyScore: Int,
        languageDetected: String,
        mentionsBankName: Boolean,
        mentionsGovtScheme: Boolean,
        featureCount: Int
    ): String {
        return """
            |$ROLE_LINE
            |An Indian user received an SMS. Assess if it is a scam.
            |Features:
            |- Sender Type: ${senderType.name}
            |- Urgency Framing: Urgency score of $urgencyScore out of 10
            |- Contains URL: $hasUrl
            |- Contains UPI ID: $hasUpiId
            |- Contains OTP Pattern: $hasOtpPattern
            |- Language Detected: $languageDetected
            |- Mentions Bank Name: $mentionsBankName
            |- Mentions Govt Scheme: $mentionsGovtScheme
            |- Total Suspicious Feature Count: $featureCount
            |$OUTPUT_INSTRUCTION
        """.trimMargin()
    }

    fun checkCallDirectReturn(
        callerType: CallerType,
        callerNumber: String,
        callDurationSeconds: Int
    ): ThreatResult? {
        if (callerType == CallerType.SPOOFED) {
            return ThreatResult(
                target = "Call from $callerNumber",
                type = ThreatType.CALL,
                severity = Severity.CRITICAL,
                explanation = "Spoofed Bank Helpline caller ID detected.",
                action = "Disconnect immediately. Do not share financial credentials."
            )
        }
        if (callerType == CallerType.TELEMARKETING_140 && callDurationSeconds > 60) {
            return ThreatResult(
                target = "Call from $callerNumber",
                type = ThreatType.CALL,
                severity = Severity.HIGH,
                explanation = "Extended 140-telemarketer call (>60s) with high social engineering risk.",
                action = "Disconnect call if asked for OTP or personal banking details."
            )
        }
        return null
    }

    fun callPrompt(
        callerType: CallerType,
        callDurationSeconds: Int,
        isRepeatCaller: Boolean,
        prefixRiskScore: Int,
        lengthMismatch: Boolean
    ): String {
        return """
            |$ROLE_LINE
            |An Indian user received this call. Is it likely a scam?
            |Features:
            |- Caller Type: ${callerType.name}
            |- Call Duration: ${callDurationSeconds}s
            |- Repeat Caller: $isRepeatCaller
            |- Prefix Risk Score: $prefixRiskScore/10
            |- Caller ID Length Mismatch: $lengthMismatch
            |$OUTPUT_INSTRUCTION
        """.trimMargin()
    }

    fun checkNfcDirectReturn(
        recordType: NfcRecordType,
        isRelayAttackSuspected: Boolean
    ): ThreatResult? {
        if (isRelayAttackSuspected) {
            return ThreatResult(
                target = "NFC Tag",
                type = ThreatType.NFC,
                severity = Severity.CRITICAL,
                explanation = "NFC relay attack signature detected (High tag read latency).",
                action = "Remove device from NFC reader immediately."
            )
        }
        if (recordType == NfcRecordType.UNKNOWN) {
            return ThreatResult(
                target = "NFC Tag",
                type = ThreatType.NFC,
                severity = Severity.HIGH,
                explanation = "Unrecognized or non-standard NFC record payload format.",
                action = "Do not execute unverified NFC payload instructions."
            )
        }
        return null
    }

    fun nfcPrompt(
        recordType: NfcRecordType,
        hasExternalUrl: Boolean,
        isStandardPaymentFormat: Boolean,
        domainIfUrl: String?,
        readTimeMs: Long,
        isRelayAttackSuspected: Boolean
    ): String {
        val cleanDomain = domainIfUrl?.let { sanitize(it) } ?: "None"

        return """
            |$ROLE_LINE
            |An Android phone in India just read an NFC tag. Assess the risk.
            |Features:
            |- Record Type: ${recordType.name}
            |- External URL: $hasExternalUrl
            |- Domain (if URL): $cleanDomain
            |- Standard Payment Format: $isStandardPaymentFormat
            |- Read Latency: ${readTimeMs}ms
            |$OUTPUT_INSTRUCTION
        """.trimMargin()
    }

    fun checkPermissionDirectReturn(
        appName: String,
        highRiskCombos: List<String>,
        criticalCombos: List<String>
    ): ThreatResult? {
        if (criticalCombos.isNotEmpty()) {
            return ThreatResult(
                target = appName,
                type = ThreatType.PERMISSION,
                severity = Severity.CRITICAL,
                explanation = "Critical permission combination detected: ${criticalCombos.joinToString(", ")}.",
                action = "Revoke dangerous accessibility/admin privileges immediately."
            )
        }
        if (highRiskCombos.size > 1) {
            return ThreatResult(
                target = appName,
                type = ThreatType.PERMISSION,
                severity = Severity.HIGH,
                explanation = "Multiple high-risk permission combinations detected: ${highRiskCombos.joinToString(", ")}.",
                action = "Review application permissions in Settings > Apps."
            )
        }
        return null
    }

    fun permissionPrompt(
        appName: String,
        appCategory: AppCategory,
        highRiskCombos: List<String>,
        criticalCombos: List<String>,
        totalDangerousPerms: Int,
        isNewlyInstalled: Boolean
    ): String {
        val cleanApp = sanitize(appName)

        return """
            |$ROLE_LINE
            |An Indian user has this app installed. Are its permissions suspicious?
            |Target: App ($cleanApp)
            |Features:
            |- Category: ${appCategory.name}
            |- High Risk Combos: ${highRiskCombos.ifEmpty { listOf("None") }.joinToString(", ")}
            |- Total Dangerous Perms: $totalDangerousPerms
            |- Newly Installed: $isNewlyInstalled
            |$OUTPUT_INSTRUCTION
        """.trimMargin()
    }
}
