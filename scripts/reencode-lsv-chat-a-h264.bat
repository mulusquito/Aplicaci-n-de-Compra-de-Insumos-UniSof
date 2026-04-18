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

set "CHAT_LSV=0417 (1)(3).mp4"
if not exist "%CHAT_LSV%" (
    echo No existe "%CHAT_LSV%" en:
    echo %CD%
    pause
    exit /b 1
)

echo Convirtiendo %CHAT_LSV% a H.264 ^(soluciona pantalla negra en Chrome/Edge^)...
ffmpeg -y -i "%CHAT_LSV%" -c:v libx264 -profile:v main -pix_fmt yuv420p -movflags +faststart -an "0417 (1)(3)-h264-temp.mp4"
if errorlevel 1 ( echo Error en ffmpeg. & pause & exit /b 1 )

echo.
echo Reemplazando archivo: el original queda como respaldo.
if exist "0417 (1)(3)-original-hevc-backup.mp4" del /f /q "0417 (1)(3)-original-hevc-backup.mp4"
move /Y "%CHAT_LSV%" "0417 (1)(3)-original-hevc-backup.mp4" >nul
move /Y "0417 (1)(3)-h264-temp.mp4" "%CHAT_LSV%" >nul

echo Listo. "%CHAT_LSV%" ahora es H.264. Respaldo: 0417 (1)(3)-original-hevc-backup.mp4
echo Reinicie Spring Boot y recargue el navegador con Ctrl+F5.
pause
