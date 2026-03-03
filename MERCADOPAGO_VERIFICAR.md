# Verificar configuración de Mercado Pago

Si el pago sigue fallando con "No pudimos procesar tu pago", revisa estos pasos:

---

## 1. Usar credenciales de PRUEBA (no producción)

1. Entra a **https://www.mercadopago.com.co/developers/panel/app**
2. Inicia sesión con tu cuenta de Mercado Pago
3. Haz clic en **"Tus integraciones"**
4. Selecciona tu aplicación (o crea una si no tienes)
5. En el menú izquierdo: **Pruebas → Credenciales de prueba**
6. Copia el **Access Token** de prueba (no el de producción)

En `application.properties` debe estar:
```properties
mercadopago.access-token=TU_ACCESS_TOKEN_DE_PRUEBA
```

> Si no ves "Credenciales de prueba", tu aplicación puede haber sido creada con un producto que no las incluye. Crea una nueva aplicación y elige **"Pagos online"** o **"Checkout Pro"**.

---

## 2. Tipo de aplicación correcto

La aplicación debe soportar **Checkout Pro** o **Pagos online**. Si creaste la app para otro producto, puede que no funcione bien en sandbox.

Para crear una app nueva:
1. Tus integraciones → Crear aplicación
2. Nombre: "UNISOF" (o el que quieras)
3. Producto: **"Pagos online"** o **"Checkout Pro"**
4. Luego ve a Credenciales de prueba y copia el Access Token

---

## 3. Pagar con usuario de prueba (opcional)

A veces el pago falla si pagas como "invitado". Prueba iniciando sesión en Mercado Pago con una **cuenta de prueba comprador**:

1. En Tus integraciones → Tu app → **Cuentas de prueba**
2. Si no tienes, haz clic en **"Crear cuenta de prueba"**
3. Tipo: **Comprador**
4. País: **Colombia**
5. Anota el **Usuario** y **Contraseña** que te dan

Luego, cuando llegues a la página de pago de Mercado Pago:
1. Haz clic en **"Iniciar sesión"** (en lugar de pagar como invitado)
2. Inicia sesión con el usuario y contraseña de la cuenta comprador de prueba
3. Usa la tarjeta de prueba: 5254 1336 7440 3564, CVV 123, vencimiento 11/30
4. Nombre del titular: **APRO**

---

## 4. Tarjetas de prueba (datos exactos)

| Campo | Valor |
|-------|-------|
| Número | 5254 1336 7440 3564 (Mastercard) |
| CVV | 123 |
| Vencimiento | 11/30 |
| Nombre | **APRO** (exactamente así) |
| Documento | 123456789 |

---

## 5. Error "Permission denied" o "requestStorageAccessFor"

Si en la consola del navegador ves `requestStorageAccessFor: Permission denied`:

1. **No uses modo incógnito** – suele bloquear cookies más estrictamente.
2. **Antes de pagar**, abre tu URL de ngrok (ej: `https://xxx.ngrok-free.dev`) y si sale el aviso de ngrok, haz clic en **"Visit Site"**.
3. **Cookies de terceros**: En Chrome → Configuración → Privacidad → Cookies → permite temporalmente "Cookies de terceros" para pruebas.
4. Prueba en una **ventana normal** (no incógnito).

---

## 6. Resumen de verificación

- [ ] Access Token es de **Credenciales de prueba** (no producción)
- [ ] La app es tipo Pagos online / Checkout Pro
- [ ] app.base-url tiene la URL de ngrok (https://...)
- [ ] Probaste iniciando sesión con cuenta comprador de prueba
- [ ] Usaste nombre "APRO" en la tarjeta
- [ ] No usas modo incógnito; aceptaste "Visit Site" en ngrok si apareció
