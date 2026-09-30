import subprocess
import time
import os
import sys

def run_adb(cmd):
    subprocess.run(f"adb shell {cmd}", shell=True, check=True)

def tap(x, y):
    run_adb(f"input tap {x} {y}")

def swipe(x1, y1, x2, y2, dur=400):
    run_adb(f"input swipe {x1} {y1} {x2} {y2} {dur}")

def back():
    run_adb("input keyevent 4")

def main():
    print("Preparing device environment...")
    # Disable heads-up notifications to prevent obscuring top bar
    subprocess.run("adb shell settings put global heads_up_notifications_enabled 0", shell=True)
    subprocess.run("adb shell input swipe 540 200 540 0 200", shell=True)
    subprocess.run("adb shell rm -f /sdcard/cipher_full_demo.mp4", shell=True)
    
    # Launch app fresh with -S
    print("Launching Cipher freshly in foreground...")
    run_adb("am start -S -n com.apocalyptolabs.viking/.MainActivity")
    time.sleep(3.0)
    
    print("Starting screenrecord for 152 seconds via Python Popen...")
    rec_proc = subprocess.Popen(
        ["adb", "shell", "screenrecord", "--verbose", "--time-limit", "152", "/sdcard/cipher_full_demo.mp4"],
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True
    )
    
    start_time = time.time()
    def elapsed():
        return time.time() - start_time
    
    def wait_until(target_sec):
        rem = target_sec - elapsed()
        if rem > 0:
            time.sleep(rem)

    print("[00:00 - 00:16] ACT 1: Main Dashboard & Active Telemetry")
    wait_until(2.0)
    # Scroll down to display all 6 armed shield cards
    swipe(540, 1800, 540, 950, 500)
    wait_until(8.0)
    # Scroll back up to the circular radar
    swipe(540, 950, 540, 1800, 500)
    wait_until(16.0)

    print("[00:16 - 00:33] ACT 2: UPI Link Guard & Homoglyph Intercept")
    # Tap SEC-UPI card
    tap(795, 1193)
    wait_until(19.0)
    # Tap Punycode scenario preset chip
    tap(386, 1900)
    wait_until(22.0)
    # Tap RUN INFERENCE EVALUATION
    tap(540, 2063)
    wait_until(31.0)
    # Dismiss BottomSheet cleanly with Back button
    back()
    wait_until(34.0)

    print("[00:34 - 01:05] ACT 3: Synthetic Threat Sandbox & Behavioral Inference")
    # Tap Sandbox test-tube icon in TopAppBar (628, 231)
    tap(628, 231)
    wait_until(37.5)
    # Tap SIM-01-SMS (Banking KYC Phishing) card at (540, 365)
    tap(540, 365)
    wait_until(40.5)
    swipe(540, 1800, 540, 1100, 400)
    wait_until(45.0)
    swipe(540, 1100, 540, 1800, 400)
    wait_until(46.5)
    # Tap SIM-04-NFC (NFC APDU Relay Anomaly) card at (540, 685)
    tap(540, 685)
    wait_until(49.5)
    swipe(540, 1800, 540, 1100, 400)
    wait_until(54.0)
    swipe(540, 1100, 540, 1800, 400)
    wait_until(55.5)
    # Tap SIM-05-PERM (Over-Privileged Background App) card at (540, 795)
    tap(540, 795)
    wait_until(58.5)
    swipe(540, 1800, 540, 1100, 400)
    wait_until(63.0)
    # Return to Dashboard
    back()
    wait_until(66.0)

    print("[01:06 - 01:38] ACT 4: Ingress APK Scanner & Downgrade Attack Defense")
    # Tap FAB SCAN INGRESS APK at (782, 2239)
    tap(782, 2239)
    wait_until(69.5)
    # Tap INGEST APK TARGET FILE at (540, 1665)
    tap(540, 1665)
    wait_until(73.5)
    # Select test_target.apk (44.5MB) in DocumentsUI picker
    tap(783, 1000)
    wait_until(81.0)
    # Scroll down to display the full neural verdict and risk analysis
    swipe(540, 1800, 540, 1200, 400)
    wait_until(92.0)
    swipe(540, 1200, 540, 1800, 400)
    wait_until(96.0)
    # Return to Dashboard
    back()
    wait_until(99.0)

    print("[01:39 - 02:04] ACT 5: Incident Vault & Golden Hour Integration")
    # Tap Threat Log / Incident Vault icon in TopAppBar at (998, 228)
    tap(998, 228)
    wait_until(103.0)
    # Filter: APK
    tap(396, 577)
    wait_until(106.0)
    # Filter: UPI
    tap(586, 577)
    wait_until(109.0)
    # Filter: SMS
    tap(776, 577)
    wait_until(112.0)
    # Filter: ALL MODULES
    tap(167, 562)
    wait_until(115.0)
    # Tap Share Evidence on top incident card
    tap(955, 841)
    wait_until(119.0)
    # Dismiss Sharesheet
    back()
    wait_until(121.5)
    # Return to Dashboard
    back()
    wait_until(125.0)

    print("[02:05 - 02:30] ACT 6: On-Device Neural Core & Forensic Reporting")
    # Tap Settings gear icon in TopAppBar at (868, 225)
    tap(868, 225)
    wait_until(128.5)
    # Toggle APK Guard switch OFF then ON
    tap(927, 970)
    time.sleep(1.0)
    tap(927, 970)
    wait_until(132.5)
    # Tap Export Forensic Audit Packet (ZIP) at (540, 1793)
    tap(540, 1793)
    wait_until(136.5)
    # Dismiss Sharesheet
    back()
    wait_until(138.5)
    # Return to Dashboard
    back()
    wait_until(140.5)
    # Tap 1930 Emergency Helpline button in TopAppBar at (493, 224)
    tap(493, 224)
    wait_until(145.0)
    # Back from Dialer to Cipher
    back()
    wait_until(146.5)
    # Closing Radar Sweep
    print("Capturing closing radar sweep...")
    wait_until(151.0)

    print("Screenrecord duration elapsed. Waiting for process to flush container...")
    # Wait for screenrecord process to naturally finish or terminate
    time.sleep(2.0)
    if rec_proc.poll() is None:
        rec_proc.terminate()
        try:
            rec_proc.wait(timeout=6)
        except Exception:
            rec_proc.kill()
            
    # Give Android OS 2 seconds to finalize moov atom and close file handle
    time.sleep(2.0)
    
    print("Pulling recording from device...")
    subprocess.run("adb pull /sdcard/cipher_full_demo.mp4 d:/Viking/cipher_full_raw.mp4", shell=True, check=True)
    
    print("Muxing video with Andrew narration audio using FFmpeg...")
    ffmpeg_cmd = (
        'ffmpeg -y -i "d:/Viking/cipher_full_raw.mp4" -i "d:/Viking/master_narration_150s.m4a" '
        '-map 0:v:0 -map 1:a:0 -c:v libx264 -preset fast -crf 20 -r 30 -pix_fmt yuv420p '
        '-c:a aac -b:a 192k -t 150 -movflags +faststart "d:/Viking/cipher_demo.mp4"'
    )
    subprocess.run(ffmpeg_cmd, shell=True, check=True)
    
    # Copies
    subprocess.run('copy /Y "d:\\Viking\\cipher_demo.mp4" "d:\\Viking\\cipher_demo_2.mp4"', shell=True)
    subprocess.run('copy /Y "d:\\Viking\\cipher_demo.mp4" "C:\\Users\\LOQ\\.gemini\\antigravity-ide\\brain\\93b64f76-f436-4b4d-8040-df3896d2f718\\cipher_demo.mp4"', shell=True)
    subprocess.run('copy /Y "d:\\Viking\\cipher_demo.mp4" "C:\\Users\\LOQ\\.gemini\\antigravity-ide\\brain\\93b64f76-f436-4b4d-8040-df3896d2f718\\cipher_demo_2.mp4"', shell=True)
    
    dur = subprocess.check_output('ffprobe -v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1 "d:/Viking/cipher_demo.mp4"', shell=True).decode().strip()
    size = subprocess.check_output('ffprobe -v error -show_entries format=size -of default=noprint_wrappers=1:nokey=1 "d:/Viking/cipher_demo.mp4"', shell=True).decode().strip()
    print(f"SUCCESS! Master Video Finalized: Duration = {dur}s, Size = {int(size)/(1024*1024):.2f} MB")

if __name__ == "__main__":
    main()
