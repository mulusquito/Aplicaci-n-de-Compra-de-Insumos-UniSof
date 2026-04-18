# Paso 2 del flujo LSV (ver videos/lsv/README.txt — CONVENCIÓN UNISOF):
# Copia a videos/lsv del repo los MP4 que existan en la biblioteca «Vídeos» de Windows,
# con el MISMO nombre que usa la app (data-sign-src / chat.js).
# Paso 3 (cierre): este script NO convierte. Tras copiar, ffprobe + ffmpeg a H.264 si hace falta (README checklist).
param(
    [switch] $DryRun
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path $PSScriptRoot -Parent
if (-not (Test-Path (Join-Path $repoRoot "pom.xml"))) {
    Write-Error "Ejecute el script desde la carpeta scripts del repositorio."
    exit 1
}
$srcDir = [Environment]::GetFolderPath("MyVideos")
$destDir = Join-Path $repoRoot "src\main\resources\static\videos\lsv"

$nombresReferenciados = @(
    "0409.mp4", "0409-2.mp4", "ver catalogo (1).mp4",
    "0409(11).mp4", "0409(12).mp4", "0409(13).mp4", "0409(14).mp4", "0409(15).mp4",
    "0409(17).mp4", "0409(18).mp4", "0409(20).mp4",
    "0417.mp4", "0418.mp4", "0418(1).mp4", "0417 (1)(1).mp4", "0417 (1)(2).mp4", "0417 (1)(3).mp4", "0417(6).mp4", "0417(7).mp4", "0417(10).mp4",
    "Dashboard.mp4", "Personal.mp4", "Administrador.mp4", "vendedor.mp4", "Jefe de compras.mp4", "codigo 6 digitos.mp4", "Verificar.mp4", "Consultar Compras insumos.mp4", "Cerrar Sesion.mp4", "Clientes.mp4", "Pedidos (1).mp4", "Pedidos.mp4", "Ventas.mp4", "proveedores.mp4"
) | Select-Object -Unique

if (-not (Test-Path -LiteralPath $srcDir)) {
    Write-Error "No se encontro la carpeta Vídeos: $srcDir"
    exit 1
}
if (-not (Test-Path $destDir)) {
    New-Item -ItemType Directory -Path $destDir -Force | Out-Null
}

$copiados = 0
$noEnVideos = 0
foreach ($nombre in $nombresReferenciados) {
    $src = Join-Path $srcDir $nombre
    if (-not (Test-Path -LiteralPath $src)) {
        $noEnVideos++
        continue
    }
    $dest = Join-Path $destDir $nombre
    if ($DryRun) {
        Write-Host "[dry-run] copiaria: $nombre"
    } else {
        Copy-Item -LiteralPath $src -Destination $dest -Force
        Write-Host "OK: $nombre"
        $copiados++
    }
}

Write-Host ""
Write-Host "Origen: $srcDir"
Write-Host "Destino: $destDir"
if ($DryRun) {
    Write-Host "Modo simulacion (-DryRun). Quitar -DryRun para copiar."
} else {
    Write-Host "Copiados: $copiados  |  no estaban en Vídeos (omitidos): $noEnVideos"
    Write-Host "Si algún archivo es hevc, convierta a H.264 (README en videos/lsv)."
}
