from cairosvg import svg2png
import zipfile
from pathlib import Path

"""
Generic file (file.svg) from https://upload.wikimedia.org/wikipedia/commons/9/9c/Generic_File_OneDrive_icon.svg
Set of supported extensions from https://github.com/dmhendricks/file-icon-vectors
"""

base = "/home/dmc7z/AndroidStudioProjects/FileSystem/icons"

with zipfile.ZipFile(f"{base}/icons.zip", "r") as f:
    f.extractall(f"{base}/svg")

svg = {}
supported = ["file", "classic/mp3", "classic/pdf", "classic/folder"]
for x in supported:
    files = list(Path(base).rglob(f"*/{x}.svg"))
    if len(files) != 1:
        raise Exception(f"Zero or multiple files found for pattern={x}")
    pathname = str(files[0])
    name, _ = pathname.split("/")[-1].rsplit(".", 1)
    f = open(pathname)
    contents = f.read()
    svg[name] = contents
    f.close()

print(svg)
# https://developer.android.com/training/multiscreen/screendensities
# Android suggests 36x36, 48x48, 72x72, 96x96, 144x144, and 192x192
for name in svg:
    svg2png(bytestring=svg, write_to=f"{name}.png")
