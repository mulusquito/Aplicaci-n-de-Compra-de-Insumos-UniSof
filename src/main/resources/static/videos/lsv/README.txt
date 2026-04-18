Vídeos LSV (lengua de señas) — para la WEB deben ser MP4 en H.264 (no H.265/HEVC).

CONVENCIÓN UNISOF (origen, rutas y cierre)
  1) Origen: los MP4 nuevos viven primero en la biblioteca «Vídeos» de Windows del equipo
     (p. ej. C:\Users\<usuario>\Videos\), con el nombre final que tendrán en la app.
  2) Rutas en la aplicación: el HTML usa siempre la misma ruta pública bajo Spring Boot:
       /videos/lsv/<mismo nombre del archivo>.mp4
     El archivo físico en el repo va en:
       src/main/resources/static/videos/lsv/<mismo nombre>.mp4
     Copia desde Vídeos hacia esa carpeta con:
       powershell -File scripts\sync-lsv-desde-videos-windows.ps1
  3) Cierre obligatorio: al terminar de traer o actualizar un MP4 al repo, comprobar códec
     con ffprobe; si es hevc / pantalla negra en el navegador, convertir a H.264 con ffmpeg
     (CHECKLIST de abajo) y volver a comprobar hasta ver h264. Reiniciar Spring Boot y Ctrl+F5.

CHECKLIST — cada archivo nuevo (ej. 0409(15).mp4)
  1) Copiar el MP4 a esta carpeta.
  2) Comprobar códec (usar -i delante del archivo; NO usar -LiteralPath: eso es solo de Copy-Item en PowerShell):
       ffprobe -v error -select_streams v:0 -show_entries stream=codec_name -of default=noprint_wrappers=1:nokey=1 -i "ARCHIVO.mp4"
     Si sale "hevc" → hay que convertir (no se ve bien en el navegador).
  3) Respaldo + conversión (sustituye ARCHIVO):
       Copy-Item -LiteralPath "ARCHIVO" -Destination "ARCHIVO-original-hevc-backup.mp4"
       ffmpeg -y -i "ARCHIVO" -c:v libx264 -profile:v main -pix_fmt yuv420p -movflags +faststart -an "ARCHIVO-temp-h264.mp4"
       Move-Item -LiteralPath "ARCHIVO-temp-h264.mp4" -Destination "ARCHIVO" -Force
  4) ffprobe otra vez → debe decir "h264". Reiniciar Spring Boot y Ctrl+F5 en el navegador.

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
  0409-2.mp4      — salida típica del reencode H.264 desde 0409(2).mp4 (scripts/reencode-lsv-a-h264.bat); legado si ya no enlaza la UI
  «ver catalogo (1).mp4» — botón «Ver Catálogo» en index.html (LSV; espacios y paréntesis en el nombre de archivo)

Ruta en el proyecto:
  src/main/resources/static/videos/lsv/

(Detalle del paso 2) Nombre en Vídeos = nombre en lsv = último segmento de /videos/lsv/… en HTML.
  Ejemplos: Dashboard.mp4, Personal.mp4, Administrador.mp4, vendedor.mp4, Jefe de compras.mp4, ver catalogo (1).mp4, codigo 6 digitos.mp4, Verificar.mp4, Consultar Compras insumos.mp4, Cerrar Sesion.mp4, Clientes.mp4, Pedidos (1).mp4, Pedidos.mp4, 0418.mp4, 0418(1).mp4, Ventas.mp4, proveedores.mp4.
  Espacios en el nombre: sign-language-bubble.js codifica la URL al cargar el <video>.

En index.html / ventas.html:
  Catálogo «Ver Catálogo» (index.html, botón .btn-catalogo): data-sign-src="/videos/lsv/ver catalogo (1).mp4" (element, tamaño 140; sustituye a 0409-2.mp4).
  Título sección "Caballeros" (index y ventas): data-sign-src="/videos/lsv/0409(20).mp4" (misma burbuja LSV, a la derecha del texto)
  Icono iniciar sesión (header index): data-sign-src="/videos/lsv/0409.mp4" + sign-language-bubble.js

Archivo 0409(11).mp4 (enlace «¿Olvidaste la contraseña?» en login.html, hover):
  Misma regla: H.264 para web; si hevc → ffmpeg. Respaldo HEVC opcional: 0409(11)-original-hevc-backup.mp4

Archivo 0409(7).mp4 (campo «contraseña» en login.html, hover):
  Misma regla: H.264 para web; si hevc → ffmpeg. Respaldo HEVC opcional: 0409(7)-original-hevc-backup.mp4

Archivo 0418(1).mp4 (sidebar Jefe de Compras «Órdenes de compra» → /ordenes-compra.html):
  H.264 para web, sin audio. data-sign-position="element-right" (mismo ítem que Pedidos / Análisis en compras, ordenes-compras, analisis-insumos, inventario-insumos vista compras, reporte-faltantes, ordenes-compra, fichas-tecnicas, facturas-proveedores).
  Paréntesis en el nombre: URL codificada por sign-language-bubble.js al cargar el video.
  Repo: src/main/resources/static/videos/lsv/0418(1).mp4  |  URL: /videos/lsv/0418(1).mp4
  Sustituye al clip anterior 0409(24).mp4 para ese menú.

Archivo «0417 (1)(2).mp4» (sidebar administrador «Consultar órdenes de compra» → /ordenes.html):
  — panel-admin, personal, proveedores, inventario-insumos (nav admin), ordenes.html, facturas-proveedores (nav JS admin).
  — LSV: data-sign-position="element-right" y data-sign-size="140" (alineado con Personal / Compras insumos).
  Origen típico: Vídeos de Windows; en el repo se entrega H.264 (yuv420p) vía ffmpeg si el origen es HEVC.

Archivo 0417.mp4 (sidebar Jefe de Compras «Reporte faltantes» → /reporte-faltantes.html):
  H.264 para web. Respaldo HEVC si aplica: 0417-original-hevc-backup.mp4

Archivo 0418.mp4 (sidebar Jefe de Compras «Análisis de insumos» → /analisis-insumos.html):
  H.264 para web, sin audio. data-sign-position="element-right" (mismo ítem en compras, ordenes-compras, inventario-insumos vista compras, reporte-faltantes, ordenes-compra, fichas-tecnicas, facturas-proveedores).
  Repo: src/main/resources/static/videos/lsv/0418.mp4  |  URL: /videos/lsv/0418.mp4
  Sustituye al clip anterior 0417(1).mp4 para ese menú.

Archivo «0417 (1)(1).mp4» (espacio tras 0417; no confundir con 0418.mp4 del ítem «Análisis de insumos»):
  — Sidebar administrador «Consultar inventario» → /inventario-insumos.html?ctx=admin
  — Sidebar Jefe de Compras «Inventario» → /inventario-insumos.html (mismo MP4; data-sign-position="element-right", sin data-sign-size, como Pedidos / Análisis en ese menú)
  — En admin: data-sign-size="140" (igual que Personal / Proveedores; la burbuja a la derecha del ítem).
  Origen típico: Vídeos de Windows. sign-language-bubble.js codifica espacios y paréntesis en la URL.
  En el repo se entrega en H.264 (yuv420p); si reemplazas el MP4 y viene en HEVC, convertir con ffmpeg (libx264, -movflags +faststart, p. ej. CRF 23).

Archivo 0417(3).mp4 — opcional / legado; ya no enlazado en la UI para inventario (en «Consultar inventario» se usa «0417 (1)(1).mp4»).
  Si conservas el archivo en la carpeta, H.264 para web. Respaldo HEVC si aplica: 0417(3)-original-hevc-backup.mp4

Archivo «Pedidos (1).mp4» (sidebar Jefe de Compras «Pedidos» → /ordenes-compras.html):
  H.264 para web, sin audio. data-sign-position="element-right" (mismo ítem en compras, analisis-insumos, inventario-insumos vista compras, reporte-faltantes, ordenes-compra, fichas-tecnicas, facturas-proveedores).
  Nombre en disco: Pedidos (1).mp4 (espacio antes del paréntesis; sign-language-bubble.js codifica la URL).
  Repo: src/main/resources/static/videos/lsv/Pedidos (1).mp4  |  URL: /videos/lsv/Pedidos (1).mp4
  Sustituye al clip anterior 0417(4).mp4 para ese menú (el archivo 0417(4).mp4 en lsv puede conservarse como respaldo o eliminarse si ya no se usa).

Archivo 0417(6).mp4 (título y botón «Iniciar Sesión» en login.html):
  H.264 para web. Respaldo HEVC: 0417(6)-original-hevc-backup.mp4

Archivo 0417(7).mp4 — «Volver al inicio» / «Volver al login»:
  H.264 para web. Respaldo HEVC: 0417(7)-original-hevc-backup.mp4
  Asignación automática en sign-language-bubble.js:
  — a[href="/"] con data-i18n-key="auth.back" o texto «← Volver al inicio» / «← Back to home»
  — a[href="/login.html"] con data-i18n-key="auth.loginBack" o texto «← Volver al login» / «← Back to login»
    (recuperar-contrasena, restablecer-contrasena, verificar-token).

Archivo 0417(10).mp4 (enlace «Manual de Usuario» en index.html, burbuja LSV debajo como «Ver Catálogo»):
  H.264 para web. Respaldo HEVC: 0417(10)-original-hevc-backup.mp4

Archivo 0409(12).mp4 (campo de texto «usuario» en login.html, hover):
  Misma regla: H.264 para web; si hevc → ffmpeg. Respaldo HEVC opcional: 0409(12)-original-hevc-backup.mp4

Archivo «codigo 6 digitos.mp4» (input #token — código de 6 dígitos en verificar-token.html, verificación en dos pasos):
  H.264 para web, sin audio. data-sign-position="element-right" (misma línea que campos de login).
  Nombre en disco: codigo 6 digitos.mp4 (espacios; sin tilde en «digitos»).
  Repo: src/main/resources/static/videos/lsv/codigo 6 digitos.mp4  |  URL: /videos/lsv/codigo 6 digitos.mp4

Archivo Verificar.mp4 (botón «Verificar» #btn-verificar en verificar-token.html):
  H.264 para web, sin audio. data-sign-position="element-right", data-sign-halign-with="#token" (columna del campo código).
  Repo: src/main/resources/static/videos/lsv/Verificar.mp4  |  URL: /videos/lsv/Verificar.mp4

Archivo 0409(18).mp4 (título sección «Damas», index.html y ventas.html):
  Misma regla: H.264 para web; si hevc → ffmpeg. Respaldo HEVC opcional: 0409(18)-original-hevc-backup.mp4

Archivo 0409(13).mp4 (títulos «Pantalones» en catálogo Caballeros/Damas, index.html y ventas.html):
  Misma regla: H.264 para web; si hevc → ffmpeg. Respaldo HEVC opcional: 0409(13)-original-hevc-backup.mp4

Archivo 0409(17).mp4 (títulos «Chaquetas» en catálogo Caballeros/Damas, index.html y ventas.html):
  Misma regla: H.264 para web; si hevc → ffmpeg. Respaldo HEVC opcional: 0409(17)-original-hevc-backup.mp4

Archivo 0409(15).mp4 (títulos «Camisas» en catálogo Caballeros/Damas, index.html y ventas.html):
  Misma regla: H.264 para web; si hevc → ffmpeg. Respaldo HEVC opcional: 0409(15)-original-hevc-backup.mp4

Archivo 0409(14).mp4 (títulos «Blaizer» en catálogo Caballeros/Damas, index.html y ventas.html):
  Misma regla: H.264 para web. Si ffprobe dice hevc, convertir con ffmpeg (como abajo).
  Copia el archivo a esta carpeta; respaldo HEVC opcional: 0409(14)-original-hevc-backup.mp4

Archivo 0409(20).mp4 (título Caballeros en index/ventas):
  Debe ser H.264 en el repo para que se vea en el navegador (HEVC → pantalla negra).
  Copia de seguridad HEVC si se re-codifica: 0409(20)-original-hevc-backup.mp4
  Comando típico:
  ffmpeg -y -i "0409(20).mp4" -c:v libx264 -profile:v main -pix_fmt yuv420p -movflags +faststart -an "0409-20.mp4"
  (Opcional: usar data-sign-src="/videos/lsv/0409-20.mp4" si prefiere URL sin paréntesis.)

Archivo Dashboard.mp4 (enlace «Dashboard»: administrador → /panel-admin.html; Jefe de Compras → /compras.html):
  H.264 para web. data-sign-position="element-right" (como el resto del menú LSV), data-sign-size="140".
  Rutas HTML: panel-admin, personal, proveedores, inventario-insumos (nav admin), ordenes, facturas-proveedores (nav inyectado si rol ADMINISTRADOR).
  Mismo MP4 y atributos en el sidebar Jefe de Compras: compras, ordenes-compras, analisis-insumos, inventario-insumos (vista compras), reporte-faltantes, ordenes-compra, fichas-tecnicas, facturas-proveedores (nav Jefe de Compras por defecto).
  Rutas principales (Spring Boot — el MP4 debe estar aquí; la app sirve desde classpath:/static/):
    • En el disco (repo):  src/main/resources/static/videos/lsv/Dashboard.mp4
    • URL pública:        /videos/lsv/Dashboard.mp4
    • Prueba en navegador: http://localhost:8080/videos/lsv/Dashboard.mp4
  Si el MP4 está en la biblioteca «Vídeos» de Windows (p. ej. C:\Users\…\Videos\Dashboard.mp4),
  ejecute desde la raíz del repo:  powershell -File scripts\ubicar-dashboard-en-lsv.ps1
  (el script copia desde Vídeos, Escritorio o static/videos/ hacia videos/lsv/).
  Respaldo HEVC si convirtió a H.264: Dashboard-original-hevc-backup.mp4

Archivo Personal.mp4 (enlace «Personal» del sidebar administrador → /personal.html):
  H.264 para web. data-sign-position="element-right", data-sign-size="140".
  Rutas HTML: panel-admin, personal, proveedores, inventario-insumos (nav admin), ordenes, facturas-proveedores (nav inyectado si rol ADMINISTRADOR).
  Ruta principal (repo): src/main/resources/static/videos/lsv/Personal.mp4  |  URL: /videos/lsv/Personal.mp4
  Respaldo HEVC si convirtió a H.264: Personal-original-hevc-backup.mp4

Archivo «Consultar Compras insumos.mp4» (enlace «Consultar compras insumos» → /facturas-proveedores.html):
  H.264 para web. data-sign-position="element-right", data-sign-size="140".
  Rutas HTML: panel-admin, personal, proveedores, inventario-insumos (nav admin), ordenes, facturas-proveedores (nav inyectado si rol ADMINISTRADOR).
  Nombre en disco (con espacios): Consultar Compras insumos.mp4
  Ruta principal (repo): src/main/resources/static/videos/lsv/Consultar Compras insumos.mp4
  URL pública (espacios codificados en el navegador): /videos/lsv/Consultar%20Compras%20insumos.mp4
  sign-language-bubble.js codifica segmentos del path al cargar el <video>.
  Respaldo HEVC si aplica: Consultar Compras insumos-original-hevc-backup.mp4

Archivo proveedores.mp4 (enlace «Proveedores» del sidebar administrador → /proveedores.html):
  H.264 para web. data-sign-position="element-right", data-sign-size="140".
  Rutas HTML: panel-admin, personal, proveedores, inventario-insumos (nav admin), ordenes, facturas-proveedores (nav inyectado si rol ADMINISTRADOR).
  Nombre en disco (minúsculas): proveedores.mp4
  Ruta principal (repo): src/main/resources/static/videos/lsv/proveedores.mp4  |  URL: /videos/lsv/proveedores.mp4
  Respaldo HEVC si aplica: proveedores-original-hevc-backup.mp4

Archivo «Cerrar Sesion.mp4» (botón «Cerrar sesión» en sidebars y menú usuario del catálogo):
  H.264 para web. data-sign-position="element-right", data-sign-size="140".
  Rutas HTML: panel-admin, personal, proveedores, ordenes, ventas, clientes, inventario-insumos, facturas-proveedores,
  compras, analisis-insumos, reporte-faltantes, ordenes-compra, ordenes-compras, fichas-tecnicas; index.html (dropdown usuario).
  Nombre en disco (espacio entre palabras, sin tilde en «Sesion»): Cerrar Sesion.mp4
  Repo: src/main/resources/static/videos/lsv/Cerrar Sesion.mp4  |  URL: /videos/lsv/Cerrar Sesion.mp4 (codificación de espacio en carga vía sign-language-bubble.js).

Archivo Ventas.mp4 (sidebar VENDEDOR, enlace «Ventas» → /ventas.html):
  H.264 para web. data-sign-position="element-right", data-sign-size="140" (misma línea que el menú admin).
  Rutas HTML: ventas.html, clientes.html, ordenes.html (nav VENDEDOR + initUnisofSignLanguageBubble tras /api/auth/me).
  Repo: src/main/resources/static/videos/lsv/Ventas.mp4  |  URL: /videos/lsv/Ventas.mp4

Archivo Clientes.mp4 (sidebar VENDEDOR, enlace «Clientes» → /clientes.html):
  H.264 para web. data-sign-position="element-right", data-sign-size="140".
  Rutas HTML: ventas.html, clientes.html; ordenes.html (nav VENDEDOR + initUnisofSignLanguageBubble tras auth).
  Repo: src/main/resources/static/videos/lsv/Clientes.mp4  |  URL: /videos/lsv/Clientes.mp4

Archivo Administrador.mp4 (etiqueta de rol «ADMINISTRADOR» en el sidebar):
  H.264 para web (misma regla que el resto de LSV). data-sign-position="element-top" (burbuja encima del nombre y del rol, sin tapar «Cerrar sesión»).
  data-sign-gap="28" y data-sign-size="140". sign-language-bubble.js evita solapar #user-name y el botón de cerrar sesión.
  Asignación automática: en cualquier página con #user-rol.admin-user-rol y texto «ADMINISTRADOR», sign-language-bubble.js
  aplica data-sign-src y enlaza el hover/táctil (MutationObserver al cambiar el texto tras /api/auth/me). Los roles «VENDEDOR» y «JEFE DE COMPRAS» usan el mismo patrón con sus MP4 (vendedor.mp4, Jefe de compras.mp4).
  Tras reemplazar HTML del sidebar, puede llamarse window.syncUnisofAdminUserRolLsv() o initUnisofSignLanguageBubble().
  Ruta principal (repo): src/main/resources/static/videos/lsv/Administrador.mp4  |  URL: /videos/lsv/Administrador.mp4
  Copiar el MP4 a la carpeta lsv si aún no está.
  Si el original era HEVC, respaldo en repo: Administrador-original-hevc-backup.mp4 (fuente antes de ffmpeg a H.264).

Archivo vendedor.mp4 (etiqueta de rol «VENDEDOR» en el sidebar — ventas.html, clientes.html, ordenes.html):
  H.264 para web. Misma colocación que Administrador.mp4: element-top, gap 28, tamaño 140 (sign-language-bubble.js, rama VENDEDOR en syncAdminUserRolLsvAttributes).
  Repo: src/main/resources/static/videos/lsv/vendedor.mp4  |  URL: /videos/lsv/vendedor.mp4

Archivo «Jefe de compras.mp4» (etiqueta de rol «JEFE DE COMPRAS» en el sidebar del módulo de compras):
  H.264 para web, sin audio. Misma colocación que Administrador.mp4 / vendedor.mp4: element-top, gap 28, tamaño 140 (sign-language-bubble.js, rama JEFE DE COMPRAS).
  Asignación automática con #user-rol.admin-user-rol y texto «JEFE DE COMPRAS» (normalizado a mayúsculas; p. ej. «Jefe de Compras» desde la API).
  Rutas HTML: compras.html, ordenes-compras.html, analisis-insumos.html, inventario-insumos.html (vista compras), reporte-faltantes.html, ordenes-compra.html, fichas-tecnicas.html, facturas-proveedores.html; tras /api/auth/me o initUnisofSignLanguageBubble.
  Nombre en disco: Jefe de compras.mp4 (espacios; sign-language-bubble.js codifica la URL).
  Repo: src/main/resources/static/videos/lsv/Jefe de compras.mp4  |  URL: /videos/lsv/Jefe de compras.mp4

Chatbot Nova (hover sobre el icono flotante, chat.js — CHAT_LAUNCHER_LSV_CANDIDATES):
  Clip único: «0417 (1)(3).mp4» en /videos/lsv/ (misma codificación de path que sign-language-bubble.js vía normalizeLsvVideoSrc en chat.js).
  Colocación: a la izquierda del botón Nova (chat.css — .chat-widget-launcher-lsv-stack); tamaño coherente con --sign-bubble-size.
  Debe ser H.264 para Edge/Chrome (si el origen es HEVC, convertir con ffmpeg como en el checklist).

URL directa en el navegador:
  http://localhost:8080/videos/lsv/0409-2.mp4
  http://localhost:8080/videos/lsv/ver%20catalogo%20(1).mp4
  http://localhost:8080/videos/lsv/0409.mp4
  http://localhost:8080/videos/lsv/Dashboard.mp4
  http://localhost:8080/videos/lsv/Personal.mp4
  http://localhost:8080/videos/lsv/Consultar%20Compras%20insumos.mp4
  http://localhost:8080/videos/lsv/proveedores.mp4
  http://localhost:8080/videos/lsv/Ventas.mp4
  http://localhost:8080/videos/lsv/Clientes.mp4
  http://localhost:8080/videos/lsv/Pedidos.mp4
  http://localhost:8080/videos/lsv/Pedidos%20(1).mp4
  http://localhost:8080/videos/lsv/0418.mp4
  http://localhost:8080/videos/lsv/Jefe%20de%20compras.mp4
  http://localhost:8080/videos/lsv/0418(1).mp4
