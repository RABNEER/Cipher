import subprocess
import os

configs = [
    ('seg1', 1.44),  # 22.82s / 1.44 = 15.85s -> fits perfectly in [00:00 - 00:16]
    ('seg2', 1.30),  # 21.43s / 1.30 = 16.48s -> fits perfectly in [00:16 - 00:33]
    ('seg3', 1.00),  # 23.04s / 1.00 = 23.04s -> fits perfectly in [00:34 - 01:05] (31s window)
    ('seg4', 1.00),  # 21.07s / 1.00 = 21.07s -> fits perfectly in [01:06 - 01:38] (32s window)
    ('seg5', 1.00),  # 15.65s / 1.00 = 15.65s -> fits perfectly in [01:39 - 02:04] (25s window)
    ('seg6', 1.15),  # 27.48s / 1.15 = 23.89s -> fits perfectly in [02:05 - 02:30] (25s window)
]

for name, tempo in configs:
    in_file = f"d:/Viking/audio_segments/{name}.mp3"
    out_file = f"d:/Viking/audio_segments/{name}_synced.mp3"
    if tempo == 1.0:
        cmd = f'ffmpeg -y -i "{in_file}" -c:a copy "{out_file}"'
    else:
        cmd = f'ffmpeg -y -i "{in_file}" -filter:a "atempo={tempo}" -c:a mp3 "{out_file}"'
    subprocess.run(cmd, shell=True, check=True)
    dur = float(subprocess.check_output(f'ffprobe -v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1 "{out_file}"', shell=True).decode().strip())
    print(f"{name}_synced: duration = {dur:.2f}s (tempo={tempo})")
