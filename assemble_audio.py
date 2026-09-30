import subprocess

starts = [0, 16500, 34000, 66000, 99000, 125000]
inputs = " ".join([f'-i "d:/Viking/audio_segments/seg{i+1}_synced.mp3"' for i in range(6)])

filter_complex = ""
for i in range(6):
    filter_complex += f"[{i}:a]adelay={starts[i]}|{starts[i]}[a{i}];"
filter_complex += "".join([f"[a{i}]" for i in range(6)]) + "amix=inputs=6:duration=longest:dropout_transition=0,apad=whole_dur=150[out]"

cmd = f'ffmpeg -y {inputs} -filter_complex "{filter_complex}" -map "[out]" -t 150 -c:a aac -b:a 192k "d:/Viking/master_narration_150s.m4a"'
print("Executing ffmpeg...")
subprocess.run(cmd, shell=True, check=True)

dur = subprocess.check_output('ffprobe -v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1 "d:/Viking/master_narration_150s.m4a"', shell=True).decode().strip()
print("Master narration 150s generated! Duration:", dur)
