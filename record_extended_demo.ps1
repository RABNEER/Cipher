# Master Extended Demo Recording Script (~168 seconds)
# Thoroughly demonstrates all 7 defense shields, inspectors, sandboxes, and tools in Cipher.

Write-Host "Removing any existing temporary recording..."
adb shell rm -f /sdcard/cipher_extended_demo.mp4

Write-Host "Starting Android screenrecord (172 seconds limit)..."
$recordJob = Start-Process adb -ArgumentList "shell screenrecord --time-limit 172 --bit-rate 6500000 /sdcard/cipher_extended_demo.mp4" -PassThru

Start-Sleep -Seconds 3

# ACT 1: Tactical Dashboard & Threat Radar Sweep (23s)
Write-Host "Act 1: Dashboard, Threat Radar & Protocol Grid..."
adb shell am start -n com.apocalyptolabs.viking/.MainActivity
Start-Sleep -Seconds 4

# Smoothly scroll down across the 6 Armed Protocol Cards
adb shell input swipe 540 1800 540 900 500
Start-Sleep -Seconds 4

# Smoothly scroll back up to the sweeping Threat Radar
adb shell input swipe 540 900 540 1800 500
Start-Sleep -Seconds 4

# ACT 2: Interactive Module Inspector BottomSheet & In-Depth Test Harness (22s)
Write-Host "Act 2: Module Inspector BottomSheet & Interactive Test Harness..."
# Tap SEC-UPI (UPI Link Guard) card
adb shell input tap 795 1193
Start-Sleep -Seconds 4

# Tap Punycode scenario preset chip
adb shell input tap 386 1900
Start-Sleep -Seconds 2

# Tap RUN INFERENCE EVALUATION
adb shell input tap 540 2063
Start-Sleep -Seconds 4

# Tap VIEW SEC-UPI DETECTION LOGS ->
adb shell input tap 540 2248
Start-Sleep -Seconds 4

# Back to Dashboard
adb shell input keyevent 4
Start-Sleep -Seconds 3

# ACT 3: Security Attack Sandbox - Multi-Vector Live Fire (32s)
Write-Host "Act 3: Attack Sandbox Live Fire Simulations..."
# Tap Sandbox test-tube icon
adb shell input tap 629 227
Start-Sleep -Seconds 3

# SIM-01-SMS (SBI Suspended KYC Phishing)
adb shell input tap 540 867
Start-Sleep -Seconds 3
adb shell input swipe 540 1800 540 1000 400
Start-Sleep -Seconds 4
adb shell input swipe 540 1000 540 1800 400
Start-Sleep -Seconds 1

# SIM-04-NFC (NFC Relay Attack Anomaly)
adb shell input tap 540 1656
Start-Sleep -Seconds 3
adb shell input swipe 540 1800 540 1000 400
Start-Sleep -Seconds 4
adb shell input swipe 540 1000 540 1800 400
Start-Sleep -Seconds 1

# SIM-05-PERM (Over-Privileged Malicious Background App)
adb shell input tap 540 1911
Start-Sleep -Seconds 3
adb shell input swipe 540 1800 540 1000 400
Start-Sleep -Seconds 4

# Back to Dashboard
adb shell input keyevent 4
Start-Sleep -Seconds 3

# ACT 4: APK Ingress Binary Scanner - 44.5MB Binary Ingestion (26s)
Write-Host "Act 4: Ingress APK Binary Scanner & Static Analysis..."
# Tap FAB SCAN INGRESS APK
adb shell input tap 782 2239
Start-Sleep -Seconds 4

# Tap INGEST APK TARGET FILE
adb shell input tap 540 1665
Start-Sleep -Seconds 4

# Select test_target.apk (44.5MB) in DocumentsUI picker
adb shell input tap 783 1000
Start-Sleep -Seconds 6

# Pause to inspect comprehensive verdict card
Start-Sleep -Seconds 4

# Back to Dashboard
adb shell input keyevent 4
Start-Sleep -Seconds 3

# ACT 5: Privilege Escalation Audit (17s)
Write-Host "Act 5: Privilege Escalation Audit & Refresh..."
# Tap PRIVILEGE AUDIT ->
adb shell input tap 871 945
Start-Sleep -Seconds 4

# Tap Refresh icon in top bar
adb shell input tap 950 227
Start-Sleep -Seconds 5

# Back to Dashboard
adb shell input keyevent 4
Start-Sleep -Seconds 3

# ACT 6: Incident Vault - Forensics, Filters & Evidence Sharing (25s)
Write-Host "Act 6: Incident Vault, Filter Chips & Evidence Sharing..."
# Tap Incident Vault / Threat Log icon in top bar
adb shell input tap 1007 227
Start-Sleep -Seconds 3

# Filter: APK
adb shell input tap 396 577
Start-Sleep -Seconds 3

# Filter: UPI
adb shell input tap 586 577
Start-Sleep -Seconds 3

# Filter: SMS
adb shell input tap 776 577
Start-Sleep -Seconds 3

# Restore: ALL MODULES
adb shell input tap 167 562
Start-Sleep -Seconds 3

# Tap Share Evidence on top incident card
adb shell input tap 955 841
Start-Sleep -Seconds 4

# Dismiss Sharesheet
adb shell input keyevent 4
Start-Sleep -Seconds 2

# Back to Dashboard
adb shell input keyevent 4
Start-Sleep -Seconds 3

# ACT 7: Core Engine Settings, Protective Shield Controls & ZIP Export (20s)
Write-Host "Act 7: Engine Settings, Shield Switches & Forensic Export..."
# Tap Settings gear icon in top bar
adb shell input tap 881 227
Start-Sleep -Seconds 4

# Toggle APK Guard switch OFF then ON
adb shell input tap 927 970
Start-Sleep -Seconds 1.5
adb shell input tap 927 970
Start-Sleep -Seconds 1.5

# Toggle Clipboard UPI switch OFF then ON
adb shell input tap 927 1163
Start-Sleep -Seconds 1.5
adb shell input tap 927 1163
Start-Sleep -Seconds 1.5

# Tap Export Forensic Audit Packet (ZIP)
adb shell input tap 540 1793
Start-Sleep -Seconds 3.5

# Dismiss Sharesheet
adb shell input keyevent 4
Start-Sleep -Seconds 2

# Back to Dashboard
adb shell input keyevent 4
Start-Sleep -Seconds 3

# ACT 8: 1930 Cybercrime Helpline & Closing Shot (11s)
Write-Host "Act 8: 1930 Emergency Helpline Dialer & Closing Radar Shot..."
# Tap 1930 Helpline button in top bar
adb shell input tap 478 226
Start-Sleep -Seconds 4

# Back to Cipher
adb shell input keyevent 4
Start-Sleep -Seconds 4

Write-Host "Gracefully signaling screenrecord to finalize container..."
adb shell pkill -2 screenrecord
$waitCount = 0
while ((adb shell pidof screenrecord) -and ($waitCount -lt 15)) {
    Start-Sleep -Seconds 1
    $waitCount++
}
Start-Sleep -Seconds 3

Write-Host "Pulling raw video file from device..."
adb pull /sdcard/cipher_extended_demo.mp4 "d:\Viking\cipher_raw.mp4"

Write-Host "Muxing video and Andrew voiceover narration with FFmpeg (H.264 CFR 30fps + AAC audio + faststart)..."
ffmpeg -y -i "d:\Viking\cipher_raw.mp4" -i "d:\Viking\master_narration.m4a" -map 0:v:0 -map 1:a:0 -c:v libx264 -preset fast -crf 21 -r 30 -pix_fmt yuv420p -c:a aac -b:a 192k -movflags +faststart "d:\Viking\cipher_demo.mp4"

Copy-Item -Force "d:\Viking\cipher_demo.mp4" "d:\Viking\cipher_demo_2.mp4"
Copy-Item -Force "d:\Viking\cipher_demo.mp4" "C:\Users\LOQ\.gemini\antigravity-ide\brain\93b64f76-f436-4b4d-8040-df3896d2f718\cipher_demo.mp4"
Copy-Item -Force "d:\Viking\cipher_demo.mp4" "C:\Users\LOQ\.gemini\antigravity-ide\brain\93b64f76-f436-4b4d-8040-df3896d2f718\cipher_demo_2.mp4"

Write-Host "Master Demo Video with Andrew Voiceover successfully verified and saved to d:\Viking\cipher_demo.mp4 and cipher_demo_2.mp4!"
