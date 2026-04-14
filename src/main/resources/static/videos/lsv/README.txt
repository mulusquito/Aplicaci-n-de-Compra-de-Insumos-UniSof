Vídeos LSV (lengua de señas) — para la WEB deben ser MP4 en H.264 (no H.265/HEVC).

IMPORTANTE — PANTALLA NEGRA EN EDGE / CHROME
  El archivo 0409(2).mp4 del proyecto está en códec HEVC (hvc1). Los navegadores suelen
  mostrar duración y controles pero NO la imagen. Hay que convertir a H.264.

  Opción A — doble clic en Windows (desde la raíz del proyecto):
    scripts\reencode-lsv-a-h264.bat
    (requiere ffmpeg en el PATH; lee 0409(2).mp4 y genera/sobrescribe 0409-2.mp4)

  Opción B — línea de comandos en esta carpeta:
    ffmpeg -y -i "0409(2).mp4" -c:v libx264 -profile:v main -pix_fmt yuv420p -movflags +faststart -an "0409-2.mp4"

  Luego reinicie Spring Boot y en el navegador use Ctrl+F5.

  Página de prueba (con la app en marcha):
    http://localhost:8080/lsv-test.html

Archivos:
  0409(2).mp4     — original (puede ser HEVC; conservar como fuente)
  0409-2.mp4      — usar en la app tras conversión a H.264 (nombre sin paréntesis en la URL)

Ruta en el proyecto:
  src/main/resources/static/videos/lsv/

En index.html:
  data-sign-src="/videos/lsv/0409-2.mp4"

URL directa en el navegador:
  http://localhost:8080/videos/lsv/0409-2.mp4
