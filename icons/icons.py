from cairosvg import svg2png
import zipfile
from pathlib import Path

"""
Generic file (file.svg) from https://upload.wikimedia.org/wikipedia/commons/9/9c/Generic_File_OneDrive_icon.svg
Set of supported extensions from https://github.com/dmhendricks/file-icon-vectors
"""

base  =  "/home/dmc7z/AndroidStudioProjects/FileSystem"
icons = f"{base}/icons"
res   = f"{base}/app/src/main/res"

# https://developer.android.com/training/multiscreen/screendensities
# Android suggests 36x36, 48x48, 72x72, 96x96, 144x144, and 192x192
sizes = {
    "-ldpi"   : 36,
    "-mdpi"   : 48,
    ""        : 48,
    "-hdpi"   : 72,
    "-xhdpi"  : 96,
    "-xxhdpi" : 144,
    "-xxxhdpi": 192
}

with zipfile.ZipFile(f"{icons}/icons.zip", "r") as f:
    f.extractall(f"{icons}/svg")

svg = {}
supported = ["file", "classic/mp3", "classic/pdf", "classic/folder", "classic/txt"]
for x in supported:
    files = list(Path(icons).rglob(f"*/{x}.svg"))
    if len(files) != 1:
        raise Exception(f"Zero or multiple files found for pattern={x}")
    pathname = str(files[0])
    name, _ = pathname.split("/")[-1].rsplit(".", 1)
    f = open(pathname)
    contents = f.read()
    svg[name] = contents
    f.close()

# Create the folders
for size in sizes:
    pathname = f"{res}/drawable{size}"
    Path(pathname).mkdir(parents=False, exist_ok=True)
    
# Create the files
for name in svg:
    for size in sizes:
        pathname = f"{res}/drawable{size}/{name}.png"
        svg2png(bytestring=svg[name], write_to=pathname, output_width=sizes[size], output_height=sizes[size])
