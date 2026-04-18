# Copia Dashboard.mp4 a la ruta principal que usa la app (LSV en classpath:/static/videos/lsv/).
# Uso:
#   .\scripts\ubicar-dashboard-en-lsv.ps1
#   .\scripts\ubicar-dashboard-en-lsv.ps1 -Origen "C:\ruta\Dashboard.mp4"
param(
    [string] $Origen = ""
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path $PSScriptRoot -Parent
if (-not (Test-Path (Join-Path $repoRoot "pom.xml"))) {
    Write-Error "No se encontro pom.xml en $repoRoot (ejecute el script desde el repo)."
    exit 1
}
$destDir = Join-Path $repoRoot "src\main\resources\static\videos\lsv"
$dest = Join-Path $destDir "Dashboard.mp4"
$candidatos = @()
if ($Origen) { $candidatos += $Origen }
$videosWin = [Environment]::GetFolderPath("MyVideos")
if ($videosWin) { $candidatos += (Join-Path $videosWin "Dashboard.mp4") }
$candidatos += @(
    (Join-Path $repoRoot "src\main\resources\static\videos\Dashboard.mp4"),
    (Join-Path $repoRoot "src\main\resources\static\Dashboard.mp4")
)
$desk = [Environment]::GetFolderPath("Desktop")
if ($desk) { $candidatos += (Join-Path $desk "Dashboard.mp4") }

if (-not (Test-Path $destDir)) {
    New-Item -ItemType Directory -Path $destDir -Force | Out-Null
}

$origenUsado = $null
foreach ($c in $candidatos) {
    if ($c -and (Test-Path -LiteralPath $c)) {
        $origenUsado = $c
        break
    }
}

if (-not $origenUsado) {
    Write-Host "No se encontro Dashboard.mp4. Ruta principal de destino en el repo:" -ForegroundColor Yellow
    Write-Host "  $dest" -ForegroundColor Cyan
    Write-Host "Busque el archivo en (o use -Origen):" -ForegroundColor Yellow
    if ($videosWin) { Write-Host "  $(Join-Path $videosWin 'Dashboard.mp4')" -ForegroundColor Gray }
    Write-Host "  $(Join-Path $repoRoot 'src\main\resources\static\videos\Dashboard.mp4')" -ForegroundColor Gray
    Write-Host "  $(Join-Path $repoRoot 'src\main\resources\static\Dashboard.mp4')" -ForegroundColor Gray
    if ($desk) { Write-Host "  $(Join-Path $desk 'Dashboard.mp4')" -ForegroundColor Gray }
    Write-Host 'Ejemplo: .\scripts\ubicar-dashboard-en-lsv.ps1 -Origen "C:\Users\...\Dashboard.mp4"' -ForegroundColor Yellow
    exit 1
}

Copy-Item -LiteralPath $origenUsado -Destination $dest -Force
Write-Host "OK: copiado" -ForegroundColor Green
Write-Host "  Desde: $origenUsado"
Write-Host "  Hasta: $dest"
Write-Host "URL de prueba (con la app en marcha): http://localhost:8080/videos/lsv/Dashboard.mp4"
