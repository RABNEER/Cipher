# VIKING Demo Setup Guide (IMC 2026)

Follow these pre-event setup steps on the demo smartphone at least 30 minutes before presenting to the jury.

---

## 📱 Hardware & OS Requirements

- **Device:** Android phone with API 26+ (Android 8.0+), 4GB+ RAM recommended
- **Screen Brightness:** 100%
- **Font Size:** Default / Medium (for clear visibility on HDMI presentation screens)
- **Display Timeout:** Set to 10 Minutes or "Never" during presentation
- **Network:** ENABLE **Airplane Mode** (Bluetooth & Wi-Fi OFF) to prove zero-cloud operation

---

## 📂 Asset Files Verification

1. Verify `gemma-270m-it-cpu-int4.bin` is installed:
   ```
   app/src/main/assets/gemma-270m-it-cpu-int4.bin
   ```
2. Place `malicious_demo.apk` in the phone Downloads directory:
   ```
   /sdcard/Download/malicious_demo.apk
   ```

---

## 🔧 Pre-Demo Checklist

1. **Install App:** Build and install release APK:
   ```bash
   ./gradlew installRelease
   ```
2. **Grant Permissions:** Launch Viking, complete the Onboarding screen, and grant SMS & Call Log permissions.
3. **Add Quick Settings Tile:**
   - Swipe down notification shade twice.
   - Tap Edit (Pencil icon).
   - Drag **Viking** tile into top 6 active Quick Settings grid positions.
4. **Pre-populate Test SMS (Optional Backup):**
   - Copy string to clipboard:
     `"URGENT: Your SBI Account 4829 is blocked. Update KYC immediately at https://sbi-kyc-update.com or access will be revoked."`

---

## 🎬 Scrcpy Screen Mirroring Command

For live projection on stage HDMI:
```bash
scrcpy --max-size 1080 --stay-awake --show-touches --window-title "VIKING Live Demo"
```
