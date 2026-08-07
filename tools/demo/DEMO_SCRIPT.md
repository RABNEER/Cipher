# VIKING — IMC 2026 Live Demo Script (3 Minutes)

**Presenter:** Ranveer Kumar (Apocalypto Labs)  
**Target Audience:** IMC 2026 Hackathon Jury & Telecommunications / Cybersecurity Judges  
**Device setup:** Android phone connected via Scrcpy / HDMI mirror to main screen. Airplane Mode ENABLED (to prove 100% offline capability).

---

## ⏱️ Timeline & Script Breakdown

### [0:00 – 0:45] Cold Open & Dashboard Overview
- **Screen:** Viking Dashboard (Dark Theme, Pulsing Teal/Green Shield Logo)
- **Spoken Script:**
  > *"Respected Judges, over 7,000 Indians lose their life savings every single day to UPI fraud, phishing SMSs, and malicious APK sideloads. Existing anti-virus tools fail because they send your personal data to cloud servers — creating privacy risks and requiring active internet. Meet **VIKING** — India's first on-device AI cybersecurity agent powered by Google's quantized Gemma 270M model. Notice my phone is in **Airplane Mode**. Everything you are about to see happens 100% on this phone, in real time, with zero network calls."*
- **Judge Sees:**
  - Dark-themed UI with "SYSTEM PROTECTED" status
  - 6 active module cards (APK, UPI, SMS, Call, NFC, Permission)
  - Status reading "Gemma 270M • Zero Network Calls"
- **Backup Plan:** If screen mirroring fails, hold device directly up to presenter camera with full brightness.

---

### [0:45 – 1:30] Live APK Sideload Scan
- **Screen:** Tap "Scan APK File" FAB → Navigate to `ApkScannerScreen`
- **Spoken Script:**
  > *"Let's test an untrusted APK package that a user just downloaded from Telegram. I tap 'Scan APK File' and select `malicious_demo.apk`. Instantly, Viking extracts the manifest permissions without installing the app, feeds the dangerous features into our on-device Gemma engine, and evaluates the risk signature."*
- **Judge Sees:**
  - File picker opening → selecting `malicious_demo.apk`
  - Animated Teal spinning ring with "Analyzing permissions… Evaluating security risk signature on-device…"
  - Verdict card popping up with a bold **CRITICAL** red badge:
    - *Target:* `com.admin.stealer`
    - *Explanation:* "CRITICAL: requestsAdminRights=true + hasInternetAccess=true. Over-privileged device administrator package."
    - *Action:* "Do not install this APK package."
- **Backup Plan:** If file picker takes > 5 seconds, use the pre-scanned result in Detection History log.

---

### [1:30 – 2:15] Real-Time SMS Phishing & Notification Alert
- **Screen:** Switch to SMS / Share Sheet Demo or paste phishing text into Viking
- **Spoken Script:**
  > *"Next, let's look at SMS phishing. Scammers send messages claiming your SBI bank account will be blocked unless you update KYC on a fake link. Watch what happens when this SMS arrives. Viking extracts key risk indicators — urgency score of 9 out of 10, fake SBI bank name, and an unverified domain link."*
- **Judge Sees:**
  - High-priority system notification firing: **"VIKING Security Alert: HIGH"**
  - Notification text: *"Suspicious SMS detected from unknown sender with high urgency score & phishing domain link. Do not click link."*
  - Tapping notification opens `ThreatLogScreen` showing the entry logged locally in SQLCipher encrypted storage.
- **Backup Plan:** If broadcast receiver is silent, trigger manual link check from Dashboard.

---

### [2:15 – 3:00] Quick Settings Tile & Wrap-Up
- **Screen:** Swipe down Android Notification Shade → Quick Settings Panel
- **Spoken Script:**
  > *"Finally, Viking integrates seamlessly into Android's core OS. A user simply pulls down their Quick Settings shade. You can see the **Viking Quick Tile** with active status reading 'All clear'. Tapping it toggles protection on or off instantly. All of this — six cybersecurity shields, zero cloud servers, zero subscription fees, zero privacy leaks — running locally on your phone. Thank you!"*
- **Judge Sees:**
  - Quick Settings tile with glowing Teal shield icon labeled "Viking" and subtitle "All clear"
  - Interactive toggle state changing smoothly
  - Clean exit back to Dashboard
- **Backup Plan:** Show tile settings in Android Settings menu if shade pull-down is covered by screen mirror overlay.

---

## 🎯 Key Points to Emphasize to Judges

1. **Airplane Mode On:** Proves zero network dependence.
2. **Gemma 270M On-Device:** State-of-the-art quantized LLM running in <450ms.
3. **Privacy First:** No message or call body is ever stored or transmitted (DPDP Act 2023 compliant).
