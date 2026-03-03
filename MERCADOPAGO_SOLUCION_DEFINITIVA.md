# Mercado Pago - Solución definitiva "No pudimos procesar tu pago"

## Diagnóstico

Tu backend **está bien**. Los logs muestran:
- `total_calculado=858000.0` ✓
- `POST /api/checkout/create-preference` → 200 ✓
- `POST /api/webhooks/mercadopago` → 200 ✓

El error viene de **dos causas**:

1. **Tarjeta incorrecta**: Tus logs muestran `last_four_digits_card_number":"3866"`. Esa tarjeta **no es** una de prueba oficial. Mercado Pago la rechaza.
2. **requestStorageAccessFor: Permission denied**: El navegador bloquea cookies de terceros en ngrok.

---

## Solución paso a paso

### 1. Usa la tarjeta de prueba EXACTA

| Campo | Valor (copia tal cual) |
|-------|------------------------|
| **Número** | `5254 1336 7440 3564` |
| **CVV** | `123` |
| **Vencimiento** | `11/30` |
| **Nombre del titular** | `APRO` (solo eso, sin apellido) |
| **Documento** | `123456789` |

> ⚠️ Si usas una tarjeta que termina en 3866 u otra distinta, el pago **siempre** fallará en sandbox.

### 2. Configura el navegador (obligatorio)

**NO uses modo incógnito.** Usa una ventana normal.

1. Abre Chrome → `chrome://settings/cookies`
2. En "Cookies y datos de sitios", selecciona **"Permitir todas las cookies"** (temporalmente para pruebas).
3. O al menos: desactiva "Bloquear cookies de terceros" para el sitio de ngrok.

### 3. Antes de pagar: visita ngrok primero

1. Abre en una pestaña: `https://leana-unshingled-marvis.ngrok-free.dev`
2. Si sale el aviso de ngrok, haz clic en **"Visit Site"**.
3. Luego ve a tu app, agrega productos al carrito y haz clic en "Ir a pagar".

### 4. En el checkout de Mercado Pago

- Elige **"Pagar con tarjeta"** (como invitado).
- No uses "Pagar con Mercado Pago" si estás logueado con tu cuenta real.
- Ingresa los datos de la tabla del punto 1.

---

## Sobre el "total_amount: null"

Gemini mencionó que `total_amount: null` causa el error. **No es así**:

- La API de Preferencias de Mercado Pago **no acepta** el campo `total_amount` en el request.
- El total se calcula automáticamente desde los items (`unit_price × quantity`).
- El `null` en la respuesta es el formato normal de la API; no indica un error.

Tu backend envía correctamente `unit_price: 429000` y `quantity: 2` → total 858.000 COP.

---

## Los 404 en la consola

Los errores `Failed to load resource: 404` en rutas como `/jms/lgz/background/...` y `armor...` son de **recursos internos de Mercado Pago** (tracking, analytics). No afectan el pago y no puedes corregirlos desde tu código.

---

## Checklist final

- [ ] Tarjeta: `5254 1336 7440 3564` (no otra)
- [ ] Nombre: `APRO`
- [ ] CVV: `123`, vencimiento: `11/30`
- [ ] Ventana normal (no incógnito)
- [ ] Cookies de terceros permitidas
- [ ] Visitaste la URL de ngrok y aceptaste "Visit Site" antes de pagar
- [ ] Pagas como invitado con tarjeta (no con cuenta MP)

Si cumples todo esto y el pago sigue fallando, prueba en otro navegador (Edge o Firefox) o en otro equipo para descartar extensiones o configuraciones locales.
