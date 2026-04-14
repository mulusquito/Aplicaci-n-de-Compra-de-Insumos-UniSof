@echo off
chcp 65001 >nul
set "DIR=%~dp0..\src\main\resources\static\videos\lsv"
cd /d "%DIR%" || (echo No se encontró la carpeta videos\lsv & pause & exit /b 1)

where ffmpeg >nul 2>&1
if errorlevel 1 (
    echo No se encontró ffmpeg. Instálelo desde https://ffmpeg.org/download.html
    echo y añada la carpeta "bin" al PATH de Windows.
    pause
    exit /b 1
)

if not exist "0409(2).mp4" (
    echo No existe "0409(2).mp4" en:
    echo %CD%
    pause
    exit /b 1
)

echo Convirtiendo HEVC/H.265 a H.264 para Edge y Chrome...
ffmpeg -y -i "0409(2).mp4" -c:v libx264 -profile:v main -pix_fmt yuv420p -movflags +faststart -an "0409-2.mp4"
if errorlevel 1 ( echo Error en ffmpeg. & pause & exit /b 1 )

echo.
echo Listo: 0409-2.mp4 es H.264. Reinicie Spring Boot y pruebe http://localhost:8080/lsv-test.html
pause
