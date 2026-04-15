@echo off
chcp 65001 >nul
set "DIR=%~dp0..\src\main\resources\static\videos\lsv"
cd /d "%DIR%" || (echo No se encontró la carpeta videos\lsv & pause & exit /b 1)

where ffmpeg >nul 2>&1
if errorlevel 1 (
    echo No se encontró ffmpeg en el PATH.
    echo Instálelo: https://ffmpeg.org/download.html  ^(añada la carpeta "bin" al PATH^)
    echo O con winget en PowerShell ^(admin opcional^): winget install Gyan.FFmpeg
    pause
    exit /b 1
)

if not exist "0409(16).mp4" (
    echo No existe "0409(16).mp4" en:
    echo %CD%
    pause
    exit /b 1
)

echo Convirtiendo 0409(16).mp4 a H.264 ^(soluciona pantalla negra en Chrome/Edge^)...
ffmpeg -y -i "0409(16).mp4" -c:v libx264 -profile:v main -pix_fmt yuv420p -movflags +faststart -an "0409(16)-h264.mp4"
if errorlevel 1 ( echo Error en ffmpeg. & pause & exit /b 1 )

echo.
echo Reemplazando archivo: el original HEVC queda como respaldo.
if exist "0409(16)-original-hevc-backup.mp4" del /f /q "0409(16)-original-hevc-backup.mp4"
move /Y "0409(16).mp4" "0409(16)-original-hevc-backup.mp4" >nul
move /Y "0409(16)-h264.mp4" "0409(16).mp4" >nul

copy /Y "0409(16).mp4" "0409-16.mp4" >nul
echo Listo. "0409(16).mp4" ahora es H.264. Respaldo: 0409(16)-original-hevc-backup.mp4
echo Copia para el chat ^(URL sin parentesis^): 0409-16.mp4
echo Reinicie Spring Boot y recargue el navegador con Ctrl+F5.
pause
