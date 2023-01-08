echo "rm ~/.android/avd/Pixel_XL_API_33.avd/*.lock"
echo "mksdcard -l mySdCard 1024M mySdCardFile.img"
echo "export PATH=/home/dmc7z/Android/Sdk/emulator:\$PATH"
echo "adb shell"
echo "sudo adb push 52096f49 /sdcard/Android/data/com.example.media/"
echo "emulator -avd Pixel_XL_API_33"


# SQLLite
# https://stackoverflow.com/questions/18370219/how-to-use-adb-in-android-studio-to-view-an-sqlite-db
echo "SQLLite"
echo "adb shell"
echo "su (first thing you do)"
echo "run-as com.example.media"
echo "cd data/data/com.example.media/databases/"
echo "sqlite3 <your-db-name>.db"
echo ".tables"
echo ".schema tablename"

echo "lsof -n -i:5037"
echo "lsof -i:5554"

echo "kill -9 125114"
echo "adb kill-server"
echo "adb start-server"

