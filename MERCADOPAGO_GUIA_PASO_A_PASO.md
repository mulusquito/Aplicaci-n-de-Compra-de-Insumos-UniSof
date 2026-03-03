# Guía minuciosa: Hacer que Mercado Pago Sandbox funcione

Sigue **cada paso en orden**. No saltes ninguno.

---

## PARTE 1: Crear cuenta comprador de prueba (OBLIGATORIO)

El pago con tarjeta como invitado falla a menudo en sandbox por restricciones del navegador. La opción más estable es pagar con una **cuenta comprador de prueba**.

### Paso 1.1: Ir al panel de desarrolladores

1. Abre Chrome (ventana normal, **no incógnito**).
2. Ve a: **https://www.mercadopago.com.co/developers/panel/app**
3. Inicia sesión con tu cuenta de Mercado Pago (alfonso.ocampom@uqvirtual.edu.co o la que uses).

### Paso 1.2: Crear cuenta comprador

1. En el menú izquierdo, busca **"Cuentas de prueba"** o **"Test users"**.
2. Haz clic en **"Crear cuenta de prueba"** o **"Create test account"**.
3. Selecciona tipo: **"Comprador"** (Buyer).
4. País: **Colombia**.
5. Descripción: escribe "Comprador UniSof" (opcional).
6. Haz clic en **Crear**.
7. **Anota en un bloc de notas**:
   - **Usuario:** (ej: TEST-1234567890123456-030115-abc123...)
   - **Contraseña:** (ej: qwerty1234)
   - **Código de verificación:** (6 dígitos para email)

### Paso 1.3: Verificar la cuenta (si te lo pide)

1. Si Mercado Pago te pide verificar el correo, usa el código de 6 dígitos que te dieron.
2. El correo de la cuenta de prueba es algo como `test_user_xxxx@testuser.com` (no necesitas acceder a ese correo; el código lo ves en el panel).

---

## PARTE 2: Configurar Chrome

### Paso 2.1: Permitir cookies

1. En Chrome, escribe en la barra de direcciones: **`chrome://settings/cookies`**
2. Presiona Enter.
3. En la sección **"Cookies y datos de sitios"**:
   - Elige **"Permitir todas las cookies"** (temporalmente para pruebas).
4. Cierra la pestaña de configuración.

### Paso 2.2: Desactivar extensiones que bloqueen

1. Ve a **`chrome://extensions`**
2. Desactiva temporalmente:
   - AdBlock, uBlock Origin, Privacy Badger
   - Cualquier extensión de bloqueo de anuncios o cookies.

---

## PARTE 3: Preparar ngrok y la app

### Paso 3.1: Iniciar la aplicación

1. Abre PowerShell en la carpeta del proyecto.
2. Ejecuta:
   ```powershell
   cd "c:\Users\alfon\Desktop\SEMESTRE 8\Software 3\insumos\insumos"
   .\mvnw.cmd spring-boot:run
   ```
3. Espera a que aparezca "Started InsumosApplication".

### Paso 3.2: Iniciar ngrok

1. Abre **otra** ventana de PowerShell.
2. Ejecuta:
   ```powershell
   ngrok http 8080
   ```
3. Copia la URL que aparece en **Forwarding** (ej: `https://leana-unshingled-marvis.ngrok-free.dev`).

### Paso 3.3: Verificar application.properties

1. Abre `insumos/src/main/resources/application.properties`.
2. En la línea `app.base-url=`, debe estar tu URL de ngrok:
   ```properties
   app.base-url=https://leana-unshingled-marvis.ngrok-free.dev
   ```
3. Si cambiaste la URL de ngrok, actualiza esta línea y reinicia la app (Ctrl+C y volver a ejecutar `spring-boot:run`).

---

## PARTE 4: Flujo de pago (orden exacta)

### Paso 4.1: Visitar ngrok primero

1. Abre una **nueva pestaña** en Chrome.
2. Escribe la URL de ngrok (ej: `https://leana-unshingled-marvis.ngrok-free.dev`).
3. Si aparece la página de advertencia de ngrok:
   - Haz clic en el botón azul **"Visit Site"**.
4. Deberías ver tu app (página de inicio o login).

### Paso 4.2: Iniciar sesión en tu app

1. Ve a la página de login de tu app.
2. Inicia sesión con tu usuario vendedor (el que usa 2FA).

### Paso 4.3: Agregar productos al carrito

1. Ve a **Ventas**.
2. Agrega un producto al carrito (ej: Blaizer clásico).
3. Haz clic en el icono del carrito para abrirlo.

### Paso 4.4: Datos del cliente

1. En "Datos del cliente", busca por cédula o haz clic en **"Crear cliente"**.
2. Completa:
   - Nombre: `Test`
   - Cédula: `123456789`
   - Correo: `test@testuser.com` (usa este para sandbox)
   - Teléfono: `3001234567`
   - Dirección: `Calle 1`

### Paso 4.5: Ir a pagar

1. Haz clic en **"Ir a pagar"**.
2. Serás redirigido al checkout de Mercado Pago (sandbox).

### Paso 4.6: Pagar con cuenta de prueba (NO con tarjeta)

1. En la página de Mercado Pago, **no** elijas "Pagar con tarjeta".
2. Elige **"Iniciar sesión"** o **"Pagar con Mercado Pago"**.
3. Inicia sesión con la **cuenta comprador de prueba** que creaste en la Parte 1:
   - Usuario: (el que anotaste)
   - Contraseña: (la que anotaste)
4. Si te pide el código de verificación, usa el de 6 dígitos del panel.
5. Una vez dentro, Mercado Pago te pedirá agregar una tarjeta. Usa:
   - **Número:** `5254 1336 7440 3564`
   - **CVV:** `123`
   - **Vencimiento:** `11/30`
   - **Nombre:** `APRO`
6. Confirma el pago.

---

## Si prefieres pagar con tarjeta como invitado

Si quieres probar el flujo de invitado con tarjeta:

1. En el checkout de MP, elige **"Pagar con tarjeta"** (o "Otro medio de pago" → Tarjeta).
2. Usa **exactamente**:
   - Número: `5254133674403564` (sin espacios)
   - CVV: `123`
   - Vencimiento: `11/30`
   - Nombre: `APRO`
   - Documento: `123456789`
3. Asegúrate de haber hecho los pasos 2.1 y 2.2 (cookies y extensiones).

---

## PARTE 5: Si sigue fallando

### Opción A: Probar en Microsoft Edge

1. Abre Microsoft Edge (no Chrome).
2. Edge suele ser menos restrictivo con cookies.
3. Repite desde la Parte 4 (visitar ngrok, login, carrito, pagar).

### Opción B: Usar otro correo del cliente

En el Paso 4.4, usa un correo que termine en **@testuser.com**:

- Correo: `comprador_test@testuser.com`

Algunos entornos de sandbox validan mejor con emails de prueba.

### Opción C: Usar localhost (sin ngrok)

Solo si no necesitas webhooks:

1. En `application.properties` pon: `app.base-url=http://localhost:8080`
2. En el checkout, Mercado Pago puede rechazar localhost en algunos casos, pero prueba.
3. Accede a tu app por: `http://localhost:8080/ventas.html`

---

## Checklist final antes de pagar

- [ ] Cuenta comprador de prueba creada en el panel de MP
- [ ] Chrome con "Permitir todas las cookies"
- [ ] Extensiones de bloqueo desactivadas
- [ ] Ventana normal (no incógnito)
- [ ] ngrok corriendo y URL en `app.base-url`
- [ ] App Spring Boot corriendo
- [ ] Visitaste la URL de ngrok y aceptaste "Visit Site"
- [ ] Iniciaste sesión en tu app como vendedor
- [ ] Agregaste productos y completaste datos del cliente
- [ ] Pagas con cuenta comprador de prueba O con tarjeta 5254...3564 y nombre APRO

---

## Resumen de la causa más probable

El error "No pudimos procesar tu pago" en sandbox suele deberse a:

1. **Cookies bloqueadas** por el navegador en dominios ngrok.
2. **Pago con tarjeta como invitado** en un entorno con restricciones.

La opción más estable es **crear una cuenta comprador de prueba** y pagar con "Cuenta Mercado Pago" usando esa cuenta (Parte 4.6).
