import asyncio
import os
import edge_tts
import subprocess

VOICE = "en-US-AndrewNeural"
OUTPUT_DIR = "d:/Viking/audio_segments"
os.makedirs(OUTPUT_DIR, exist_ok=True)

segments = [
    {
        "id": "seg1",
        "target_start": 0.0,
        "target_end": 16.0,
        "text": "Welcome. This is Cipher, an on-device AI mobile cyber defense agent engineered for Android. On our central telemetry radar, all six modular defense shields are armed—monitoring APK installations, UPI payment links, SMS payloads, call spoofing, NFC proximity, and permission escalations—running entirely client-side with zero network egress."
    },
    {
        "id": "seg2",
        "target_start": 16.0,
        "target_end": 33.0,
        "text": "Testing the UPI Link Guard. When a user encounters an obfuscated payment URL—here, a Cyrillic homoglyph IDN domain, xn--sbi-9da.com—our deterministic rule engine intercepts it in under 0.01 milliseconds. It flags an immediate high-risk homograph threat without pinging external servers."
    },
    {
        "id": "seg3",
        "target_start": 34.0,
        "target_end": 65.0,
        "text": "Moving into the isolated Threat Sandbox. Here, we trigger simulated real-world attack vectors against our local pipeline. Notice how banking SMS endpoints are validated, while NFC APDU relay timing anomalies trigger a critical hardware alert. Over-privileged background applications requesting persistent audio and SMS access are quarantined automatically."
    },
    {
        "id": "seg4",
        "target_start": 66.0,
        "target_end": 98.0,
        "text": "Next, our APK Binary Scanner evaluates raw sideloaded packages before execution. Ingesting a target application, our local neural engine parses the manifest and bytecode. It detects a version downgrade attack targeting installed state rollbacks, returning a high-severity verdict to abort installation instantly."
    },
    {
        "id": "seg5",
        "target_start": 99.0,
        "target_end": 124.0,
        "text": "All events are logged inside the AES-256 encrypted local incident vault. To counter the national fraud crisis during the critical one-hour golden period, Cipher provides one-tap actionable handoff to the National Cyber Crime Helpline."
    },
    {
        "id": "seg6",
        "target_start": 125.0,
        "target_end": 150.0,
        "text": "Under system protocols, Cipher confirms our on-device Gemma 270M INT4 weights are resident in memory, ensuring 100% DPDP Act 2023 compliance. A single tap generates an encrypted forensic audit zip report, while the emergency dialer automatically stages a direct call to helpline 1930. Complete, local cyber defense—zero server cost, absolute privacy."
    }
]

async def generate():
    for seg in segments:
        out_path = os.path.join(OUTPUT_DIR, f"{seg['id']}.mp3")
        print(f"Generating {seg['id']} with {VOICE}...")
        communicate = edge_tts.Communicate(seg["text"], VOICE, rate="+8%", pitch="+0Hz")
        await communicate.save(out_path)
        
        # probe duration
        cmd = f'ffprobe -v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1 "{out_path}"'
        dur = subprocess.check_output(cmd, shell=True).decode().strip()
        print(f"[{seg['id']}] Generated: {dur}s (Target window: {seg['target_start']}s -> {seg['target_end']}s)")

if __name__ == "__main__":
    asyncio.run(generate())
