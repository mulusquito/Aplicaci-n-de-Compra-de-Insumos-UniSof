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
  Catálogo "Ver catálogo": data-sign-src="/videos/lsv/0409-2.mp4"
  Icono iniciar sesión (header): data-sign-src="/videos/lsv/0409.mp4"

Chatbot Nova (hover sobre el icono flotante, chat.js — CHAT_LAUNCHER_LSV_CANDIDATES):
  Principal: 0409(16).mp4; respaldos: 0409-16.mp4, 0409.mp4, 0409-2.mp4.
  Debe ser H.264 para Edge/Chrome (imagen negra → convertir con ffmpeg como arriba).

URL directa en el navegador:
  http://localhost:8080/videos/lsv/0409-2.mp4
  http://localhost:8080/videos/lsv/0409.mp4
