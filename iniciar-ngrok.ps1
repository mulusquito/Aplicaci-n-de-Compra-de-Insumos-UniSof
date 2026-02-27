# Script para iniciar ngrok - Ver NGROK_SETUP.md para instrucciones completas
# Requiere: ngrok configurado con authtoken

Write-Host ""
Write-Host "Iniciando ngrok (puerto 8080)..." -ForegroundColor Cyan
Write-Host "Copia la URL https que aparece y ponla en application.properties" -ForegroundColor Gray
Write-Host ""

$env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")
ngrok http 8080
