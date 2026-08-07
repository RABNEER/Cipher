# VIKING — On-Device AI Cybersecurity Agent

![Android Build](https://img.shields.io/badge/Android-API%2026%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![AI Runtime](https://img.shields.io/badge/AI_Engine-Gemma_270M_INT4-4285F4?style=for-the-badge&logo=google&logoColor=white)
![Security](https://img.shields.io/badge/Privacy-100%25_On--Device-00B4D8?style=for-the-badge&logo=shield&logoColor=white)
![Compliance](https://img.shields.io/badge/DPDP_Act_2023-Compliant-34C759?style=for-the-badge)
![License](https://img.shields.io/badge/License-Apache_2.0-FF9500?style=for-the-badge)

> **One AI Agent. Six Shields. Zero Cloud. Built for Bharat.**

VIKING is a production-ready, zero-network, on-device AI cybersecurity agent built for Android. Powered by Google's quantized **Gemma 270M INT4** model via MediaPipe GenAI LLM Inference API, Viking detects mobile threats across 6 vectors in real-time — completely offline, with zero data leaving the device.

---

## 🛡️ Architecture & How It Works

Viking follows Clean Architecture + MVVM with Hilt dependency injection, Room (SQLCipher encrypted), and WorkManager.

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           ANDROID APP LAYER                             │
│       (Jetpack Compose UI / Services / Receivers / WorkManager)         │
└────────────────────────────────────┬────────────────────────────────────┘
                                     │
                                     ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                    FEATURE EXTRACTION & SANITIZATION                    │
│   (Extracts metadata & features; zero raw message/call body sent)       │
└────────────────────────────────────┬────────────────────────────────────┘
                                     │
                                     ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                     PROMPT BUILDER & DIRECT BYPASS                      │
│   (Checks local blocklists/whitelists; constructs <280 token prompt)    │
└──────────────────┬──────────────────────────────────────┬───────────────┘
                   │ Direct Return                        │ Prompt Built
                   ▼                                      ▼
┌──────────────────────────────────────┐ ┌────────────────────────────────┐
│      STATIC RULE ENGINE BYPASS       │ │     GEMMA 270M INT4 ENGINE     │
│ (Known scam domains, bank whitelist) │ │   (MediaPipe LLM Inference)    │
└──────────────────┬───────────────────┘ └────────────────┬───────────────┘
                   │                                      │
                   └──────────────────┬───────────────────┘
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                           THREAT CLASSIFIER                             │
│             (Parses JSON result into structured ThreatResult)           │
└────────────────────────────────────┬────────────────────────────────────┘
                                     │
                                     ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                  PRESENTATION & INCIDENT RESPONSE                       │
│    (UI Cards / High-Priority System Notifications / Encrypted DB Log)   │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 🔒 Six Threat Shield Modules

| Shield Module | Vector Detected | Direct Return Cases (No AI Needed) | Gemma 270M Triggered |
| :--- | :--- | :--- | :--- |
| **APK Scanner** | Unsigned APKs, version downgrade attacks, dangerous permission combos | Unsigned APKs, version downgrades | Complex permission scope evaluation |
| **UPI Link Guard** | Phishing domains, IDN punycode attacks, homoglyph domains, shorteners | Blocklisted domains, URL shorteners | Homoglyph & deep subdomain analysis |
| **SMS Shield** | Financial scams, OTP fraud, urgency framing in 8 regional languages | Whitelisted bank short codes | Multi-feature urgency & scam framing |
| **Call Fingerprint** | Spoofed bank helplines, 140xxx telemarketing, scam prefixes | Spoofed bank ID, 60s+ 140 calls | International ping & duration risk |
| **NFC Monitor** | Relay attacks, unverified payload URLs, malicious NDEF records | Relay attacks (>500ms latency), unknown NDEF | Unverified NDEF external link analysis |
| **Permission Audit** | Over-privileged apps (e.g. calculator with SMS/mic access) | Critical combos (Accessibility+Net) | Category mismatch & risk matrix diffs |

---

## ⚡ Tech Stack

- **Language:** Kotlin 2.0 (Jetpack Compose UI)
- **AI Runtime:** MediaPipe GenAI LLM Inference API (`com.google.mediapipe:tasks-genai:0.10.14`)
- **Model:** Gemma 270M INT4 quantized CPU model (`gemma-270m-it-cpu-int4.bin`)
- **Architecture:** MVVM + Clean Architecture
- **Dependency Injection:** Hilt 2.51.1
- **Database & Security:** Room 2.6.1 + SQLCipher (AES-256-GCM Keystore Passphrase)
- **Background Work:** WorkManager 2.9.1 (`BATTERY_NOT_LOW` constraint)
- **Logging & Crash Safety:** Timber + VikingReleaseTree (rotating file logs) + ANR Watchdog

---

## 🚀 Setup & Build Instructions

### Prerequisites
- Android Studio Ladybug (2024.2.1+) or command-line Gradle
- Android SDK 35 (minSdk 26+)
- JDK 17

### Adding the Gemma Model
Place the quantized Gemma model binary in the app assets folder:
```bash
viking/app/src/main/assets/gemma-270m-it-cpu-int4.bin
```

### Build Commands
```bash
# Build Debug APK
./gradlew assembleDebug

# Build Release APK with R8 minification & resource shrinking
./gradlew assembleRelease

# Run Unit Tests
./gradlew test

# Run Python Benchmark Suite
python tools/benchmark/run_benchmark.py
```

---

## 📊 Performance & Benchmark Metrics

Results from `run_benchmark.py` across 60 threat scenarios:

| Metric | Result |
| :--- | :--- |
| **Asset Load Time** | **8.14 ms** (606 domains, 236 urgency words, 21 prefixes, 141 trusted apps) |
| **Peak Asset Memory Footprint** | **0.15 MB** |
| **Direct Return Bypass Rate** | **31.7%** (19 / 60 scenarios resolved without invoking model) |
| **Avg Prompt Token Length** | **13 tokens** (Max 280 token bound enforced) |
| **Average Decision Latency** | **< 0.01 ms** (Rule engine) / **< 450 ms** (Gemma INT4 CPU execution) |
| **Battery Budget Impact** | **< 0.2% per day** (WorkManager periodic execution with `BATTERY_NOT_LOW`) |

---

## 🔐 Privacy & Compliance

1. **Zero Network Calls:** Zero `HttpURLConnection`, `OkHttp`, or `Retrofit` imports anywhere in the codebase.
2. **DPDP Act 2023 Compliant:** No raw user text, call audio, or message contents are ever stored or logged.
3. **Encrypted Storage:** Room DB encrypted via SQLCipher using a random 256-bit passphrase stored securely in Android SharedPreferences.
4. **Local Logging Only:** Application logs written exclusively to `context.filesDir/logs/` (max 3x1MB rotating files).

---

## 📂 Project Structure

```
viking/
├── app/
│   ├── build.gradle.kts
│   ├── viking-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── assets/
│       │   ├── gemma-270m-it-cpu-int4.bin
│       │   ├── scam_domains.txt
│       │   ├── urgency_words.txt
│       │   ├── scam_prefixes.txt
│       │   └── trusted_packages.txt
│       └── java/com/apocalyptolabs/viking/
│           ├── VikingApplication.kt
│           ├── MainActivity.kt
│           ├── core/
│           │   ├── ai/ (GemmaEngine, ThreatClassifier, PromptBuilder)
│           │   ├── model/ (ThreatType, ThreatResult, Severity, SecurityEnums)
│           │   └── util/ (VikingLogger, PermissionChecker, Extensions, ANRWatchdog)
│           ├── data/
│           │   ├── db/ (VikingDatabase, ThreatLogEntity, ThreatLogDao, SQLCipher)
│           │   └── repository/ (ThreatRepository, DataStore)
│           ├── domain/usecase/ (6 Use Cases)
│           ├── service/ (VikingAccessibilityService, SmsMonitorReceiver, NfcMonitorService, CallMonitorService, VikingQuickTile, PermissionAuditWorker)
│           └── ui/ (Dashboard, Scanner, ThreatLog, Permissions, Settings, VikingNavGraph)
└── tools/
    ├── benchmark/run_benchmark.py
    └── demo/DEMO_SCRIPT.md
```

---

## 👤 Team & Attribution

Built by **Ranveer Kumar** · Apocalypto Labs  
*TCOE Emerging Technologies Hackathon 2026 / IMC 2026*
