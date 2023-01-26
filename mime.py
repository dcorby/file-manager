import re
import json
import pprint

directory = "/home/dmc7z/AndroidStudioProjects/FileSystem"

mime_types = {}
with open("/etc/mime.types") as fi:
    start = False
    for line in fi:
        line = line.strip()
        if line.startswith("application/"):
            start = True
        if line == "" or " " not in line or not start:
            continue
        mime, exts = re.split("\s+", line, maxsplit=1)
        for ext in exts.split():
            mime_types[ext] = mime

with open(f"{directory}/mime_types.json", "w", encoding="utf-8") as fo:
    json.dump(mime_types, fo, ensure_ascii=False, indent=2)
