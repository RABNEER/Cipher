# VIKING — Technical Architecture & Security Design Specification

## Executive Summary
VIKING is an on-device AI cybersecurity agent for Android designed specifically for the Indian threat landscape. It executes 100% locally using Google's quantized **Gemma 270M INT4** model via the MediaPipe GenAI LLM Inference API. The architecture enforces zero network connectivity, strict input sanitization against prompt injection attacks, 31.7% direct-return rule-engine bypasses, and DPDP Act 2023 privacy compliance.

---

## 1. On-Device AI Engine Lifecycle & Threading

```
                     ┌──────────────────────────────┐
                     │   VikingApplication Init     │
                     └──────────────┬───────────────┘
                                    │
                                    ▼
                     ┌──────────────────────────────┐
                     │      GemmaEngine Init        │
                     │  (Executors.singleThread)    │
                     └──────────────┬───────────────┘
                                    │
                                    ▼
                     ┌──────────────────────────────┐
                     │   SHA-256 Checksum Validation │
                     └──────────────┬───────────────┘
                                    │
                         ┌──────────┴──────────┐
                         │ Valid               │ Mismatch / Missing
                         ▼                     ▼
              ┌────────────────────┐  ┌──────────────────┐
              │ EngineState.Ready  │  │EngineState.Error │
              └──────────┬─────────┘  └──────────────────┘
                         │
                         ▼
              ┌────────────────────┐
              │   Inference Call   │
              │ (8000ms Timeout)   │
              └──────────┬─────────┘
                         │
        ┌────────────────┼────────────────┐
        │ Success        │ Timeout        │ Critical Memory Trim / Battery <15%
        ▼                ▼                ▼
┌───────────────┐ ┌───────────────┐ ┌─────────────────────────────┐
│ Parsed Result │ │ Retry /       │ │ LowBatteryThrottled State   │
│               │ │ Fallback Result│ │ (Static Rule Engine Mode)   │
└───────────────┘ └───────────────┘ └─────────────────────────────┘
```

### Key Concurrency & Memory Guarantees
- **Dedicated Single-Thread Dispatcher:** All MediaPipe `LlmInference` calls execute strictly on `Executors.newSingleThreadExecutor().asCoroutineDispatcher()` to prevent native memory corruption and thread-racing.
- **Inference Timeout Guard:** Every call to `infer(prompt)` is wrapped in `withTimeout(8000L)`. If inference exceeds 8 seconds, the engine cancels execution, retries once with exponential backoff (300ms), and defaults to a fail-safe `Severity.MEDIUM` verdict.
- **Memory Trim Listener (`ComponentCallbacks2`):** Upon receiving `TRIM_MEMORY_CRITICAL`, the engine releases native model handles (`llmInference = null`), shifts state to `EngineState.Loading`, and safely re-initializes.
- **Smart Battery Throttling:** When the OS broadcasts `ACTION_BATTERY_LOW` (<15% battery), the AI engine automatically shifts to `LowBatteryThrottled` state, routing 100% of security checks to the zero-battery static rule engine.

---

## 2. Adversarial Prompt Injection Defense Framework

To prevent attackers from using prompt injection techniques (e.g. embedding `"ignore previous instructions"` inside SMS text or APK package names), VIKING implements strict 3-tier sanitization in `PromptBuilder.kt`:

1. **Character Filtering:** Strips all characters outside printable ASCII (`32..126`) and Devanagari Hindi Unicode range (`U+0900..U+097F`).
2. **Length Truncation:** Hard-truncates any single user-controllable input parameter to maximum 120 characters.
3. **Pattern Rejection:** Scans inputs against injection keywords (`["ignore previous", "system:", "you are now", "disregard", "forget your", "new instructions"]`).
   - If an injection attempt is detected, `PromptBuilder` throws `PromptInjectionException`, bypassing Gemma completely and outputting a `Severity.HIGH` security alert.

---

## 3. Threat Shield Module Specifications

### Shield 1: APK Scanner (`ScanApkUseCase`)
- **Input:** Uri of target APK package file.
- **Extracts:** Package name, requested dangerous permissions, signature SHA-256 hash, min/target SDK versions, version code.
- **Rules:**
  - Unsigned APKs → Automatic `HIGH` severity.
  - Version Code < Installed Version Code → Automatic `HIGH` severity (Downgrade attack protection).
  - Signature Hash + Version Code Caching → Prevents redundant AI inference on previously scanned APK binaries.

### Shield 2: UPI Link Guard (`CheckUpiLinkUseCase`)
- **Input:** Raw URL string from user paste, intent share, or clipboard.
- **Extracts:** Domain, HTTPS status, subdomain depth, homoglyph normalization, IDN punycode prefix (`xn--`), local blocklist match.
- **Rules:**
  - Local Scam Domain Match (606 domains) → Direct Return `CRITICAL`.
  - IDN Punycode / Homoglyph Detected → Direct Return `HIGH`.
  - URL Shortener (`bit.ly`, `tinyurl.com`, `t.co`) → Direct Return `HIGH` (cannot verify without network call).

### Shield 3: SMS Shield (`AnalyzeSmsUseCase`)
- **Input:** Sender address & raw message body.
- **Extracts:** Sender type (Bank Shortcode, Telemarketing 140xxx, Unknown, International), URL presence, UPI ID pattern, OTP pattern, regional urgency keyword score (236 terms across 8 languages).
- **Rules:**
  - Bank Short Code (`VM-SBIINB`, `AD-HDFCBK`) → Direct Return `SAFE`.
  - Ordered Broadcast Abort → Aborts incoming SMS broadcast if `Severity.CRITICAL` threat detected.
  - 60-Second Deduplication → Prevents notification spam for duplicate scam SMS fingerprints.

### Shield 4: Call Fingerprint (`FingerprintCallUseCase` & `DigitalArrestDetectorUseCase`)
- **Input:** Caller ID number, duration, repeat status, transcript/keywords.
- **Extracts:** Prefix risk score, 140xxx telemarketing prefix, spoofed 1800 toll-free number length mismatch, digital arrest keywords (`cbi`, `police`, `customs`, `narcotics`, `digital custody`).
- **Rules:**
  - Digital Arrest Fraud Keywords → Direct Return `CRITICAL`.
  - Spoofed 1800 Toll-Free Length Mismatch → Direct Return `HIGH`.
  - Telemarketing 140 Call > 60s → Direct Return `HIGH`.

### Shield 5: NFC Monitor (`MonitorNfcUseCase`)
- **Input:** NFC tag type, payload size, NDEF record type, payload URL, read latency ms.
- **Rules:**
  - Payment App in Foreground (`PhonePe`, `GPay`, `Paytm`) → Direct Return `HIGH` (Payment screen protection).
  - Tag Read Latency > 500ms → Direct Return `CRITICAL` (NFC Relay Attack signature).
  - Unknown/Non-standard NDEF Record → Direct Return `HIGH`.

### Shield 6: Permission Privilege Auditor (`AuditPermissionsUseCase`)
- **Input:** Installed non-system package list.
- **Extracts:** Granted dangerous permissions, app category heuristics, permission risk matrix combinations.
- **Rules:**
  - `BIND_ACCESSIBILITY_SERVICE` + `INTERNET` → Direct Return `CRITICAL`.
  - `BIND_DEVICE_ADMIN` → Direct Return `CRITICAL`.
  - `READ_SMS` + `SEND_SMS` → Direct Return `HIGH`.
  - Trusted Package Whitelist (141 apps) → Automatic Skip.

---

## 4. Encrypted Data Layer & Security Architecture

### SQLCipher AES-256 Room Database
- Database `viking_threat_db` is encrypted using SQLCipher (`net.zetetic:android-database-sqlcipher:4.5.4`).
- Database passphrase is a randomly generated 256-bit cryptographically secure key created via `SecureRandom` and persisted in encrypted private shared preferences (`DatabaseSecurityHelper.kt`).
- Schema Version 2 includes enriched metadata fields: `appVersion`, `deviceApiLevel`, `modelVersion`, `scanDurationMs`.

### DataStore Lightweight Preferences
- System settings and module toggle states (`module_apk_enabled`, `module_upi_enabled`, etc.) are persisted using Jetpack DataStore Preferences (`viking_settings.pb`).

---

## 5. System Services & Background Integration

1. **`VikingQuickTile.kt`:** Android Quick Settings `TileService` allowing 1-tap toggling of active monitoring directly from the notification shade.
2. **`VikingAccessibilityService.kt`:** Monitors window state changes for sideload install dialogs and over-privileged permission dialogs with a 30-second per-package rate limit.
3. **`ClipboardGuardService.kt`:** Foreground clipboard listener scanning copied text for phishing URLs and UPI collect links.
4. **`MediaSideloadObserverService.kt`:** Monitors `/sdcard/Download` for incoming `.apk` files from messaging apps (WhatsApp/Telegram).
5. **`SimStateReceiver.kt`:** Listens for `ACTION_SIM_STATE_CHANGED` events to flag potential SIM swap / IMSI takeover attempts.
6. **`PermissionAuditWorker.kt`:** WorkManager periodic task scheduled every 7 days with `BATTERY_NOT_LOW` constraint and exponential backoff.

---

## 6. DPDP Act 2023 Compliance & Zero-Cloud Guarantee

- **Zero Network Imports:** The codebase contains 0 instances of network libraries (`HttpURLConnection`, `OkHttp`, `Retrofit`, `Volley`).
- **Data Minimization:** No raw user message text, call audio transcripts, or contacts are stored in Room DB — only extracted metadata features and `ThreatResult` verdicts.
- **Local Observability:** Log files are stored strictly inside `context.filesDir/logs/` with rotating file limits (3 x 1MB files). Logs redacting sensitive numbers and URLs via `VikingLogger`.
